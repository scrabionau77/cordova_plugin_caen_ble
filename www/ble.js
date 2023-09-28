var exec = require('cordova/exec')

var caenBle = {
    discoverDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "discoverDevices", []);
    },

    connectToDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "connectToDevice", [address]);
    },

    stopDiscovery: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "stopDiscovery", []);
    },

    disconnectDevice: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "disconnectDevice", []);
    },

    requestPermissions: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "requestPermissions", []);
    },

    startTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "startTagCheck", []);
    },

    stopTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "caenBle", "stopTagCheck", []);
    }
};

module.exports = caenBle;