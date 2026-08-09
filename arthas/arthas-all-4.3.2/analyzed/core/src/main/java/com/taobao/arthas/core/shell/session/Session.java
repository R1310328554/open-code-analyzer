package com.taobao.arthas.core.shell.session;

import com.taobao.arthas.core.distribution.SharingResultDistributor;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.system.Job;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * Shell 会话上下文接口，以键值对存储 PID、Instrumentation、TTY 等运行时属性。
 * <p>
 * 同时提供乐观锁（{@link #tryLock}）用于命令执行互斥，以及前台 Job 与结果分发器引用。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author gongdewei 2020-03-23
 */
public interface Session {
    /** Session 中存储 {@link InternalCommandManager} 的键 */
    String COMMAND_MANAGER = "arthas-command-manager";
    /** 目标 JVM 进程 PID 的键 */
    String PID = "pid";
    /** Java {@link Instrumentation} 实例的键 */
    String INSTRUMENTATION = "instrumentation";
    /** 会话唯一 id 的键 */
    String ID = "id";
    /** 所属 {@link ShellServer} 的键 */
    String SERVER = "server";
    /** 登录用户标识的键 */
    String USER_ID = "userId";
    /**
     * 会话静默模式，不输出连接欢迎信息。
     */
    String QUIET = "arthas-session-quiet";
    /** 关联 TTY/Term 对象的键 */
    String TTY = "tty";

    /** 会话创建时间戳（毫秒）的键 */
    String CREATE_TIME = "createTime";

    /** 会话最后活跃时间戳的键 */
    String LAST_ACCESS_TIME = "lastAccessedTime";

    /** 命令结果共享分发器的键 */
    String RESULT_DISTRIBUTOR = "resultDistributor";

    /** 当前前台 Job 引用的键 */
    String FOREGROUND_JOB = "foregroundJob";


    /**
     * 向会话写入键值；obj 为 null 时等价于 remove。
     *
     * @param key the key for the data
     * @param obj the data
     * @return a reference to this, so the API can be used fluently
     */
    Session put(String key, Object obj);

    /**
     * 按 key 读取会话属性。
     *
     * @param key the key of the data
     * @return the data
     */
    <T> T get(String key);

    /**
     * 移除并返回指定 key 的值。
     *
     * @param key the key of the data
     * @return the data that was there or null if none there
     */
    <T> T remove(String key);

    /** @return 会话是否已被某命令持有锁 */
    boolean isLocked();

    /** 释放会话锁；非持有者调用将抛异常 */
    void unLock();

    /** 尝试获取会话锁，成功返回 true（CAS 语义） */
    boolean tryLock();

    /** @return 当前锁序号，-1 表示未锁定 */
    int getLock();

    /** @return 会话唯一标识 */
    String getSessionId();

    /** @return 附加的目标 JVM 进程号 */
    long getPid();

    /** @return 当前可用的命令解析器列表 */
    List<CommandResolver> getCommandResolvers();

    /** @return Java Instrumentation，用于类增强与诊断 */
    Instrumentation getInstrumentation();

    /** 更新最后活跃时间，供超时回收判断 */
    void setLastAccessTime(long time);

    /** @return 最后活跃时间戳（毫秒） */
    long getLastAccessTime();

    /** @return 会话创建时间戳（毫秒） */
    long getCreateTime();

    /** 设置命令结果共享分发器（Web 多消费者场景） */
    void setResultDistributor(SharingResultDistributor resultDistributor);

    /** @return 结果分发器，未设置时为 null */
    SharingResultDistributor getResultDistributor();

    /** 记录当前前台 Job 引用 */
    void setForegroundJob(Job job);

    /** @return 前台 Job，空闲时为 null */
    Job getForegroundJob();

    /** @return 是否绑定真实 TTY 终端（相对纯 API 会话） */
    boolean isTty();

    /** @return 当前登录用户 id */
    String getUserId();

    /** 设置登录用户 id */
    void setUserId(String userId);
}
