
/**
 * Utilities and shared functionality for the build hooks.
 */

var path = require("path");
var fs = require("fs");

/**
 * Used to get the path to the build.gradle file for the Android project.
 *
 * @returns {string} The path to the build.gradle file.
 */
function getBuildGradlePath() {
    return path.join("platforms", "android", "build.gradle");
};

module.exports = {

    /**
     * Used to read the contents of the Android project's build.gradle file.
     *
     * @returns {string} The contents of the Android project's build.gradle file.
     */
    readBuildGradle: function() {
        return fs.readFileSync(getBuildGradlePath(), "utf-8");
    },

    /**
     * Used to write the given build.gradle contents to the Android project's
     * build.gradle file.
     *
     * @param {string} buildGradle The body of the build.gradle file to write.
     */
    writeBuildGradle: function(buildGradle) {
        fs.writeFileSync(getBuildGradlePath(), buildGradle);
    }
};
