package cn.krossframwork.commons;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
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
		linkedBlockingQueue.poll();
		t2.start();
		t1.start();
		countDownLatch.await();
		System.out.println("linkedBlockingQueue cost: " + (System.currentTimeMillis() - l));
	}

	public static void testConcurrentLinkedQueue() throws InterruptedException {
		ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
		long l = System.currentTimeMillis();
		Thread t1 = new Thread(() -> {
			for (int i = 0; i < size; i++) {
				concurrentLinkedQueue.offer(i);
				Thread.yield();
			}
		});

		CountDownLatch countDownLatch = new CountDownLatch(size);
		Thread t2 = new Thread(() -> {
			for (; ; ) {
				Integer poll = concurrentLinkedQueue.poll();
				if (poll != null) {
					countDownLatch.countDown();
				}
			}
		});
		t2.start();
		t1.start();
		countDownLatch.await();
		System.out.println("concurrentLinkedQueue cost: " + (System.currentTimeMillis() - l));
	}

	public static void testLinkedList() throws InterruptedException {
		LinkedList<Integer> linkedList = new LinkedList<>();
		long l = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			linkedList.offer(i);
		}
		CountDownLatch countDownLatch = new CountDownLatch(size);
		Thread t2 = new Thread(() -> {
			for (; ; ) {
				Integer poll = linkedList.poll();
				if (poll != null) {
					countDownLatch.countDown();
				}
			}
		});
		t2.start();
		countDownLatch.await();
		System.out.println("linkedList cost: " + (System.currentTimeMillis() - l));
	}

	public static void main(String[] args) throws InterruptedException {
		testLinkedBlockingQueue();
		testConcurrentLinkedQueue();
		testLinkedList();
	}
}
