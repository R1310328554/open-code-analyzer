package com.taobao.arthas.core.command.model;

import java.util.Collection;
import java.util.List;

/**
 * sc（search class）命令的结构化结果：类搜索命中列表或单个类详情。
 * <p>
 * 两种构造模式互斥：{@link #classNames}+{@link #segment} 用于分页列出匹配类名；
 * {@link #classInfo} 配合 {@link #detailed}/{@link #withField} 返回单类结构与字段。
 * 多 ClassLoader 场景下填充 {@link #matchedClassLoaders} 提示用户加 {@code -c}。
 *
 * @author gongdewei 2020/04/08
 */
public class SearchClassModel extends ResultModel {
    /** 单个类的详细信息（详情模式） */
    private ClassDetailVO classInfo;
    /** 是否在详情中包含字段信息 */
    private boolean withField;
    /** 是否输出完整类结构（反编译级别细节） */
    private boolean detailed;
    /** 匹配到的类全限定名列表（列表模式） */
    private List<String> classNames;
    /** 分页段号，配合 Arthas 结果分段输出 */
    private int segment;

    /** ClassLoader 歧义时的候选加载器 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户指定的 ClassLoader 类名 */
    private String classLoaderClass;

    public SearchClassModel() {
    }

    public SearchClassModel(ClassDetailVO classInfo, boolean detailed, boolean withField) {
        this.classInfo = classInfo;
        this.detailed = detailed;
        this.withField = withField;
    }

    public SearchClassModel(List<String> classNames, int segment) {
        this.classNames = classNames;
        this.segment = segment;
    }

    @Override
    public String getType() {
        return "sc";
    }

    public ClassDetailVO getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassDetailVO classInfo) {
        this.classInfo = classInfo;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public int getSegment() {
        return segment;
    }

    public void setSegment(int segment) {
        this.segment = segment;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public boolean isWithField() {
        return withField;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public SearchClassModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public SearchClassModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }
}
