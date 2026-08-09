package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * 某个 ClassLoader 下加载的类名集合视图，用于 sc 等命令的分页/分段输出。
 * <p>
 * 实现 {@link Countable}，便于 ResultModel 统计条目数；{@link #size()} 在 classes 为空时仍返回 1，
 * 保证分页元数据至少占一条记录。
 *
 * @author gongdewei 2020/4/21
 */
public class ClassSetVO implements Countable {
    /** 所属 ClassLoader 的摘要信息 */
    private ClassLoaderVO classloader;
    /** 当前分段内的全限定类名集合 */
    private Collection<String> classes;
    /** 分段序号，用于大结果集分页展示 */
    private int segment;

    /** 默认 segment 为 0 的便捷构造 */
    public ClassSetVO(ClassLoaderVO classloader, Collection<String> classes) {
        this(classloader, classes, 0);
    }

    public ClassSetVO(ClassLoaderVO classloader, Collection<String> classes, int segment) {
        this.classloader = classloader;
        this.classes = classes;
        this.segment = segment;
    }

    public ClassLoaderVO getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoaderVO classloader) {
        this.classloader = classloader;
    }

    public Collection<String> getClasses() {
        return classes;
    }

    public void setClasses(Collection<String> classes) {
        this.classes = classes;
    }

    public int getSegment() {
        return segment;
    }

    public void setSegment(int segment) {
        this.segment = segment;
    }

    /**
     * 返回本段内类名数量；classes 为 null 时返回 1，避免分页计数为 0。
     */
    @Override
    public int size() {
        return classes != null ? classes.size() : 1;
    }
}
