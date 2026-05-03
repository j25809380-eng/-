package com.fitnote.backend.common;

/**
 * @deprecated 请使用 Result<T> 替代。
 * 保留此类以兼容未迁移的老代码，内部委托给 Result。
 */
@Deprecated
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, "success", null);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    // ========== getters（兼容 Spring 序列化） ==========
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
