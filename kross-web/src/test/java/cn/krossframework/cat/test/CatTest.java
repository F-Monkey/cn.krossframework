package cn.krossframework.cat.test;

import cn.krossframework.state.ExecuteTask;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.web.WebApplication;
import cn.krossframework.web.cat.CatTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

@SpringBootTest(classes = WebApplication.class)
@RunWith(SpringRunner.class)
public class CatTest {

    @Autowired
    WorkerManager workerManager;

    @Autowired
    StateGroupPool stateGroupPool;

    public void multiTest(long groupId) {
        ExecuteTask walkTask = new ExecuteTask(groupId, new CatTask(2), () -> {
            System.out.println("walk error");
        });

        ExecuteTask sleepTask = new ExecuteTask(groupId, new CatTask(3), () -> {
            System.out.println("sleep error");
        });

        ExecuteTask eatTask = new ExecuteTask(groupId, new CatTask(4), () -> {
            System.out.println("eat error");
        });

        workerManager.enter(new ExecuteTask(groupId, new CatTask(10), () -> {
            System.out.println("enter error");
        }));


        StateGroup stateGroup;
        while ((stateGroup = stateGroupPool.find(groupId)) == null || stateGroup.getCurrentWorkerId() == null) {

        }

        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                workerManager.addTask(walkTask);
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                workerManager.addTask(sleepTask);
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {
                }
                workerManager.addTask(eatTask);
            }
        }).start();

        try {
            Thread.sleep(10_000);
        } catch (InterruptedException ignore) {
        }
        workerManager.addTask(new ExecuteTask(groupId, new CatTask(1), () -> {
            System.out.println("stop fail");
        }));
    }

    @Test
    public void test() throws InterruptedException {
        for (long i = 1; i < 2000; i++) {
            final long groupId = i;
            Thread.sleep(200);
            new Thread(() -> this.multiTest(groupId)).start();
        }
        new CountDownLatch(1).await();
    }
}
