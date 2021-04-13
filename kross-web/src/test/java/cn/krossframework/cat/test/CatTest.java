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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

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
        LinkedBlockingQueue<ExecuteTask> executeTasks = new LinkedBlockingQueue<>(3000);
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(walkTask);
        }
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(sleepTask);
        }
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(eatTask);
        }
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();

                    if (poll != null) {
                        workerManager.addTask(poll);
                    } else {
                        System.out.println("end add task");
                        this.interrupt();
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();

                    if (poll != null) {
                        workerManager.addTask(poll);
                    } else {
                        System.out.println("end add task");
                        this.interrupt();
                    }
                }
            }
        };

        Thread t3 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();
                    if (poll != null) {
                        workerManager.addTask(poll);
                    } else {
                        System.out.println("end add task");
                        this.interrupt();
                    }
                }
            }
        };

        t1.start();
        t2.start();
        t3.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }
        workerManager.addTask(new ExecuteTask(groupId, new CatTask(1), () -> {
            System.out.println("stop fail");
        }));
    }

    static class TaskAddThread extends Thread {

        private final Object Lock = new Object();

        LinkedBlockingQueue<Runnable> task = new LinkedBlockingQueue<>();


        void addTask(Runnable task) {
            this.task.offer(task);
            synchronized (this.Lock) {
                this.Lock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                Runnable poll = task.poll();
                if (poll != null) {
                    poll.run();
                }
                synchronized (this.Lock) {
                    try {
                        this.Lock.wait(20);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }

    @Test
    public void test() throws InterruptedException {
        Map<Long, TaskAddThread> threadMap = new HashMap<>();
        for (long i = 0; i < 10L; i++) {
            TaskAddThread taskAddThread = new TaskAddThread();
            threadMap.put(i, taskAddThread);
            taskAddThread.start();
        }
        for (long i = 1; i <= 2000; i++) {
            final long groupId = i;
            TaskAddThread taskAddThread = threadMap.get(groupId % 10);
            taskAddThread.addTask(() -> {
                this.multiTest(groupId);
            });
        }
        new CountDownLatch(1).await();
    }
}
