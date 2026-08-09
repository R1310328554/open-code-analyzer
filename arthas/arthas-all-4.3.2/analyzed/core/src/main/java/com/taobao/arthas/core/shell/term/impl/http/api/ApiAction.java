package com.taobao.arthas.core.shell.term.impl.http.api;

/**
 * HTTP API {@code action} 字段枚举，标识客户端请求类型。
 * <p>
 * 由 {@link ApiRequest#getAction()} 解析，{@code HttpApiHandler} 按值分发到
 * 同步/异步执行、会话管理与结果拉取等逻辑。
 *
 * @author gongdewei 2020-03-25
 */
public enum ApiAction {
    /** 同步执行命令并等待结果 */
    EXEC,

    /** 异步提交命令，结果通过 PULL_RESULTS 获取 */
    ASYNC_EXEC,

    /** 中断当前 Session 中正在执行的 Job */
    INTERRUPT_JOB,

    /** 从 Session 结果队列拉取异步输出 */
    PULL_RESULTS,

    /** 创建新的 Arthas Shell Session */
    INIT_SESSION,

    /** 加入已有 Session（多客户端共享） */
    JOIN_SESSION,

    /** 关闭并销毁 Session */
    CLOSE_SESSION,

    /** 查询 Session 状态与元信息 */
    SESSION_INFO
}
