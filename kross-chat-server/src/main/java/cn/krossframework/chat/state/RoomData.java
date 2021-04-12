package cn.krossframework.chat.state;

import cn.krossframework.proto.Chat;
import cn.krossframework.state.AbstractStateData;
import cn.krossframework.commons.web.Character;

import java.util.ArrayList;
import java.util.List;

public class RoomData extends AbstractStateData {

    private final List<Chat.ChatMessage> chatMessageList;

    private final List<Character> chatterList;

    public RoomData() {
        this.chatMessageList = new ArrayList<>();
        this.chatterList = new ArrayList<>();
    }

    public List<Character> getChatterList() {
        return this.chatterList;
    }

    public List<Chat.ChatMessage> getChatMessageList() {
        return this.chatMessageList;
    }

    public void addChatMessage(Chat.ChatMessage chatMessage) {
        this.chatMessageList.add(chatMessage);
    }

    public void addCharacter(Character character) {
        this.chatterList.add(character);
    }
}
