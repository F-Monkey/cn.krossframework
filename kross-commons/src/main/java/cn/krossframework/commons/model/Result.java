package cn.krossframework.commons.model;

import lombok.Getter;

@Getter
public final class Result<T> {
    private int code;
    private String msg;
    private T data;
    private Throwable e;

    public static <T> Builder<T> newBuilder() {
        return new Builder<>();
    }

    public static <T> Result<T> ok(T data) {
        return Result.<T>newBuilder().code(ResultCode.SUCCESS).data(data).build();
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String msg) {
        return Result.<T>newBuilder().code(ResultCode.FAIL).msg(msg).build();
    }

    public static <T> Result<T> error(Throwable e) {
        return Result.<T>newBuilder().code(ResultCode.ERROR).e(e).build();
    }

    public static class Builder<T> {
        private final Result<T> result;

        private Builder() {
            this.result = new Result<>();
        }

        public Builder<T> code(int code) {
            this.result.code = code;
            return this;
        }

        public Builder<T> msg(String msg) {
            this.result.msg = msg;
            return this;
        }

        public Builder<T> data(T data) {
            this.result.data = data;
            return this;
        }

        public Builder<T> e(Throwable e) {
            this.result.e = e;
            return this;
        }

        public Result<T> build() {
            return this.result;
        }
    }
}
