package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * {@code vmtool} 命令的结构化结果：封装通过 JVM Tool Interface 获取的对象或 ClassLoader 列表。
 * <p>
 * {@link #value} 为 OGNL 序列化后的 {@link ObjectVO}；当存在多个匹配 ClassLoader 时
 * {@link #matchedClassLoaders} 供用户选择 {@code -c} hash 后再执行。
 *
 * @author hengyunabc 2022-04-24
 */
public class VmToolModel extends ResultModel {
    /** vmtool 表达式求值结果 */
    private ObjectVO value;

    /** 多个 ClassLoader 匹配时的候选列表 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户指定的 ClassLoader 类名（用于消歧提示） */
    private String classLoaderClass;


    /** 结果类型标识，固定为 {@code vmtool} */
    @Override
    public String getType() {
        return "vmtool";
    }

    public ObjectVO getValue() {
        return value;
    }

    public VmToolModel setValue(ObjectVO value) {
        this.value = value;
        return this;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public VmToolModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public VmToolModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }
}
