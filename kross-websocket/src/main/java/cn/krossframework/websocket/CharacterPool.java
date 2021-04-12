package cn.krossframework.websocket;

import cn.krossframework.commons.web.Character;
import cn.krossframework.commons.web.Session;
import com.google.common.base.Preconditions;

public interface CharacterPool {
    class FetchCharacter {
        private final boolean isNew;
        private final Character character;

        public FetchCharacter(boolean isNew,
                              Character character) {
            Preconditions.checkNotNull(character);
            this.character = character;
            this.isNew = isNew;
        }

        public Character getCharacter() {
            return character;
        }

        public boolean isNew() {
            return isNew;
        }
    }

    FetchCharacter findOrCreate(Session session, User user);
}
