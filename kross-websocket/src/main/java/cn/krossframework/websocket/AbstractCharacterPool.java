package cn.krossframework.websocket;

import cn.krossframework.commons.thread.AutoTask;
import cn.krossframework.state.AbstractStateGroupPool;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCharacterPool implements CharacterPool {

    private static final Logger log = LoggerFactory.getLogger(AbstractCharacterPool.class);

    private final CharacterFactory factory;

    private volatile ConcurrentHashMap<String, Character> characterMap;

    public AbstractCharacterPool(CharacterFactory characterFactory) {
        Preconditions.checkNotNull(characterFactory);
        this.factory = characterFactory;
        this.characterMap = new ConcurrentHashMap<>();
        new AutoTask(0, 1) {
            @Override
            protected void run() {
                AbstractCharacterPool.this.removeInvalidCharacter();
            }
        }.start();
    }

    protected void removeInvalidCharacter() {
        if (this.characterMap.size() <= 0) {
            return;
        }
        final ConcurrentHashMap<String, Character> characterMap = this.characterMap;
        log.info("start remove offline character, currentSize: {}", characterMap.size());
        characterMap.entrySet().removeIf(e -> e.getValue().isOffLine());
        this.characterMap = characterMap;
        log.info("end remove offline character, currentSize: {}", this.characterMap.size());
    }

    @Override
    public FetchCharacter findOrCreate(final Session session, final User user) {
        final ConcurrentHashMap<String, Character> characterMap = this.characterMap;
        boolean[] isNew = {false};
        Character character = characterMap.computeIfAbsent(user.getUid(), (id) -> {
            isNew[0] = true;
            return this.factory.create(session, user);
        });
        this.characterMap = characterMap;
        return new FetchCharacter(isNew[0], character);
    }
}
