package com.taobao.arthas.core.command.model;

import java.time.LocalDateTime;

/**
 * stack 命令的结构化结果：在指定时刻捕获某线程的调用栈及相关上下文。
 * <p>
 * 与 {@link TraceModel} 不同，stack 只输出单次快照，不构建调用树；
 * {@link #traceId} / {@link #rpcId} 用于与分布式追踪上下文对齐（若存在）。
 *
 * @author gongdewei 2020/4/13
 */
public class StackModel extends ResultModel {

    /** 采样时刻（Agent 侧本地时间） */
    private LocalDateTime ts;
    /** 获取栈帧的耗时（毫秒），用于诊断 stack 命令自身开销 */
    private double cost;
    /** 分布式 traceId，无追踪上下文时可能为空 */
    private String traceId;
    /** RPC 子跨度标识，与 traceId 配套使用 */
    private String rpcId;
    /** 目标线程名称 */
    private String threadName;
    /** 目标线程 id（字符串形式，兼容 JSON 序列化） */
    private String threadId;
    /** 是否为守护线程 */
    private boolean daemon;
    /** 线程优先级 */
    private int priority;
    /** 线程上下文 ClassLoader 的描述（hash 或类名摘要） */
    private String classloader;
    /** 自栈顶向下的调用栈元素数组 */
    private StackTraceElement[] stackTrace;

    @Override
    public String getType() {
        return "stack";
    }

    public LocalDateTime getTs() {
        return ts;
    }

    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
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

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
