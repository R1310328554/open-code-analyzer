package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * ognl 命令的结构化结果：OGNL 表达式求值返回值及 ClassLoader 上下文。
 * <p>
 * 当用户未指定 {@code -c} 且匹配到多个 ClassLoader 时，{@link #matchedClassLoaders}
 * 会列出候选加载器供客户端提示选择；{@link #value} 封装表达式结果的对象表示。
 *
 * @author gongdewei 2020/4/29
 */
public class OgnlModel extends ResultModel {
    /** OGNL 表达式求值结果（类型、字符串形式等由 ObjectVO 描述） */
    private ObjectVO value;

    /** 表达式解析时匹配到的多个 ClassLoader（歧义场景下返回给客户端） */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户通过 -c 指定的 ClassLoader 类名，用于限定求值上下文 */
    private String classLoaderClass;


    @Override
    public String getType() {
        return "ognl";
    }

    public ObjectVO getValue() {
        return value;
    }

    /** 设置求值结果，支持链式调用 */
    public OgnlModel setValue(ObjectVO value) {
        this.value = value;
        return this;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public OgnlModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public OgnlModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }
}
