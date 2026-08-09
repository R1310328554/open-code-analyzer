package com.taobao.arthas.core.command.model;

import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.Map;

/**
 * thread 命令的结构化结果：按子命令模式承载不同粒度的线程诊断数据。
 * <p>
 * 同一 Model 通过互斥字段表达多种输出形态——单线程详情、死锁检测（-b）、
 * CPU 最忙线程（-n）、以及全量/过滤统计列表；客户端根据非空字段选择渲染模板。
 *
 * @author gongdewei 2020/4/26
 */
public class ThreadModel extends ResultModel {

    /** 单线程模式：如 {@code thread 12}，含栈帧与锁信息 */
    private ThreadInfo threadInfo;

    /** {@code thread -b}：阻塞/死锁相关锁与持有线程摘要 */
    private BlockingLockInfo blockingLockInfo;

    /** {@code thread -n N}：按 CPU 使用率排序的最忙 N 个线程 */
    private List<BusyThreadInfo> busyThreads;

    /** 线程列表统计：dashboard / thread 无 id 时的概览行 */
    private List<ThreadVO> threadStats;
    /** 各 {@link Thread.State} 的线程数量汇总 */
    private Map<Thread.State, Integer> threadStateCount;
    /** 是否包含全部线程（false 时可能只展示活跃或采样子集） */
    private boolean all;

    public ThreadModel() {
    }

    public ThreadModel(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    public ThreadModel(BlockingLockInfo blockingLockInfo) {
        this.blockingLockInfo = blockingLockInfo;
    }

    public ThreadModel(List<BusyThreadInfo> busyThreads) {
        this.busyThreads = busyThreads;
    }

    public ThreadModel(List<ThreadVO> threadStats, Map<Thread.State, Integer> threadStateCount, boolean all) {
        this.threadStats = threadStats;
        this.threadStateCount = threadStateCount;
        this.all = all;
    }

    @Override
    public String getType() {
        return "thread";
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    public BlockingLockInfo getBlockingLockInfo() {
        return blockingLockInfo;
    }

    public void setBlockingLockInfo(BlockingLockInfo blockingLockInfo) {
        this.blockingLockInfo = blockingLockInfo;
    }

    public List<BusyThreadInfo> getBusyThreads() {
        return busyThreads;
    }

    public void setBusyThreads(List<BusyThreadInfo> busyThreads) {
        this.busyThreads = busyThreads;
    }

    public List<ThreadVO> getThreadStats() {
        return threadStats;
    }

    public void setThreadStats(List<ThreadVO> threadStats) {
        this.threadStats = threadStats;
    }

    public Map<Thread.State, Integer> getThreadStateCount() {
        return threadStateCount;
    }

    public void setThreadStateCount(Map<Thread.State, Integer> threadStateCount) {
        this.threadStateCount = threadStateCount;
    }

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }
}
