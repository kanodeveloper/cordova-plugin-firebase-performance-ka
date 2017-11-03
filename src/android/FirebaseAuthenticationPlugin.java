package by.chemerisuk.cordova.firebase;

import android.util.Log;

import android.support.annotation.NonNull;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


public class FirebaseAuthenticationPlugin extends CordovaPlugin implements OnCompleteListener<AuthResult> {
    private static final String TAG = "FirebaseAuthentication";

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider phoneAuthProvider;
    private CallbackContext signinCallback;
    private GoogleApiClient googleApiClient;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Authentication plugin");

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.phoneAuthProvider = PhoneAuthProvider.getInstance();

        Context context = this.cordova.getActivity().getApplicationContext();
        String defaultClientId = getDefaultClientId(context);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(defaultClientId)
                .requestEmail()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
        //firebaseAuth = FirebaseAuth.getInstance();
        //firebaseAuth.addAuthStateListener(this);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getIdToken")) {
            getIdToken(args.getBoolean(0), callbackContext);
            return true;
        } else if (action.equals("signInWithEmailAndPassword")) {
            signInWithEmailAndPassword(args.getString(0), args.getString(1), callbackContext);
            return true;
        } else if (action.equals("signInWithVerificationId")) {
            signInWithVerificationId(args.getString(0), args.getString(1), callbackContext);
            return true;
        } else if (action.equals("verifyPhoneNumber")) {
            verifyPhoneNumber(args.getString(0), args.getLong(1), callbackContext);
            return true;
        } else if (action.equals("signOut")) {
            signOut(callbackContext);
            return true;
        } else if (action.equals("signInWithGoogle")) {
            signInWithGoogle(callbackContext);
            return true;
        }

        return false;
    }

    private void getIdToken(final boolean forceRefresh, final CallbackContext callbackContext) throws JSONException {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    callbackContext.error("User is not authorized");
                } else {
                    user.getIdToken(forceRefresh)
                        .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    callbackContext.success(task.getResult().getToken());
                                } else {
                                    callbackContext.error(task.getException().getMessage());
                                }
                            }
                        });
                }
            }
        });
    }

    @Override
    public void onComplete(Task<AuthResult> task) {

        if (this.signinCallback == null) return;

        if (task.isSuccessful()) {
            FirebaseUser user = task.getResult().getUser();
            this.signinCallback.success(getProfileData(user));
        } else {
            this.signinCallback.error(task.getException().getMessage());
        }

        this.signinCallback = null;
    }

    private void signInWithEmailAndPassword(final String email, final String password, CallbackContext callbackContext) throws JSONException {
        this.signinCallback = callbackContext;

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(cordova.getActivity(), FirebaseAuthenticationPlugin.this);
            }
        });
    }

    private void signInWithVerificationId(final String verificationId, final String code, final CallbackContext callbackContext) {
        final PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        this.signinCallback = callbackContext;

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(cordova.getActivity(), FirebaseAuthenticationPlugin.this);
                } else {
                    user.updatePhoneNumber(credential)
                        .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    callbackContext.success(getProfileData(firebaseAuth.getCurrentUser()));
                                } else {
                                    callbackContext.error(task.getException().getMessage());
                                }
                            }
                        });
                }
            }
        });
    }

    private void verifyPhoneNumber(final String phoneNumber, final long timeout, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                phoneAuthProvider.verifyPhoneNumber(phoneNumber, timeout, MILLISECONDS, cordova.getActivity(),
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user == null) {
                                firebaseAuth.signInWithCredential(credential);
                            } else {
                                user.updatePhoneNumber(credential);
                            }
                        }

                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            callbackContext.success(verificationId);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            callbackContext.error(e.getMessage());
                        }
                    }
                );
            }
        });
    }

    private void signOut(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                firebaseAuth.signOut();

                callbackContext.success();
            }
        });
    }

    private void signInWithGoogle(final CallbackContext callbackContext) {

        this.signinCallback = callbackContext;

        final CordovaPlugin plugin = this;

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                cordova.startActivityForResult(plugin, signInIntent, RC_SIGN_IN);
            }
        });
    }

    private static JSONObject getProfileData(FirebaseUser user) {
        JSONObject result = new JSONObject();

        try {
            result.put("uid", user.getUid());
            result.put("displayName", user.getDisplayName());
            result.put("email", user.getEmail());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("photoURL", user.getPhotoUrl());
            result.put("providerId", user.getProviderId());
            result.put("token", user.getIdToken(false));
        } catch (JSONException e) {
            Log.e(TAG, "Fail to process getProfileData", e);
        }

        return result;
    }

    private String getDefaultClientId(Context context) {

        String packageName = context.getPackageName();
        int id = context.getResources().getIdentifier("default_web_client_id", "string", packageName);
        return context.getString(id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                JSONObject resultObj = new JSONObject();

                try {
                    resultObj.put("token", account.getIdToken());
                    this.signinCallback.success(resultObj);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to process getIdToken", e);
                    this.signinCallback.error("failed");
                }

                //call the following to auth and connect with firebase user
                //firebaseAuthWithGoogle(account);

            } else {
                this.signinCallback.error("failed");
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        this.firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this);
    }
}
