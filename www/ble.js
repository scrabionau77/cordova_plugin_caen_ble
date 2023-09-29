var exec = require('cordova/exec')

var CaenBle = {
    discoverDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "discoverDevices", []);
    },

    connectToDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "connectToDevice", [address]);
    },

    stopDiscovery: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "stopDiscovery", []);
    },

    disconnectDevice: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "disconnectDevice", []);
    },

    requestPermissions: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "requestPermissions", []);
    },

    startTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "startTagCheck", []);
    },

    stopTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "stopTagCheck", []);
    }
};

module.exports = CaenBle;