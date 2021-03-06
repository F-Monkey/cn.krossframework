import "./lib/protobuf.js"
import * as webSocket from "./webSocket.js"
import * as constants from "./constants.js"

webSocket.func_map[constants.SEND_MESSAGE_RESULT] = onSendMsgResult;
webSocket.func_map[constants.ENTER_ROOM_RESULT] = onCreateRoomResult;
webSocket.func_map[constants.EXIT_ROOM_RESULT] = onExit;
webSocket.func_map[constants.CLICK_OFF_RESULT] = onClickOff;

let ChatRoomData;
let Login;
let LoginResult;
let Enter;
let EnterResult;
let Stream;
let ChatMessage;
let ChatMessageResult;
let Character;
let Exit;
let ExitResult;
let ClickOff;
let ClickOffResult;

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
        Exit = root.lookup("Exit");
        ExitResult = root.lookup("ExitResult");
        ClickOff = root.lookup("ClickOff");
        ClickOffResult = root.lookup("ClickOffResult");
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
    refreshChatRoomData(chatRoomData);
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

export function enterRoom(){
    let roomId = document.getElementById("room_id").value;
    if(!roomId || roomId == ''){
        alert("please input your roomId");
        return;
    }
    let enter = {};
    enter["roomId"] = roomId;
    let enterContent = Enter.encode(Enter.create(enter)).finish();
    webSocket.send(constants.ENTER_ROOM, enterContent);
}

export function exit(){
    let current_room_id = document.getElementById("current_room_id").value;
    if(!current_room_id || current_room_id == ''){
        return;
    }
    var start = (new Date()).getTime();
    while((new Date()).getTime() - start < 20) {
        continue;
    }
    let exitData = {}
    let exitContent = Exit.encode(Exit.create(exitData)).finish();
    webSocket.send(constants.EXIT_ROOM, exitContent);
    document.getElementById("current_room_id").value = "";
}

function onExit(data, msg){
    let exitResult = ExitResult.decode(data);
    alert(msg);
    let chatRoomData = exitResult.chatRoomData;
    refreshChatRoomData(chatRoomData);
}

function refreshChatRoomData(chatRoomData){
    let character_list_ul = document.getElementById("character_list");
    if(!chatRoomData){
        character_list_ul.innerHTML = "";
        document.getElementById("current_room_id").value = "";
        return;
    }
    let chatter_list = chatRoomData.chatter;
    let master = chatRoomData.master;
    document.getElementById("current_room_id").value = chatRoomData.id;
    if(chatter_list && chatter_list.length > 0){
        character_list_ul.innerHTML = "";
        var character;
        for(var i in chatter_list){
            character = chatter_list[i];
            let li = document.createElement("li");
            let userSpan = document.createElement("span");
            userSpan.innerHTML = character.id;
            if(character.id == master){
                userSpan.style = "color:#F00";
            }
            li.appendChild(userSpan);
            character_list_ul.appendChild(li);
        }
    }
}

export function clickOff() {
    let userId = document.getElementById("click_user_id").value;
    if(!userId || userId == ''){
        alert("please enter click off user id");
        return;
    }
    let clickOff = {};
    clickOff["characterId"] = userId.split(",");
    let clickOffContent = ClickOff.encode(ClickOff.create(clickOff)).finish();
    webSocket.send(constants.CLICK_OFF, clickOffContent);
}

function onClickOff(data,msg) {
    alert(msg);
    let clickOffResult = ClickOffResult.decode(data);
    refreshChatRoomData(clickOffResult.chatRoomData);
}
