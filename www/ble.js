var exec = require('cordova/exec')

var caenBle = {
    discoverDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "discoverDevices", []);
    },

    connectToDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "connectToDevice", [address]);
    },

    stopDiscovery: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "stopDiscovery", []);
    },

    disconnectDevice: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "disconnectDevice", []);
    },

    requestPermissions: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "requestPermissions", []);
    },

    startTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "startTagCheck", []);
    },

    stopTagCheck: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "MyBluetoothPlugin", "stopTagCheck", []);
    }
};

module.exports = caenBle;