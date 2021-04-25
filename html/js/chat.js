import "./lib/protobuf.js"
import * as webSocket from "./webSocket.js"
import * as constants from "./constants.js"

let ChatRoomData;
let Login;
let LoginResult;
let Enter;
let EnterResult;
let Stream;
let ChatMessage;
let ChatMessageResult;
let Character;

protobuf.load("/proto/Chat.proto", function(error, root){
        if(error) throw error;
        ChatRoomData = root.lookup("ChatRoomData");
        Login = root.lookup("Login");
        LoginResult = root.lookup("LoginResult");
        Enter = root.lookup("Enter");
        EnterResult = root.lookup("EnterResult");
        Stream = root.lookup("Stream");
        ChatMessage = root.lookup("ChatMessage");
        ChatMessageResult = root.lookup("ChatMessageResult");
});

protobuf.load("/proto/Entity.proto",function(error, root){
        if(error) throw error;
        Character = root.lookup("Character");
});

function buildPackage(cmdType,content){
    let pkg = {};
    pkg["cmdType"] = cmdType;
    if(content){
        pkg.content = content;
    }
    return webSocket.Package.encode(webSocket.Package.create(pkg)).finish();
}

export function userLogin(){
    let username = document.getElementById("username").value;
    let user_id = document.getElementById("user_id").value;
    let login = {};
    if(username && username != ''){
        login["username"] = username;
    }
    if(user_id && user_id != ''){
        login["uid"] = user_id;
    }
    let loginDataContent = Login.encode(Login.create(login)).finish();
    webSocket.func_map[constants.LOGIN_RESULT] = onLoginResult;
    webSocket.send(buildPackage(constants.LOGIN,loginDataContent));
}

function onLoginResult(data){
    let loginResult = LoginResult.decode(data);
    alert(loginResult.uid);
}
