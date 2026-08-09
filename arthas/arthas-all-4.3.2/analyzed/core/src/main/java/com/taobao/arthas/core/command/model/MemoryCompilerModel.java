package com.taobao.arthas.core.command.model;

import java.util.Collection;
import java.util.List;

/**
 * mc（Memory Compiler）命令的结构化结果：内存编译生成的类文件路径列表。
 * <p>
 * 用户在 Arthas 中提交 Java 源码片段，mc 编译后返回 .class 输出路径；
 * 多 ClassLoader 场景同样携带 {@link #matchedClassLoaders} 供用户消歧。
 *
 * @author gongdewei 2020/4/20
 */
public class MemoryCompilerModel extends ResultModel {

    /** 编译产物路径列表（通常位于临时目录下的 .class 文件） */
    private List<String> files;
    /** 多个 ClassLoader 命中时的候选 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 指定用于编译/加载的 ClassLoader 类名 */
    private String classLoaderClass;

    public MemoryCompilerModel() {
    }

    public MemoryCompilerModel(List<String> files) {
        this.files = files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public MemoryCompilerModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public MemoryCompilerModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    @Override
    public String getType() {
        return "mc";
    }

}
