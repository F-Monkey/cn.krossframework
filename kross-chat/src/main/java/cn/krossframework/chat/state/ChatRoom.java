package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.chat.state.data.ChatTask;
import cn.krossframework.commons.model.ResultCode;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.DefaultErrorState;
import cn.krossframework.state.data.Task;
import cn.krossframework.state.util.Time;
import cn.krossframework.websocket.Character;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom extends AbstractStateGroup {

    private List<Chat.ChatMessageResult> history;

    private String master;

    private List<String> assistants;

    private List<Character> chatterList;

    public ChatRoom(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
        this.assistants = new ArrayList<>();
        this.chatterList = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        if (!super.tryEnterGroup(task)) {
            return false;
        }
        if (task instanceof ChatTask) {
            Character character = ((ChatTask) task).getCharacter();
            if (this.master == null) {
                this.master = character.getId();
            }
            boolean containsCharacter = false;
            for (Character c : this.chatterList) {
                if (character.getId().equals(c.getId())) {
                    containsCharacter = true;
                    break;
                }
            }
            if (!containsCharacter) {
                this.chatterList.add(character);
            }
            character.setCurrentGroupId(this.id);
            character.sendMsg(ChatCmdUtil.enterRoomResult(ResultCode.SUCCESS, "进入房间：" + this.id, this));
            String broadCastMsg = "[" + character.getNickName() + "] 进入房间";
            this.broadCast(ChatCmdUtil.enterRoomResult(ResultCode.SUCCESS, broadCastMsg, this), character.getId());
            return true;
        }
        return false;
    }

    public void broadCast(Command.PackageGroup packageGroup, String... excludeId) {
        if (excludeId != null && excludeId.length > 0) {
            for (String id : excludeId) {
                for (Character character : this.chatterList) {
                    if (character.getId().equals(id)) {
                        continue;
                    }
                    character.sendMsg(packageGroup);
                }
            }
            return;
        }
        for (Character character : this.chatterList) {
            character.sendMsg(packageGroup);
        }
    }

    public String getMaster() {
        return this.master;
    }

    public List<Character> getChatterList() {
        return chatterList;
    }

    public List<Chat.ChatMessageResult> getHistory() {
        return history;
    }

    public void addChatMessage(Chat.ChatMessageResult chatMessageResult) {
        this.history.add(chatMessageResult);
    }

    @Override
    public boolean canDeposed() {
        return (super.currentState == null || DefaultErrorState.CODE.equals(super.currentState.getCode()))
                && (this.chatterList.isEmpty() || isAllExists());
    }

    private boolean isAllExists() {
        for (Character chatter : this.chatterList) {
            Long currentGroupId = chatter.getCurrentGroupId();
            if (currentGroupId != null && currentGroupId == this.id) {
                return false;
            }
        }
        return true;
    }
}
