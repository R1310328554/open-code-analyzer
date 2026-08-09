package com.taobao.arthas.core.command.model;

import java.lang.management.ThreadInfo;

/**
 * 线程阻塞锁信息：描述某把锁的持有者及等待该锁的线程数量。
 * <p>
 * 从 {@link com.taobao.arthas.core.util.ThreadUtil} 提取，供 thread -b 等
 * 阻塞分析命令组装 {@link BusyThreadInfo} 时使用。
 *
 * @author gongdewei 2020/7/14
 */
public class BlockingLockInfo {

    /** 当前持有该锁的线程信息 */
    private ThreadInfo threadInfo = null;
    /** 锁对象的 identityHashCode，用于关联 MonitorInfo */
    private int lockIdentityHashCode = 0;
    /** 阻塞等待该锁的线程数量 */
    private int blockingThreadCount = 0;

    public BlockingLockInfo() {
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    public int getLockIdentityHashCode() {
        return lockIdentityHashCode;
    }

    public void setLockIdentityHashCode(int lockIdentityHashCode) {
        this.lockIdentityHashCode = lockIdentityHashCode;
    }

    public int getBlockingThreadCount() {
        return blockingThreadCount;
    }

    public void setBlockingThreadCount(int blockingThreadCount) {
        this.blockingThreadCount = blockingThreadCount;
    }
}
