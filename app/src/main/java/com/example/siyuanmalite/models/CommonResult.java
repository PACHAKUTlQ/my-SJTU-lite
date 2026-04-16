package com.example.siyuanmalite.models;

public class CommonResult<T> {
    public T result;
    public Boolean success;
    public String message;

    public CommonResult(Boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }

    public CommonResult(Boolean success, String message, T result)
    {
        this.success = success;
        this.message = message;
        this.result = result;
    }
}
