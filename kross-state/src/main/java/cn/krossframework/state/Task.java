package cn.krossframework.state;

import javax.annotation.Nullable;

public interface Task {
    @Nullable
    FailCallBack getFailCallBack();
}
