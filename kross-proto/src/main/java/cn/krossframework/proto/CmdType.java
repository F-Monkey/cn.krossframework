package cn.krossframework.proto;

public interface CmdType {
    int CREATE_ROOM = 1000;
    int CREATE_ROOM_RESULT = 1001;

    int ENTER = 1002;
    int ENTER_RESULT = 1003;

    int SEND_MESSAGE = 1004;
    int SEND_MESSAGE_RESULT = 1005;

    int CLICK_OFF = 1006;
    int CLICK_OFF_RESULT = 1007;
}
