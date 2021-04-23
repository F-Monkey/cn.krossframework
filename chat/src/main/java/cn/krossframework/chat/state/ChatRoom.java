package cn.krossframework.chat.state;

import cn.krossframework.chat.cmd.ChatCmdUtil;
import cn.krossframework.proto.Command;
import cn.krossframework.proto.chat.Chat;
import cn.krossframework.state.AbstractStateGroup;
import cn.krossframework.state.Task;
import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.util.Time;
import cn.krossframework.websocket.Character;

import java.util.List;

public class ChatRoom extends AbstractStateGroup {

    private List<Chat.ChatMessageResult> history;

    private String master;

    private List<String> assistants;

    private List<Character> chatterList;

    public ChatRoom(long id, Time time, StateGroupConfig stateGroupConfig) {
        super(id, time, stateGroupConfig);
    }

    @Override
    public boolean tryEnterGroup(Task task) {
        if (!super.tryEnterGroup(task)) {
            return false;
        }
        if (task instanceof ChatTask) {
            Character character = ((ChatTask) task).getCharacter();
            this.chatterList.add(character);
            character.sendMsg(ChatCmdUtil.enterRoomResult("进入房间：" + this.id, this));
            String broadCastMsg = "[" + character.getNickName() + "] 进入房间";
            this.broadCast(character.getId(), ChatCmdUtil.enterRoomResult(broadCastMsg, this));
        }
        return false;
    }

    public void broadCast(String excludeId, Command.PackageGroup packageGroup) {
        for (Character character : this.chatterList) {
            if (excludeId != null && excludeId.equals(character.getId())) {
                continue;
            }
            character.sendMsg(packageGroup);
        }
    }

    public List<Character> getChatterList() {
        return chatterList;
    }

    public List<Chat.ChatMessageResult> getHistory() {
        return history;
    }

    @Override
    public boolean tryAddTask(Task task) {
        return super.tryAddTask(task);
    }
}
