package com.taobao.arthas.core.command.monitor200;

import java.time.LocalDateTime;

/**
 * {@code monitor} 命令单个「类+方法」在一个统计周期内的聚合指标。
 * 由 {@link MonitorAdviceListener} 无锁累加，定时任务输出前填充 className/methodName。
 *
 * @author vlinux
 */
public class MonitorData {
    /** 被监控类的全限定名 */
    private String className;
    /** 被监控方法名 */
    private String methodName;
    /** 周期内调用总次数 */
    private int total;
    /** 正常返回次数 */
    private int success;
    /** 抛异常次数 */
    private int failed;
    /** 累计耗时（毫秒），用于计算平均 RT */
    private double cost;
    /** 最近一次更新时的本地时间戳；getter 在 null 时懒填当前时间 */
    private LocalDateTime timestamp;

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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public LocalDateTime getTimestamp() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
