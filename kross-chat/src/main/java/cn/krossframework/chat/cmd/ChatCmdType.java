package cn.krossframework.chat.cmd;

public interface ChatCmdType {
    int LOGIN = 1000;
    int LOGIN_RESULT = 1001;

    int LOGOUT = 1002;
    int LOGOUT_RESULT = 1003;

    int CLICK_OFF = 1004;
    int CLICK_OFF_RESULT = 1005;

    int CREATE_ROOM = 1006;

    int CREATE_ROOM_RESULT = 1007;

    int SEND_MESSAGE = 1008;

    int SEND_MESSAGE_RESULT = 1009;
}
