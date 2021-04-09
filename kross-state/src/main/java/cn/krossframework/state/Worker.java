package cn.krossframework.state;

public interface Worker {

    long getId();

    void start();

    boolean isStart();

    default boolean isFull() {
        return false;
    }

    default boolean isEmpty() {
        return false;
    }

    void update();

    void stop();

}
