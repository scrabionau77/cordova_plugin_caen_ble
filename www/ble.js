var exec = require('cordova/exec');
var connectionStatusCallback = null;

var CaenBle = {
    checkBtOn: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "checkBtOn", []);
    },

    requestPermissions: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "requestPermissions", []);
    },

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

    startTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "startTagCheck", []);
    },

    stopTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "CaenBle", "stopTagCheck", []);
    },

    registerConnectionCallback: function(callback) {
        connectionStatusCallback = callback;
        cordova.exec(function(status) {
            if (connectionStatusCallback) {
                connectionStatusCallback(status);
            }
        }, null, "CaenBle", "registerConnectionCallback", []);
    },
};

module.exports = CaenBle;