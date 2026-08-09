package com.taobao.arthas.core.command.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.taobao.arthas.core.command.klass100.RetransformCommand.RetransformEntry;

/**
 * retransform 命令的结构化结果：对已增强类触发 retransform 或管理 retransform 规则。
 * <p>
 * 支持按类名批量 retransform、列出/删除 {@link RetransformEntry} 规则；
 * {@link #ids} 关联 watch/trace 等增强 id；{@link #deletedRetransformEntry}
 * 在删除单条规则时回填被移除项。
 *
 * @author hengyunabc 2021-01-06
 *
 */
public class RetransformModel extends ResultModel {

    /** 本次 retransform 成功的类数量 */
    private int retransformCount;

    /** 已 retransform 的类全限定名列表 */
    private List<String> retransformClasses;
    /** ClassLoader 歧义时的候选列表 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** -c 指定的 ClassLoader 类名 */
    private String classLoaderClass;

    /** 当前注册的 retransform 规则条目（list 子命令） */
    private List<RetransformEntry> retransformEntries;

    /** 关联的增强器 id 列表（与 trace/watch 等命令联动） */
    private List<Integer> ids;

    /** delete 子命令移除的单条规则，便于客户端确认 */
    private RetransformEntry deletedRetransformEntry;
    
//    private List<ClassVO> trigger

//    List<ClassVO> classVOs = ClassUtils.createClassVOList(matchedClasses);
    public RetransformModel() {
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    /** 懒初始化列表并递增计数，避免 NPE */
    public void addRetransformClass(String className) {
        if (retransformClasses == null) {
            retransformClasses = new ArrayList<String>();
        }
        retransformClasses.add(className);
        retransformCount++;
    }

    public int getRetransformCount() {
        return retransformCount;
    }

    public void setRetransformCount(int retransformCount) {
        this.retransformCount = retransformCount;
    }

    public List<String> getRetransformClasses() {
        return retransformClasses;
    }

    public void setRetransformClasses(List<String> retransformClasses) {
        this.retransformClasses = retransformClasses;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public RetransformModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public RetransformModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    public List<RetransformEntry> getRetransformEntries() {
        return retransformEntries;
    }

    public void setRetransformEntries(List<RetransformEntry> retransformEntries) {
        this.retransformEntries = retransformEntries;
    }

    public RetransformEntry getDeletedRetransformEntry() {
        return deletedRetransformEntry;
    }

    public void setDeletedRetransformEntry(RetransformEntry deletedRetransformEntry) {
        this.deletedRetransformEntry = deletedRetransformEntry;
    }

    @Override
    public String getType() {
        return "retransform";
    }

}
