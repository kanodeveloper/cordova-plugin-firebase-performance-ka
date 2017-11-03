var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebasePerformance";

module.exports = {
    startTrace: function(success, error) {
        exec(success, error, PLUGIN_NAME, "startTrace", []);
    },
    stopTrace: function(success, error) {
        exec(success, error, PLUGIN_NAME, "stopTrace", []);
    }
};