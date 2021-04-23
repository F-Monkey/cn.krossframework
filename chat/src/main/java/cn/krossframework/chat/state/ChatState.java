package cn.krossframework.chat.state;

import cn.krossframework.state.data.AbstractState;

public class ChatState extends AbstractState {

    public static final String CODE = "chat";

    @Override
    public String getCode() {
        return CODE;
    }
}
