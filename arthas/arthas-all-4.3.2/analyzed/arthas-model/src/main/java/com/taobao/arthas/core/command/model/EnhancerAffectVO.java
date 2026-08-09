package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * 字节码增强（Enhancer）操作的影响统计值对象，纯数据传输用途。
 * <p>
 * 记录增强耗时、命中类/方法数量、监听器 ID 及可能的异常与 dump 文件路径。
 *
 * @author gongdewei 2020/6/22
 */
public class EnhancerAffectVO {

    /** 增强操作耗时（毫秒）。 */
    private long cost;
    /** 被增强的方法数量。 */
    private int methodCount;
    /** 被增强的类数量。 */
    private int classCount;
    /** 关联的监听器 ID。 */
    private long listenerId;
    /** 增强过程中抛出的异常（若有）。 */
    private Throwable throwable;
    /** 类 dump 文件路径列表。 */
    private List<String> classDumpFiles;
    /** 被增强的方法签名列表。 */
    private List<String> methods;
    /** 超出限制时的提示消息。 */
    private String overLimitMsg;

    public EnhancerAffectVO() {
    }

    public EnhancerAffectVO(long cost, int methodCount, int classCount, long listenerId) {
        this.cost = cost;
        this.methodCount = methodCount;
        this.classCount = classCount;
        this.listenerId = listenerId;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public int getClassCount() {
        return classCount;
    }

    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }

    public long getListenerId() {
        return listenerId;
    }

    public void setListenerId(long listenerId) {
        this.listenerId = listenerId;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public List<String> getClassDumpFiles() {
        return classDumpFiles;
    }

    public void setClassDumpFiles(List<String> classDumpFiles) {
        this.classDumpFiles = classDumpFiles;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public void setOverLimitMsg(String overLimitMsg) {
        this.overLimitMsg = overLimitMsg;
    }

    public String getOverLimitMsg() {
        return overLimitMsg;
    }
}
