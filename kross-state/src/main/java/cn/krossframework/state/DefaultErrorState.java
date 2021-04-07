package cn.krossframework.state;

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
