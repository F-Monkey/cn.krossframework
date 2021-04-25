import "./lib/protobuf.js"
import * as webSocket from "./webSocket.js"
import * as constants from "./constants.js"

webSocket.func_map[constants.SEND_MESSAGE_RESULT] = onSendMsgResult;
webSocket.func_map[constants.EXISTS_ROOM_RESULT] = onExists;


let ChatRoomData;
let Login;
let LoginResult;
let Enter;
let EnterResult;
let Stream;
let ChatMessage;
let ChatMessageResult;
let Character;
let Exists;
let ExistsResult;

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
        Exists = root.lookup("Exists");
        ExistsResult = root.lookup("ExistsResult");
});

protobuf.load("/proto/Entity.proto",function(error, root){
        if(error) throw error;
        Character = root.lookup("Character");
});


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
    webSocket.send(constants.LOGIN,loginDataContent);
}

function onLoginResult(data){
    let loginResult = LoginResult.decode(data);
    document.getElementById("user_id").value = loginResult.uid;
}

export function createRoom(){
    let enter = {};
    let enterContent = Enter.encode(Enter.create(enter)).finish();
    webSocket.func_map[constants.CREATE_ROOM_RESULT] = onCreateRoomResult;
    webSocket.send(constants.CREATE_ROOM, enterContent);
}

function onCreateRoomResult(data, msg){
    alert(msg);
    let chatRoomData = ChatRoomData.decode(data);
    document.getElementById("current_room_id").value = chatRoomData.id;
}

export function sendMsg(){
    let msg = document.getElementById("msg").value;
    if(!msg || msg == ''){
        alert("empty msg");
        return;
    }
    let chatMsg = {};
    chatMsg["msg"] = msg;
    let chatMsgContent = ChatMessage.encode(ChatMessage.create(chatMsg)).finish();
    webSocket.func_map[constants.SEND_MESSAGE_RESULT] = onSendMsgResult;
    webSocket.send(constants.SEND_MESSAGE, chatMsgContent);
}

function onSendMsgResult(data){
    let chatMsgResult = ChatMessageResult.decode(data);
    let li = document.createElement("li");
    let userSpan = document.createElement("span");
    userSpan.innerHTML = chatMsgResult.from +": ";
    let span = document.createElement("span");
    span.innerHTML = chatMsgResult.msg;
    li.appendChild(userSpan);
    li.appendChild(span);
    document.getElementById("history").appendChild(li);
}


export function joinRoom(){
    let roomId = document.getElementById("room_id").value;
    let enter = {};
    if(roomId && roomId != ''){
        enter["roomId"] = roomId;
    }
    let enterContent = Enter.encode(Enter.create(enter)).finish();
    webSocket.func_map[constants.CREATE_ROOM_RESULT] = onCreateRoomResult;
    webSocket.send(constants.CREATE_ROOM, enterContent);
}

export function exists(){
    let existsData = {}
    let existsContent = Exists.encode(Exists.create(existsData)).finish();
    webSocket.send(constants.EXISTS_ROOM, existsContent);
}

function onExists(data, msg){
    let existsResult = EnterResult.decode(data);
    let chatRoomData = existsResult.chatRoomData;
    alert(msg);
}

