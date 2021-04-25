package cn.krossframework.state;

import cn.krossframework.state.config.StateGroupConfig;
import cn.krossframework.state.data.StateData;
import cn.krossframework.state.data.Task;

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
     * @return stateGroupConfig
     */
    <T extends StateGroupConfig> T getConfig();

    /**
     * @param <T> stateData type
     * @return stateData
     */
    <T extends StateData> T getStateData();

    /**
     * set workerId
     */
    void setCurrentWorkerId(long id);

    /**
     * set current state,and init state info
     *
     * @param code state code
     * @throws NullPointerException when state is not exists
     */
    void initAndSetCurrentState(String code) throws NullPointerException;

    /**
     * try enter stateGroup, if stateGroup is full or state can be deposed, return false
     *
     * @param task enterGroup task
     * @return enterResult
     */
    boolean tryEnterGroup(Task task);

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
