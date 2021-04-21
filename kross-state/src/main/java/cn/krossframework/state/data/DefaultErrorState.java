package cn.krossframework.state.data;

/**
 * Default error state
 */
public class DefaultErrorState extends AbstractState {

    public static final String CODE = "ERROR";

    @Override
    public String getCode() {
        return CODE;
    }
}
