package cn.krossframework.web.config;

import cn.krossframework.state.*;
import cn.krossframework.web.cat.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
public class StateConfig {

    private static final Logger log = LoggerFactory.getLogger(StateConfig.class);

    @Bean
    WorkerManager workerManager() throws Exception {
        DefaultLazyTime defaultLazyTime = new DefaultLazyTime(100);
        CatFactory catFactory = new CatFactory(defaultLazyTime, new StateGroupConfig() {

        }) {
            @Override
            public StateGroup create(long id) {
                StateGroup stateGroup = new Cat(id, super.time, super.stateGroupConfig);
                stateGroup.addState(new Sleep());
                stateGroup.addState(new Eat());
                stateGroup.addState(new Walk());
                stateGroup.addState(new DefaultErrorState());
                stateGroup.initAndSetCurrentState(Walk.CODE);
                return stateGroup;
            }
        };
        CatPool catPool = new CatPool(catFactory);
        catPool.setRemoveDeposedStateGroupPeriod(2000);
        catPool.afterPropertiesSet();
        return new AbstractWorkerManager(500, 20, 10,
                2000, 2000, 20, catPool) {
        };
    }
}
