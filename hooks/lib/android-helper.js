//sourced from https://github.com/sarriaroman/FabricPlugin/blob/master/hooks/lib/android-helper.js

var fs = require("fs");
var path = require("path");
var utilities = require("./utilities");

module.exports = {

    addBuildToolsGradle: function() {

        var buildGradle = utilities.readBuildGradle();

        buildGradle +=  [
            "",
            "// Firebase Performance Plugin - Start ",
            "buildscript {",
            "    repositories {",
            "        jcenter()",
            "    }",
            "    dependencies {",
            "        classpath 'com.google.firebase:firebase-plugins:1.+'",
            "    }",
            "}",
            "",
            "apply plugin: 'com.google.firebase.firebase-perf'",
            "// Firebase Performance Plugin - End",
        ].join("\n");

        utilities.writeBuildGradle(buildGradle);
    },

    removeBuildToolsFromGradle: function() {

        var buildGradle = utilities.readBuildGradle();

        buildGradle = buildGradle.replace(/\n\/\/ Firebase Performance Plugin - Start[\s\S]*\/\/ Firebase Performance Plugin - End/, "");

        utilities.writeBuildGradle(buildGradle);
    }
};