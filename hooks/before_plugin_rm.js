
var androidHelper = require('./lib/android-helper');
var iosHelper = require("./lib/ios-helper");
var utilities = require("./lib/utilities");

module.exports = function(context) {

    var platforms = context.opts.cordova.platforms;

    // Remove the Gradle modifications that were added when the plugin was installed.
    if (platforms.indexOf("android") !== -1) {
        androidHelper.removeBuildToolsFromGradle();
    }
};
