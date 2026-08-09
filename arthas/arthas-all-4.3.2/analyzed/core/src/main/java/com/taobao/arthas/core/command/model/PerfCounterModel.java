package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * perfcounter 命令的结构化结果：JVM 内部性能计数器（PerfCounter）快照。
 * <p>
 * 数据来源于 HotSpot 的 perf 数据；{@link #details} 为 true 时各条目包含
 * units、variability 等元数据，false 时仅返回名称与数值，便于快速浏览。
 *
 * @author gongdewei 2020/4/27
 */
public class PerfCounterModel extends ResultModel {
    /** 计数器条目列表，顺序与 JVM 导出一致 */
    private List<PerfCounterVO> perfCounters;
    /** 是否以详细模式返回（含单位、变异性等字段） */
    private boolean details;

    public PerfCounterModel() {
    }

    public PerfCounterModel(List<PerfCounterVO> perfCounters, boolean details) {
        this.perfCounters = perfCounters;
        this.details = details;
    }

    @Override
    public String getType() {
        return "perfcounter";
    }

    public List<PerfCounterVO> getPerfCounters() {
        return perfCounters;
    }

    public void setPerfCounters(List<PerfCounterVO> perfCounters) {
        this.perfCounters = perfCounters;
    }

    public boolean isDetails() {
        return details;
    }

    public void setDetails(boolean details) {
        this.details = details;
    }
}
