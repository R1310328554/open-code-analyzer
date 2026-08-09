package com.taobao.arthas.core.command.model;

import java.lang.Thread.State;

/**
 * 线程摘要视图：供 dashboard 与 thread 命令展示单行线程指标。
 * <p>
 * {@link #cpu} / {@link #deltaTime} / {@link #time} 来自采样间隔内的 CPU 占用估算；
 * {@link #equals} / {@link #hashCode} 仅基于 id 与 name，便于在集合中去重。
 *
 * @author gongdewei 2020/4/22
 */
public class ThreadVO {
    /** 线程 id */
    private long id;
    /** 线程名称 */
    private String name;
    /** 线程组名 */
    private String group;
    /** 优先级 */
    private int priority;
    /** 当前线程状态 */
    private State state;
    /** 采样窗口内 CPU 使用率（百分比或归一化值，取决于采集实现） */
    private double cpu;
    /** 与上一采样相比的 CPU 时间增量（纳秒或毫秒，取决于实现） */
    private long deltaTime;
    /** 累计 CPU 时间 */
    private long time;
    /** 中断标志是否已设置 */
    private boolean interrupted;
    /** 是否为守护线程 */
    private boolean daemon;

    public ThreadVO() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(long deltaTime) {
        this.deltaTime = deltaTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    /** 同一 id 且 name 相等则视为同一线程 VO */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadVO threadVO = (ThreadVO) o;

        if (id != threadVO.id) return false;
        return name != null ? name.equals(threadVO.name) : threadVO.name == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
