package com.taobao.arthas.core.shell.session;

import com.taobao.arthas.core.shell.system.JobController;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;

import java.lang.instrument.Instrumentation;

/**
 * Arthas 会话管理器接口，面向 Web/API 等非 TTY 连接场景。
 * <p>
 * 负责 Session 的创建、查询、移除与空闲回收，并暴露命令管理器与 Job 控制器。
 *
 * @author gongdewei 2020-03-20
 */
public interface SessionManager {

    /** 创建新会话并注入 PID、Instrumentation 等公共属性 */
    Session createSession();

    /** @param sessionId 会话 id；不存在时返回 null */
    Session getSession(String sessionId);

    /** 移除会话：中断前台 Job 并关闭结果分发器 */
    Session removeSession(String sessionId);

    /** 刷新会话最后活跃时间，防止被回收 */
    void updateAccessTime(Session session);

    /** 关闭管理器：停止定时器并清理全部会话与 Job */
    void close();

    /** @return 全局命令管理器 */
    InternalCommandManager getCommandManager();

    /** @return JVM Instrumentation 实例 */
    Instrumentation getInstrumentation();

    /** @return 关联的 Job 控制器 */
    JobController getJobController();
}
