package cn.krossframework.chat.model;

import java.io.Serializable;
import java.util.Date;

public class BaseModel<T extends Serializable> implements Serializable {
    private T id;
    private Date createTime;
    private Date updateTime;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
