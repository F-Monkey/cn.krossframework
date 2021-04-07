package cn.krossframework.state.test.time;

import cn.krossframework.state.DefaultLazyTime;
import cn.krossframework.state.Time;
import org.junit.Test;

public class TimeTest {

    @Test
    public void test() throws InterruptedException {
        Time time = new DefaultLazyTime(100);
        for (; ; ) {
            Thread.sleep(1000);
            System.out.println(time.getCurrentTimeMillis());
        }
    }
}
