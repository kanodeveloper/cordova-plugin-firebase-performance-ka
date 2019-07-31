var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebasePerformance";

module.exports = {
    startTrace: function(key, success, error) {
        exec(success, error, PLUGIN_NAME, "startTrace", [key]);
    },
    stopTrace: function(key, success, error) {
        exec(success, error, PLUGIN_NAME, "stopTrace", [key]);
    }
};