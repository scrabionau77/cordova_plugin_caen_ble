function discoverDevices(success, error) {
    var code = window.prompt("Type Y to success response, any other to fail response");
    if(code == "Y") {
        var result = {
            success: true
        };
        success(result);
    } else {
        error("No devices");
    }
}

function connectToDevice(address, success, error) {
    success();
}

function stopDiscovery(success, error) {
    success();
}

function disconnectDevice(success, error) {
    success();
}

function requestPermissions(success, error) {
    success();
}

function startTagCheck(success, error) {
    var now = new Date();
    var result = {
        NumberTags: 1,
        TagList: [
            { Antenna: 0, Id: "E28011700000020F71C535F1", TimeStamp: now.getTime() }
        ]
    }
    success(result);
}

function stopTagCheck(success, error) {
    success();
}

module.exports = {
    discoverDevices: discoverDevices,
    connectToDevice: connectToDevice,
    stopDiscovery: stopDiscovery,
    disconnectDevice: disconnectDevice,
    requestPermissions: requestPermissions,
    startTagCheck: startTagCheck,
    stopTagCheck: stopTagCheck,
};

require("cordova/exec/proxy").add("caenBle",module.exports);