package com.taobao.arthas.core.command.model;

import java.time.LocalDateTime;

/**
 * TimeTunnel（tt）单条时间片视图：记录一次方法调用的入参、返回值、异常与耗时。
 * <p>
 * 支持链式 setter 便于 Agent 侧流式组装；{@link #isReturn} 与 {@link #isThrow} 互斥表示
 * 正常返回或异常结束，二者均为 false 时可能表示仅记录了进入（inbound）片段。
 *
 * @author gongdewei 2020/4/27
 */
public class TimeFragmentVO {
    /** 在 tt 索引表中的序号（{@code tt -i} 定位用） */
    private Integer index;
    /** 调用发生时刻 */
    private LocalDateTime timestamp;
    /** 方法执行耗时（毫秒） */
    private double cost;
    /** 是否为正常 return 结束 */
    private boolean isReturn;
    /** 是否为 throw 结束 */
    private boolean isThrow;
    /** 目标对象描述（类名@hash 或 toString 摘要） */
    private String object;
    /** 声明类全限定名 */
    private String className;
    /** 方法名 */
    private String methodName;
    /** 入参列表，元素为 {@link ObjectVO} 序列化视图 */
    private ObjectVO[] params;
    /** 返回值对象视图（isReturn 为 true 时有效） */
    private ObjectVO returnObj;
    /** 抛出的异常对象视图（isThrow 为 true 时有效） */
    private ObjectVO throwExp;

    public TimeFragmentVO() {
    }

    public Integer getIndex() {
        return index;
    }

    public TimeFragmentVO setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TimeFragmentVO setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public double getCost() {
        return cost;
    }

    public TimeFragmentVO setCost(double cost) {
        this.cost = cost;
        return this;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public TimeFragmentVO setReturn(boolean aReturn) {
        isReturn = aReturn;
        return this;
    }

    public boolean isThrow() {
        return isThrow;
    }

    public TimeFragmentVO setThrow(boolean aThrow) {
        isThrow = aThrow;
        return this;
    }

    public String getObject() {
        return object;
    }

    public TimeFragmentVO setObject(String object) {
        this.object = object;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public TimeFragmentVO setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public TimeFragmentVO setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public ObjectVO[] getParams() {
        return params;
    }

    public TimeFragmentVO setParams(ObjectVO[] params) {
        this.params = params;
        return this;
    }

    public ObjectVO getReturnObj() {
        return returnObj;
    }

    public TimeFragmentVO setReturnObj(ObjectVO returnObj) {
        this.returnObj = returnObj;
        return this;
    }

    public ObjectVO getThrowExp() {
        return throwExp;
    }

    public TimeFragmentVO setThrowExp(ObjectVO throwExp) {
        this.throwExp = throwExp;
        return this;
    }
}
