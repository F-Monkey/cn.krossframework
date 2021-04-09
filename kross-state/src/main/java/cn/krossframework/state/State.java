package cn.krossframework.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface State {

    Logger log = LoggerFactory.getLogger(State.class);

    /**
     * set stateData that initialized from {@link StateGroup}
     *
     * @param stateData stateData
     */
    void setStateData(StateData stateData);

    /**
     * get StateData
     *
     * @param <T> stateData type
     * @return stateData
     */
    <T extends StateData> T getStateData();

    /**
     * return current state code, The code for each set of states
     * witch in the same {@link StateGroup} cannot be repeated
     *
     * @return state code
     */
    String getCode();

    /**
     * init state
     * it is depends on the state data should be reset if the function is called
     * when the state is switched from another one
     *
     * @param time time of being called should be recorded
     */
    void init(Time time);

    /**
     * record error message when {@link #init(Time)} execute error
     *
     * @param time  exception happened time
     * @param error error msg
     */
    default void initOnError(Time time, Throwable error) {
        log.error("{} init error on time:{}, error:\n", this.getCode(), time.getCurrentTimeMillis(), error);
    }

    /**
     * update state data by cmd
     *
     * @param time execute time
     * @param task task
     */
    void handleTask(Time time, Task task);

    /**
     * record error message when {@link #handleTask(Time, Task)} execute error
     *
     * @param time  execute time
     * @param task  task
     * @param error error message
     */
    default void handleTaskOnError(Time time, Task task, Throwable error) {
        log.error("{} handleCmd error on time:{}, task:\n{} error:\n", this.getCode(), time.getCurrentTimeMillis(), task, error);
    }

    /**
     * update state data after {@link #handleTask(Time, Task)}
     * if {@link StateInfo#isFinished} equals true, then execute {@link #finish(Time)}
     * and switch to next state
     *
     * @param time      execute time
     * @param stateInfo if current state should be finished, then update {@link StateInfo#isFinished} to true。
     */
    void update(Time time, StateInfo stateInfo);

    /**
     * record error message when {@link #update(Time, StateInfo)} execute error
     *
     * @param time      execute time
     * @param stateInfo stateInfo
     * @param error     error message
     */
    default void updateOnError(Time time, StateInfo stateInfo, Throwable error) {
        log.error("{} update error on time:{}, stateInfo:\n{} error:\n", this.getCode(), time.getCurrentTimeMillis(), stateInfo, error);
    }

    /**
     * finish current state and return next state code
     *
     * @param time state finish time
     * @return next state code
     */
    String finish(Time time);

    /**
     * record {@link #finish(Time)} error message
     *
     * @param time  execute time
     * @param error error message
     * @return error state code
     */
    default String finishOnError(Time time, Throwable error) {
        log.error("{} finish error on time:{}, error:\n", this.getCode(), time.getCurrentTimeMillis(), error);
        return DefaultErrorState.CODE;
    }
}
