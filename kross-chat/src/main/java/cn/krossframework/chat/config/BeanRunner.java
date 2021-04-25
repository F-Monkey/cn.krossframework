package cn.krossframework.chat.config;

import cn.krossframework.commons.beam.InitializeBean;
import com.google.common.base.Preconditions;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BeanRunner implements ApplicationRunner, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.initPropertiesSet();
    }

    private void initPropertiesSet() throws Exception {
        Map<String, InitializeBean> beans = this.applicationContext.getBeansOfType(InitializeBean.class);
        for (InitializeBean initializeBean : beans.values()) {
            initializeBean.afterPropertiesSet();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Preconditions.checkNotNull(applicationContext);
        this.applicationContext = applicationContext;
    }
}
