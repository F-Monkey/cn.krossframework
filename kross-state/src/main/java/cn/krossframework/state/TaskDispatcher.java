package cn.krossframework.state;

public interface TaskDispatcher extends Worker {
    boolean tryAddTask(AbstractTask task);
}
