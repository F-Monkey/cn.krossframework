package cn.krossframework.state.util;

public interface Lock {

    boolean tryLock();

    void unlock();
}
