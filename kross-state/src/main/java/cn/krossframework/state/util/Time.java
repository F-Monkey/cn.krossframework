package cn.krossframework.state.util;

public interface Time {
    default long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }
}
