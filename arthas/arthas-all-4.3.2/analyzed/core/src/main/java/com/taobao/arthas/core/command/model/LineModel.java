package com.taobao.arthas.core.command.model;

import java.time.LocalDateTime;

/**
 * line 命令命中观测点时的单次事件结果模型。
 * <p>
 * 当目标方法执行到指定行号时，Agent 采集线程上下文、局部变量（{@link ObjectVO}）、
 * 耗时及调用栈，序列化为本模型推送给客户端；{@link #sizeLimit} 可限制变量展开深度。
 */
public class LineModel extends ResultModel {
    /** 事件触发时间戳 */
    private LocalDateTime ts;
    /** 从进入方法到命中行号的耗时（毫秒） */
    private double cost;
    /** 该行可观测的局部变量/表达式求值结果 */
    private ObjectVO value;
    /** 变量序列化深度上限，null 表示使用默认策略 */
    private Integer sizeLimit;
    /** 被观测类全限定名 */
    private String className;
    /** 被观测方法名 */
    private String methodName;
    /** 方法 JVM 描述符 */
    private String methodDesc;
    /** 命中的源码行号 */
    private int lineNumber;
    /** 执行线程名称 */
    private String threadName;
    /** 执行线程 ID */
    private long threadId;
    /** 命中时刻的线程栈（便于定位调用链） */
    private StackTraceElement[] stackTrace;

    @Override
    public String getType() {
        return "line";
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

    public ObjectVO getValue() {
        return value;
    }

    public void setValue(ObjectVO value) {
        this.value = value;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
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

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
}
