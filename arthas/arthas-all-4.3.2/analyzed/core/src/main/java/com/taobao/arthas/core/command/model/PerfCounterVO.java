package com.taobao.arthas.core.command.model;


/**
 * 单条 JVM PerfCounter 的值对象，供 {@link PerfCounterModel} 聚合展示。
 * <p>
 * {@link #value} 类型随计数器而定（Long、Double 等）；{@link #variability}
 * 描述采样变异性（如 CONSTANT、VARIABLE），仅在详细模式下填充。
 *
 * @author gongdewei 2020/4/27
 */
public class PerfCounterVO {

    /** 计数器名称，与 JVM 内部 perf 键一致 */
    private String name;
    /** 计量单位，如 events、bytes（详细模式） */
    private String units;
    /** 变异性分类：CONSTANT / VARIABLE / MONOTONIC 等（详细模式） */
    private String variability;
    /** 当前采样值，具体类型取决于计数器定义 */
    private Object value;

    public PerfCounterVO() {
    }

    public PerfCounterVO(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public PerfCounterVO(String name, String units, String variability, Object value) {
        this.name = name;
        this.units = units;
        this.variability = variability;
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setVariability(String variability) {
        this.variability = variability;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getUnits() {
        return units;
    }

    public String getVariability() {
        return variability;
    }

    public Object getValue() {
        return value;
    }
}
