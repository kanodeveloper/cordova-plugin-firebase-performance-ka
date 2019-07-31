package com.kanoapps.cordova.firebase;

import java.util.HashMap;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import android.content.Context;

public class FirebasePerformancePlugin extends CordovaPlugin {
  private static final String TAG = "FirebasePerformancePlgn";

  private FirebasePerformance firebasePerformance;

  private HashMap<String, Trace> myTraces = new HashMap<String, Trace>();

  @Override
  protected void pluginInitialize() {
    Log.d(TAG, "Starting Firebase Analytics plugin");

    Context context = this.cordova.getActivity().getApplicationContext();

    this.firebasePerformance = FirebasePerformance.getInstance();
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if ("startTrace".equals(action)) {
      startTrace(callbackContext, args.getString(0));

      return true;
    } else if ("stopTrace".equals(action)) {
      stopTrace(callbackContext, args.getString(0));

      return true;
    }

    return false;
  }

  private void startTrace(CallbackContext callbackContext, String name) throws JSONException {

    final FirebasePerformancePlugin self = this;

    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        Trace myTrace = self.firebasePerformance.newTrace(name);
        myTrace.start();
        self.myTraces.put(name, myTrace);
      }
    });

    callbackContext.success();
  }

  private void stopTrace(CallbackContext callbackContext, String name) throws JSONException {

    final FirebasePerformancePlugin self = this;

    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        if (self.myTraces.containsKey(name)) {
          self.myTraces.get(name).stop();

          self.myTraces.remove(name);
        }
      }
    });

    callbackContext.success();
  }
}