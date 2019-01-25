//sourced from https://github.com/sarriaroman/FabricPlugin/blob/master/hooks/after_plugin_add.js

var androidHelper = require('./lib/android-helper');
var utilities = require("./lib/utilities");

module.exports = function(context) {

    var platforms = context.opts.cordova.platforms;

    // Modify the Gradle build file to add a task that will upload the debug symbols
    // at build time.
    if (platforms.indexOf("android") !== -1) {
        androidHelper.removeBuildToolsFromGradle();
        androidHelper.addBuildToolsGradle();
    }
};