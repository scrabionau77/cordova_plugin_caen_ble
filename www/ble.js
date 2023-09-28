var exec = require('cordova/exec')

var caenBle = {
    discoverDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "discoverDevices", []);
    },

    connectToDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "connectToDevice", [address]);
    },

    stopDiscovery: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "stopDiscovery", []);
    },

    disconnectDevice: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "disconnectDevice", []);
    },

    requestPermissions: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "requestPermissions", []);
    },

    startTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "startTagCheck", []);
    },

    stopTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MaeCaenBLE", "stopTagCheck", []);
    }
};

module.exports = caenBle;