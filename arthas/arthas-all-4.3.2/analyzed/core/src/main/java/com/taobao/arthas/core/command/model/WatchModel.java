package com.taobao.arthas.core.command.model;

import java.time.LocalDateTime;

/**
 * {@code watch} 命令的结构化结果：单次方法拦截点的观测快照。
 * <p>
 * 包含触发时间、耗时、OGNL 表达式求值结果及类/方法/切点信息；
 * {@link #sizeLimit} 与 {@link #accessPoint} 对应命令行 {@code -M} 与 before/after 选项。
 *
 * @author gongdewei 2020/03/26
 */
public class WatchModel extends ResultModel {

    /** 观测触发时间戳 */
    private LocalDateTime ts;
    /** 方法调用耗时（毫秒） */
    private double cost;
    /** OGNL 表达式求值结果 */
    private ObjectVO value;

    /** 结果字节数上限（与 options object-size-limit 联动） */
    private Integer sizeLimit;
    /** 被观测类的全限定名 */
    private String className;
    /** 被观测方法名 */
    private String methodName;
    /** 切点：before / afterReturning / afterThrowing 等 */
    private String accessPoint;

    public WatchModel() {
    }

    /** 结果类型标识，固定为 {@code watch} */
    @Override
    public String getType() {
        return "watch";
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

    public ObjectVO getValue() {
        return value;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setValue(ObjectVO value) {
        this.value = value;
    }

    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
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

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }
}
