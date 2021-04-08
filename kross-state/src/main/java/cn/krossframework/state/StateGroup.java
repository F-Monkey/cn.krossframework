package cn.krossframework.state;

public interface StateGroup {

    /**
     * id
     *
     * @return stateGroup id
     */
    long getId();

    /**
     * get workerId
     *
     * @return workerId
     */
    Long getCurrentWorkerId();

    /**
     * set workerId
     */
    void setCurrentWorkerId(long id);

    /**
     * add state
     *
     * @param state state
     */
    void addState(State state);

    /**
     * set current state,and init state info
     *
     * @param code state code
     * @throws NullPointerException when state is not exists
     */
    void initAndSetCurrentState(String code) throws NullPointerException;

    /**
     * try add task
     *
     * @param task task
     * @return if task add fail, return false
     */
    boolean tryAddTask(Task task);

    /**
     * update stateGroup info
     */
    void update();

    /**
     * stateGroup can be deposed
     *
     * @return true if this stateGroup can be deposed
     */
    boolean canDeposed();
}
