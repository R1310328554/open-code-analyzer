package com.taobao.arthas.core.shell.term.impl.http.api;

/**
 * HTTP API 响应状态枚举，写入 {@link ApiResponse#getState()}。
 *
 * @author gongdewei 2020-03-19
 */
public enum ApiState {
    /**
     * 异步命令已调度，Job 已创建但结果尚未就绪
     */
    SCHEDULED,

//    RUNNING,

    /**
     * 请求处理成功
     */
    SUCCEEDED,

    /**
     * 请求被中断（如同步 EXEC 超时强制 interrupt）
     */
    INTERRUPTED,

    /**
     * 请求处理失败
     */
    FAILED,

    /**
     * 请求被拒绝（如不支持的 HTTP 方法或 action）
     */
    REFUSED
}
