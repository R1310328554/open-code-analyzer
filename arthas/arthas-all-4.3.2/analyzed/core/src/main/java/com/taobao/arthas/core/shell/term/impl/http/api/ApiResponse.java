package com.taobao.arthas.core.shell.term.impl.http.api;

/**
 * HTTP API 统一响应体，序列化为 POST {@code /api} 的 JSON 返回值。
 * <p>
 * 链式 setter 便于构建；{@link #state} 表示处理结果，
 * {@link #body} 承载命令输出或会话元数据等业务载荷。
 *
 * @author gongdewei 2020-03-19
 */
public class ApiResponse<T> {
    /** 与 {@link ApiRequest#getRequestId()} 对应的请求 ID */
    private String requestId;
    /** 处理状态，见 {@link ApiState} */
    private ApiState state;
    /** 错误或提示信息 */
    private String message;
    /** 关联的 Arthas Shell Session ID */
    private String sessionId;
    /** 结果消费者 ID，多客户端拉取结果时使用 */
    private String consumerId;
    /** 异步/同步执行关联的 Job ID */
    private String jobId;
    /** 业务数据体（命令结果、会话信息等） */
    private T body;

    public String getRequestId() {
        return requestId;
    }

    public ApiResponse<T> setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ApiState getState() {
        return state;
    }

    public ApiResponse<T> setState(ApiState state) {
        this.state = state;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ApiResponse<T> setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public ApiResponse<T> setConsumerId(String consumerId) {
        this.consumerId = consumerId;
        return this;
    }

    public String getJobId() {
        return jobId;
    }

    public ApiResponse<T> setJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    public T getBody() {
        return body;
    }

    public ApiResponse<T> setBody(T body) {
        this.body = body;
        return this;
    }

}
