package cn.krossframwork.commons;


import cn.krossframework.commons.collection.RingBuffer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class RingBufferTest {

    public static final int size = 10000000;

    public static void testLinkedBlockingQueue() throws InterruptedException {
        LinkedBlockingQueue<Integer> linkedBlockingQueue = new LinkedBlockingQueue<>(size);
        long l = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < size; i++) {
                linkedBlockingQueue.offer(i);
                Thread.yield();
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(size);
        Thread t2 = new Thread(() -> {
            for (; ; ) {
                Integer poll = linkedBlockingQueue.poll();
                if (poll != null) {
                    countDownLatch.countDown();
                }
            }
        });
        t2.start();
        t1.start();
        countDownLatch.await();
        System.out.println("linkedBlockingQueue cost: " + (System.currentTimeMillis() - l));
    }

    public static void testRingBuffer() throws InterruptedException {
        RingBuffer<Integer> linkedBlockingQueue = new RingBuffer<>(size);
        long l = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < size; i++) {
                linkedBlockingQueue.offer(i);
                Thread.yield();
            }
        });

        CountDownLatch countDownLatch = new CountDownLatch(size);
        Thread t2 = new Thread(() -> {
            for (; ; ) {
                Integer poll = linkedBlockingQueue.poll();
                if (poll != null) {
                    countDownLatch.countDown();
                }
            }
        });
        t1.start();

        t2.start();
        countDownLatch.await();
        System.out.println("ringBuffer cost: " + (System.currentTimeMillis() - l));
    }


    public static void main(String[] args) throws InterruptedException {
        testLinkedBlockingQueue();
        testRingBuffer();
    }
}
