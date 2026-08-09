package com.taobao.arthas.core.command.model;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

/**
 * 繁忙/阻塞线程的扩展视图：在 {@link ThreadVO} 基础上合并 JMX {@link ThreadInfo} 字段。
 * <p>
 * 用于 thread 命令高 CPU 或阻塞场景，包含锁等待、栈帧、已持锁监视器等诊断数据。
 *
 * @author gongdewei 2020/4/26
 */
public class BusyThreadInfo extends ThreadVO {

    /** 阻塞等待锁的累计毫秒数（-1 表示 JVM 未启用监控） */
    private long         blockedTime;
    /** 阻塞次数 */
    private long         blockedCount;
    /** 等待通知的累计毫秒数 */
    private long         waitedTime;
    /** wait 次数 */
    private long         waitedCount;
    /** 当前等待的锁信息 */
    private LockInfo lockInfo;
    /** 等待锁的名称 */
    private String       lockName;
    /** 锁持有者线程 ID */
    private long         lockOwnerId;
    /** 锁持有者线程名 */
    private String       lockOwnerName;
    /** 是否执行 native 代码 */
    private boolean      inNative;
    /** 是否被调试器挂起 */
    private boolean      suspended;
    /** 调用栈 */
    private StackTraceElement[] stackTrace;
    /** 已持有的内置锁（monitor） */
    private MonitorInfo[]       lockedMonitors;
    /** 已持有的显式锁（如 ReentrantLock） */
    private LockInfo[]          lockedSynchronizers;


    /**
     * 从 ThreadVO（采样 CPU 等）与 ThreadInfo（JMX 阻塞详情）合并构造。
     * threadInfo 为 null 时仅保留 ThreadVO 字段。
     */
    public BusyThreadInfo(ThreadVO thread, ThreadInfo threadInfo) {
        this.setId(thread.getId());
        this.setName(thread.getName());
        this.setDaemon(thread.isDaemon());
        this.setInterrupted(thread.isInterrupted());
        this.setPriority(thread.getPriority());
        this.setGroup(thread.getGroup());
        this.setState(thread.getState());
        this.setCpu(thread.getCpu());
        this.setDeltaTime(thread.getDeltaTime());
        this.setTime(thread.getTime());

        // 补充 JMX ThreadInfo 中的锁与栈信息
        if (threadInfo != null) {
            this.setLockInfo(threadInfo.getLockInfo());
            this.setLockedMonitors(threadInfo.getLockedMonitors());
            this.setLockedSynchronizers(threadInfo.getLockedSynchronizers());
            this.setLockName(threadInfo.getLockName());
            this.setLockOwnerId(threadInfo.getLockOwnerId());
            this.setLockOwnerName(threadInfo.getLockOwnerName());
            this.setStackTrace(threadInfo.getStackTrace());
            this.setBlockedCount(threadInfo.getBlockedCount());
            this.setBlockedTime(threadInfo.getBlockedTime());
            this.setInNative(threadInfo.isInNative());
            this.setSuspended(threadInfo.isSuspended());
            this.setWaitedCount(threadInfo.getWaitedCount());
            this.setWaitedTime(threadInfo.getWaitedTime());
        }

    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public void setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public void setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public void setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }

    public boolean isInNative() {
        return inNative;
    }

    public void setInNative(boolean inNative) {
        this.inNative = inNative;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public MonitorInfo[] getLockedMonitors() {
        return lockedMonitors;
    }

    public void setLockedMonitors(MonitorInfo[] lockedMonitors) {
        this.lockedMonitors = lockedMonitors;
    }

    public LockInfo[] getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    public void setLockedSynchronizers(LockInfo[] lockedSynchronizers) {
        this.lockedSynchronizers = lockedSynchronizers;
    }
}
