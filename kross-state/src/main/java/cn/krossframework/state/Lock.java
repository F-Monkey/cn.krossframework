package cn.krossframework.state;

public interface Lock {

    boolean tryLock();

    void unlock();
}
