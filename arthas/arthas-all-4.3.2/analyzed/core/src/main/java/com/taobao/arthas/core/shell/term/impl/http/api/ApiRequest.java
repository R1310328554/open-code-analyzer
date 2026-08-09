package com.taobao.arthas.core.shell.term.impl.http.api;

/**
 * HTTP API 请求体 DTO，对应 POST {@code /api} 的 JSON 字段。
 * <p>
 * {@link #action} 映射 {@link ApiAction}；{@link #sessionId} 与
 * {@link #consumerId} 用于多客户端结果分发与超时控制。
 *
 * @author gongdewei 2020-03-19
 */
public class ApiRequest {
    /** API 动作名，对应 {@link ApiAction} 枚举值 */
    /** 待执行的 Arthas 命令行（EXEC/ASYNC_EXEC 时使用） */
    /** 客户端请求 ID，用于关联响应 */
    /** 目标 Shell Session ID */
    /** 结果消费者 ID，PULL_RESULTS 时区分客户端 */
    /** 命令执行超时（秒），可选 */
    /** 用户标识，多租户或审计场景使用 */

    @Override
    public String toString() {
        return "ApiRequest{" +
                "action='" + action + '\'' +
                ", command='" + command + '\'' +
                ", requestId='" + requestId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", consumerId='" + consumerId + '\'' +
                ", execTimeout=" + execTimeout +
                ", userId='" + userId + '\'' +
                '}';
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public Integer getExecTimeout() {
        return execTimeout;
    }

    public void setExecTimeout(Integer execTimeout) {
        this.execTimeout = execTimeout;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
