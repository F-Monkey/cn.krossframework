require("lib/protobuf.js")

if(!window.WebSocket){
    console.log("this browser does not supports 'WebSocket'");
    return;
}



let wsUri = "ws://localhost:8000";

let ws = new WebSocket(wsUri);
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


protobuf.load("/proto/Cmd.proto", function(error, root){
       if(error) throw error;

    });

function onMessage(data){

}
