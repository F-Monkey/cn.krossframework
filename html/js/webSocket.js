import "./lib/protobuf.js"
import * as constants from "./constants.js"

export let ResultMessage;
export let Package;
export let PackageGroup;

export let func_map = {};

protobuf.load("/proto/Cmd.proto", function(error, root){
       if(error) throw error;
       ResultMessage = root.lookup("ResultMessage");
       Package = root.lookup("Package");
       PackageGroup = root.lookup("PackageGroup");
    });

function onMessage(data){
    const fileReader = new FileReader();
    fileReader.readAsArrayBuffer(data);
    fileReader.onloadend = function(){
            const u8 = new Uint8Array(this.result);
            let packageGroup = PackageGroup.decode(u8);
            var resultMsg;
            var data;
            for(var index in packageGroup.packages){
                var pkg = packageGroup.packages[index];
                resultMsg = pkg.resultMsg
                if(constants.SUCCESS == resultMsg.code){
                    var func = func_map[pkg.cmdType];
                    if(func){
                        func(pkg.content, resultMsg.msg);
                    }
                    continue;
                }
                if(constants.FAIL == resultMsg.code){
                    alert(resultMsg.msg);
                    continue;
                }
                if(constants.ERROR == resultMsg.code){
                    console.info(resultMsg.msg);
                }
            }
    }
}

let ws;

function loadWebSocket(){
    if(!window.WebSocket){
        console.log("this browser does not supports 'WebSocket'");
        return;
    }

    let wsUri = "ws://192.168.111.1:8888/chat";

    ws = new WebSocket(wsUri);
    ws.onopen = function(evt) {
        console.log("WebSocket connect success");
    };
    ws.onclose = function(evt) {
        console.log("WebSocket closed")
    };
    ws.onmessage = function(evt) {
        onMessage(evt.data);
    };
    ws.onerror = function(evt) {
        console.log("WebSocket error");
    };
}

export function send(cmdType, content){
    if(!ws){
        alert("webSocket has not initialized");
        return;
    }
    if(ws.readyState != 1){
        alert("webSocket has not open yet!");
        return;
    }
    let pkg = {};
    pkg["cmdType"] = cmdType;
    if(content){
        pkg.content = content;
    }
    let pkgContent = webSocket.Package.encode(webSocket.Package.create(pkg)).finish();
    ws.send(pkgContent);
}

loadWebSocket();

