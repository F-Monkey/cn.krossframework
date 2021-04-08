package cn.krossframework.commons.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class RingBuffer<T> implements Queue<T> {

    private static final int DEFAULT_SIZE = 1 << 10;
    private final AtomicReferenceArray<T> buffer;
    private final AtomicInteger head;
    private final AtomicInteger tail;

    public RingBuffer() {
        this(0);
    }

    public RingBuffer(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("invalid size");
        }
        if (size == 0) {
            size = DEFAULT_SIZE;
        }
        this.buffer = new AtomicReferenceArray<>(size);
        this.head = new AtomicInteger(0);
        this.tail = new AtomicInteger(0);
    }

    private boolean isFull() {
        return (this.tail.get() + 1) % this.buffer.length() == this.head.get() % this.buffer.length();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return this.head.get() == this.tail.get();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        if (!this.offer(t)) {
            throw new IllegalStateException("queue is full");
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(T t) {
        if (this.isFull()) {
            return false;
        }
        int index = (this.tail.get() + 1) % this.buffer.length();
        if (!this.buffer.compareAndSet(index, null, t)) {
            return offer(t);
        }
        this.tail.incrementAndGet();
        return true;
    }

    @Override
    public T remove() {
        throw new IllegalStateException("queue is full");
    }

    @Override
    @SuppressWarnings("unchecked")
    public T poll() {
        if (this.isEmpty()) {
            return null;
        }
        int index = (this.head.get() + 1) % this.buffer.length();
        T t = this.buffer.get(index);
        if (t == null) {
            return this.poll();
        }
        if (!buffer.compareAndSet(index, t, null)) {
            return this.poll();
        }
        this.head.incrementAndGet();
        return t;
    }

    @Override
    public T element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T peek() {
        throw new UnsupportedOperationException();
    }
}
