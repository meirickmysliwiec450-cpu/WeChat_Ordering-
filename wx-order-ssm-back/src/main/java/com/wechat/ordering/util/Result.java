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

    public static Result<?> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    public static Result<?> error(String message) {
        return new Result<>(500, message);
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
