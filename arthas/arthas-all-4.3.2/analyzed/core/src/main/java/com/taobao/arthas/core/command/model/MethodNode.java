package com.taobao.arthas.core.command.model;

/**
 * trace 命令调用树中的方法节点：记录单次/多次方法调用的耗时与异常信息。
 * <p>
 * 继承 {@link TraceNode}，节点类型固定为 "method"；
 * {@link #end} 会累加 min/max/total 耗时，支持对相同调用路径做合并统计。
 *
 * @author gongdewei 2020/4/29
 */
public class MethodNode extends TraceNode {

    /** 被追踪方法的声明类全限定名 */
    private String className;
    /** 方法名 */
    private String methodName;
    /** 调用发生处的源码行号（无行号表时为 -1） */
    private int lineNumber;
    /** 该方法是否抛出异常 */
    private Boolean isThrow;
    /** 异常类型简名或消息摘要 */
    private String throwExp;

    /**
     * 是否为 invoke 方法，true 为 beforeInvoke，false 为方法体入口的 onBefore
     */
    private boolean isInvoking;

    /**
     * 开始时间戳
     */
    private long beginTimestamp;

    /**
     * 结束时间戳
     */
    private long endTimestamp;

    /**
     * 合并统计相同调用,并计算最小\最大\总耗时
     */
    private long minCost = Long.MAX_VALUE;
    private long maxCost = Long.MIN_VALUE;
    private long totalCost = 0;
    /** 该节点被命中的次数（多次 trace 合并时递增） */
    private long times = 0;


    public MethodNode(String className, String methodName, int lineNumber, boolean isInvoking) {
        super("method");
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.isInvoking = isInvoking;
    }

    /** 记录方法进入时刻（纳秒，System.nanoTime） */
    public void begin() {
        beginTimestamp = System.nanoTime();
    }

    /**
     * 记录方法退出并更新耗时统计。
     * 首次调用前若未 begin，getCost 可能产生异常大值；trace 框架保证成对调用。
     */
    public void end() {
        endTimestamp = System.nanoTime();

        long cost = getCost();
        if (cost < minCost) {
            minCost = cost;
        }
        if (cost > maxCost) {
            maxCost = cost;
        }
        times++;
        totalCost += cost;
    }

    /** 单次调用的纳秒耗时（endTimestamp - beginTimestamp） */
    public long getCost() {
        return endTimestamp - beginTimestamp;
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

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Boolean getThrow() {
        return isThrow;
    }

    public void setThrow(Boolean aThrow) {
        isThrow = aThrow;
    }

    public String getThrowExp() {
        return throwExp;
    }

    public void setThrowExp(String throwExp) {
        this.throwExp = throwExp;
    }

    public long getMinCost() {
        return minCost;
    }

    public void setMinCost(long minCost) {
        this.minCost = minCost;
    }

    public long getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(long maxCost) {
        this.maxCost = maxCost;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public boolean isInvoking() {
        return isInvoking;
    }

    public void setInvoking(boolean invoking) {
        isInvoking = invoking;
    }
}
