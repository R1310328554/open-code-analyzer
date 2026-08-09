package com.taobao.arthas.core.command.model;

import java.util.Collection;
import java.util.NavigableMap;

/**
 * jad（反编译）命令的结构化结果：类元信息、源码文本及行号映射。
 * <p>
 * 单类反编译时填充 {@link #classInfo} 与 {@link #source}；
 * 匹配多个类时 {@link #matchedClasses} 非空，需用户进一步指定 classPattern。
 * {@link #mappings} 将反编译后行号映射回原始 .class 行号，供 IDE 跳转对齐。
 *
 * @author gongdewei 2020/4/22
 * @author hengyunabc 2021-02-23
 */
public class JadModel extends ResultModel {
    /** 目标类的摘要（包名、修饰符、父类等） */
    private ClassVO classInfo;
    /** 类文件在磁盘或 jar 内的路径 */
    private String location;
    /** 反编译得到的 Java 源码文本 */
    private String source;
    /** 反编译行号 → 原始字节码行号的映射（NavigableMap 支持按行查找） */
    private NavigableMap<Integer,Integer> mappings;
    /** 多个 ClassLoader 命中时的候选列表，提示用户加 -c 指定 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户指定的 ClassLoader 类名（-c 参数） */
    private String classLoaderClass;

    /** 匹配到多个类时的候选列表（需进一步缩小 classPattern） */
    private Collection<ClassVO> matchedClasses;

    @Override
    public String getType() {
        return "jad";
    }

    public JadModel() {
    }

    public ClassVO getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassVO classInfo) {
        this.classInfo = classInfo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public NavigableMap<Integer, Integer> getMappings() {
        return mappings;
    }

    public void setMappings(NavigableMap<Integer, Integer> mappings) {
        this.mappings = mappings;
    }

    public Collection<ClassVO> getMatchedClasses() {
        return matchedClasses;
    }

    public void setMatchedClasses(Collection<ClassVO> matchedClasses) {
        this.matchedClasses = matchedClasses;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    /** 链式设置 ClassLoader 类名，便于命令层 fluent 构建 */
    public JadModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public JadModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }
}
