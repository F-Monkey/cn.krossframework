package cn.krossframework.state;

public interface Time {
    default long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }
}
