package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * sm（search method）命令的结构化结果：在类中搜索匹配的方法签名。
 * <p>
 * {@link #methodInfo} 封装命中方法的类名、方法名、描述符等；{@link #detail}
 * 为 true 时包含更完整的字节码/注解信息。ClassLoader 过滤逻辑与 sc 命令一致，
 * 歧义时通过 {@link #matchedClassLoaders} 返回候选列表。
 *
 * @author gongdewei 2020/4/9
 */
public class SearchMethodModel extends ResultModel {
    /** 匹配到的方法信息（可能含多个重载时的代表条目） */
    private MethodVO methodInfo;
    /** 是否返回详细方法结构（参数、返回值、修饰符等） */
    private boolean detail;

    /** 多个 ClassLoader 匹配时的候选集合 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** -c 指定的 ClassLoader 类名 */
    private String classLoaderClass;

    public SearchMethodModel() {
    }

    public SearchMethodModel(MethodVO methodInfo, boolean detail) {
        this.methodInfo = methodInfo;
        this.detail = detail;
    }

    public MethodVO getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(MethodVO methodInfo) {
        this.methodInfo = methodInfo;
    }

    public boolean isDetail() {
        return detail;
    }

    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public SearchMethodModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public SearchMethodModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }
    
    @Override
    public String getType() {
        return "sm";
    }
}
