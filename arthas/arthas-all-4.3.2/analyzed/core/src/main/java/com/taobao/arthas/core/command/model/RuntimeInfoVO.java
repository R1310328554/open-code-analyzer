package com.taobao.arthas.core.command.model;

/**
 * Dashboard 运行时信息值对象：操作系统与 JVM 环境快照。
 * <p>
 * 由 dashboard 命令 RUNTIME 面板填充；{@link #systemLoadAverage} 在部分平台
 * 不可用时可能为负值；{@link #uptime} 为 JVM 启动至今毫秒数，
 * {@link #timestamp} 为采样时刻便于客户端展示相对时间。
 *
 * @author gongdewei 2020/4/22
 */
public class RuntimeInfoVO {
    /** 操作系统名称，如 Linux、Mac OS X */
    private String osName;
    /** 操作系统版本字符串 */
    private String osVersion;
    /** Java 运行时版本，如 1.8.0_292 */
    private String javaVersion;
    /** JAVA_HOME 路径 */
    private String javaHome;
    /** 系统负载均值（1 分钟）；不可用时常为 -1 */
    private double systemLoadAverage;
    /** 可用处理器（逻辑核心）数量 */
    private int processors;
    /** JVM 已运行时间（毫秒） */
    private long uptime;
    /** 信息采集时的 Unix 时间戳（毫秒） */
    private long timestamp;

    public RuntimeInfoVO() {
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public int getProcessors() {
        return processors;
    }

    public void setProcessors(int processors) {
        this.processors = processors;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
