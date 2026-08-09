package com.taobao.arthas.core.command.model;

import com.taobao.arthas.core.command.klass100.ClassLoaderCommand.ClassLoaderStat;
import com.taobao.arthas.core.command.klass100.ClassLoaderCommand.ClassLoaderUrlStat;
import com.taobao.arthas.core.command.klass100.ClassLoaderCommand.UrlClassStat;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * classloader 命令的统一结果模型，按子命令填充不同字段组合。
 * <p>
 * 支持 -l 列表、-t 树形、统计、URL 与类加载明细等多种输出模式；
 * type 固定为 "classloader"。
 *
 * @author gongdewei 2020/4/21
 */
public class ClassLoaderModel extends ResultModel {

    /** 按 pattern 匹配的类集合（classloader 查类） */
    private ClassSetVO classSet;
    /** getResources 结果路径列表 */
    private List<String> resources;
    /** 单个类的加载详情 */
    private ClassDetailVO loadClass;
    /** ClassLoader 的 URL classpath 列表 */
    private List<String> urls;
    /** classloader -l / -t：ClassLoader 列表或树节点 */
    private List<ClassLoaderVO> classLoaders;
    /** 是否为树形结构输出 */
    private Boolean tree;

    /** 按名称聚合的 ClassLoader 统计信息 */
    private Map<String, ClassLoaderStat> classLoaderStats;

    /** 按类名匹配到的多个 ClassLoader（需用户再指定 -c） */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用于匹配 ClassLoader 的类名 */
    private String classLoaderClass;

    /** URL 维度统计：ClassLoader → URL 加载统计 */
    private Map<ClassLoaderVO, ClassLoaderUrlStat> urlStats;

    /** 指定 ClassLoader 下 URL→已加载类 统计 */
    private ClassLoaderVO classLoader;
    private List<UrlClassStat> urlClassStats;
    /** 是否输出 URL 类统计明细 */
    private Boolean urlClassStatsDetail;

    public ClassLoaderModel() {
    }

    /** 结果类型标识 "classloader" */
    @Override
    public String getType() {
        return "classloader";
    }

    public ClassSetVO getClassSet() {
        return classSet;
    }

    public ClassLoaderModel setClassSet(ClassSetVO classSet) {
        this.classSet = classSet;
        return this;
    }

    public List<String> getResources() {
        return resources;
    }

    public ClassLoaderModel setResources(List<String> resources) {
        this.resources = resources;
        return this;
    }

    public ClassDetailVO getLoadClass() {
        return loadClass;
    }

    public ClassLoaderModel setLoadClass(ClassDetailVO loadClass) {
        this.loadClass = loadClass;
        return this;
    }

    public List<String> getUrls() {
        return urls;
    }

    public ClassLoaderModel setUrls(List<String> urls) {
        this.urls = urls;
        return this;
    }

    public List<ClassLoaderVO> getClassLoaders() {
        return classLoaders;
    }

    public ClassLoaderModel setClassLoaders(List<ClassLoaderVO> classLoaders) {
        this.classLoaders = classLoaders;
        return this;
    }

    public Boolean getTree() {
        return tree;
    }

    public ClassLoaderModel setTree(Boolean tree) {
        this.tree = tree;
        return this;
    }

    public Map<String, ClassLoaderStat> getClassLoaderStats() {
        return classLoaderStats;
    }

    public ClassLoaderModel setClassLoaderStats(Map<String, ClassLoaderStat> classLoaderStats) {
        this.classLoaderStats = classLoaderStats;
        return this;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public ClassLoaderModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public ClassLoaderModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    public Map<ClassLoaderVO, ClassLoaderUrlStat> getUrlStats() {
        return urlStats;
    }

    public void setUrlStats(Map<ClassLoaderVO, ClassLoaderUrlStat> urlStats) {
        this.urlStats = urlStats;
    }

    public ClassLoaderVO getClassLoader() {
        return classLoader;
    }

    public ClassLoaderModel setClassLoader(ClassLoaderVO classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public List<UrlClassStat> getUrlClassStats() {
        return urlClassStats;
    }

    public ClassLoaderModel setUrlClassStats(List<UrlClassStat> urlClassStats) {
        this.urlClassStats = urlClassStats;
        return this;
    }

    public Boolean getUrlClassStatsDetail() {
        return urlClassStatsDetail;
    }

    public ClassLoaderModel setUrlClassStatsDetail(Boolean urlClassStatsDetail) {
        this.urlClassStatsDetail = urlClassStatsDetail;
        return this;
    }

}
