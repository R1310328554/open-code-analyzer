package com.taobao.arthas.core.command.model;

import java.time.LocalDateTime;

/**
 * trace 调用树的根节点：绑定被追踪线程的元数据，类型固定为 {@code "thread"}。
 * <p>
 * 作为 {@link TraceTree} 的 root，其 {@link #children} 挂载 {@link MethodNode} 等方法调用子树；
 * 构造时默认 {@link #timestamp} 为当前时刻，便于与后续方法节点对齐时序。
 *
 * @author gongdewei 2020/4/29
 */
public class ThreadNode extends TraceNode {

    /** 被追踪线程名称 */
    private String threadName;
    /** 被追踪线程 id（JVM 原生 long id） */
    private long threadId;
    /** 是否为守护线程 */
    private boolean daemon;
    /** 线程优先级 */
    private int priority;
    /** 线程上下文 ClassLoader 描述 */
    private String classloader;
    /** 追踪开始或根节点创建时间戳 */
    private LocalDateTime timestamp;

    /** 可选：分布式 traceId */
    private String traceId;
    /** 可选：RPC 子跨度 id */
    private String rpcId;

    public ThreadNode() {
        super("thread");
        timestamp = LocalDateTime.now();
    }

    public ThreadNode(String threadName, long threadId, boolean daemon, int priority, String classloader) {
        super("thread");
        this.threadName = threadName;
        this.threadId = threadId;
        this.daemon = daemon;
        this.priority = priority;
        this.classloader = classloader;
        timestamp = LocalDateTime.now();
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getClassloader() {
        return classloader;
    }

    public void setClassloader(String classloader) {
        this.classloader = classloader;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRpcId() {
        return rpcId;
    }

    public void setRpcId(String rpcId) {
        this.rpcId = rpcId;
    }
}
