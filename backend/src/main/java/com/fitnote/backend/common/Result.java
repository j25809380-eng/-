package com.fitnote.backend.common;

/**
 * 统一 API 返回封装
 * 所有 Controller 必须返回 Result<T> 以确保前端拿到一致的 JSON 结构
 */
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ========== 成功 ==========

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    // ========== 错误 ==========

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // ========== 常用快捷方法 ==========

    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(400, msg, null);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(401, msg, null);
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }

    public static <T> Result<T> notFound(String msg) {
        return new Result<>(404, msg, null);
    }

    public static <T> Result<T> serverError(String msg) {
        return new Result<>(500, msg, null);
    }

    // ========== getters ==========

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
