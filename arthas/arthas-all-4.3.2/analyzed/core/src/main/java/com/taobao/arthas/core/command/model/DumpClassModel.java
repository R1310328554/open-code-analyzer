package com.taobao.arthas.core.command.model;

import java.util.Collection;
import java.util.List;

/**
 * {@code dump} 命令的结果模型，描述类字节码导出 outcome。
 * <p>
 * 成功时 {@link #dumpedClasses} 含磁盘路径；匹配歧义时可能仅填充
 * {@link #matchedClasses} / {@link #matchedClassLoaders} 供用户二次指定 ClassLoader。
 *
 * @author gongdewei 2020/4/21
 */
public class DumpClassModel extends ResultModel {

    /** 已成功 dump 到本地的类及路径列表 */
    private List<DumpClassVO> dumpedClasses;

    /** 模式匹配到的候选类（多 ClassLoader 冲突时返回） */
    private Collection<ClassVO> matchedClasses;
    /** 模式匹配到的候选 ClassLoader */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户指定的 ClassLoader 类名过滤条件 */
    private String classLoaderClass;

    public DumpClassModel() {
    }

    @Override
    public String getType() {
        return "dump";
    }

    public List<DumpClassVO> getDumpedClasses() {
        return dumpedClasses;
    }

    public DumpClassModel setDumpedClasses(List<DumpClassVO> dumpedClasses) {
        this.dumpedClasses = dumpedClasses;
        return this;
    }

    public Collection<ClassVO> getMatchedClasses() {
        return matchedClasses;
    }

    public DumpClassModel setMatchedClasses(Collection<ClassVO> matchedClasses) {
        this.matchedClasses = matchedClasses;
        return this;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public DumpClassModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public DumpClassModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

}
