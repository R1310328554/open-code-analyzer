package com.taobao.arthas.core.command.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * redefine 命令的结构化结果：热替换 class 字节码后的汇总信息。
 * <p>
 * 通过 Instrumentation.redefineClasses 完成替换；{@link #addRedefineClass} 每成功
 * 替换一个类会同步递增 {@link #redefinitionCount}。多 ClassLoader 歧义时返回
 * {@link #matchedClassLoaders} 供用户指定 {@code -c} 后重试。
 *
 * @author gongdewei 2020/4/16
 */
public class RedefineModel extends ResultModel {

    /** 成功完成 redefine 的类数量，与 redefinedClasses 长度通常一致 */
    private int redefinitionCount;

    /** 已被热替换的类全限定名列表 */
    private List<String> redefinedClasses;
    /** 匹配到的候选 ClassLoader（未指定 -c 且存在多个时） */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户指定的 ClassLoader 类名过滤条件 */
    private String classLoaderClass;

    public RedefineModel() {
        redefinedClasses = new ArrayList<String>();
    }

    /** 记录一次成功的类重定义，计数与列表同步更新 */
    public void addRedefineClass(String className) {
        redefinedClasses.add(className);
        redefinitionCount++;
    }

    public int getRedefinitionCount() {
        return redefinitionCount;
    }

    public void setRedefinitionCount(int redefinitionCount) {
        this.redefinitionCount = redefinitionCount;
    }

    public List<String> getRedefinedClasses() {
        return redefinedClasses;
    }

    public void setRedefinedClasses(List<String> redefinedClasses) {
        this.redefinedClasses = redefinedClasses;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public RedefineModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public RedefineModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    @Override
    public String getType() {
        return "redefine";
    }

}
