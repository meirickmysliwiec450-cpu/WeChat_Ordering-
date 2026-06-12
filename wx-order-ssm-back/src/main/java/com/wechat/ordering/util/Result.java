package com.wechat.ordering.util;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应结果格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "成功", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "成功");
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(Integer code, String message) {
        return (Result<T>) new Result<>(code, message);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(String message) {
        return (Result<T>) new Result<>(500, message);
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
