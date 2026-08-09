package com.taobao.arthas.core.command.model;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassLoader 摘要视图：名称、哈希、父加载器及已加载类数等。
 * <p>
 * 支持 children 列表构建加载器树（classloader -t）；亦用于多 ClassLoader
 * 匹配时提示用户指定 -c。
 *
 * @author gongdewei 2020/4/21
 */
public class ClassLoaderVO {
    /** ClassLoader.toString() 或自定义描述 */
    private String name;
    /** 十六进制 hashCode，与 -c 参数对应 */
    private String hash;
    /** 父 ClassLoader 哈希或描述 */
    private String parent;
    /** 该加载器已加载的类数量 */
    private Integer loadedCount;
    /** 同类型 ClassLoader 实例个数（统计模式） */
    private Integer numberOfInstances;
    /** 子 ClassLoader 列表，树形输出时使用 */
    private List<ClassLoaderVO> children;

    public ClassLoaderVO() {
    }

    /** 懒初始化 children 并追加子节点，构建 classloader 树 */
    public void addChild(ClassLoaderVO child){
        if (this.children == null){
            this.children = new ArrayList<ClassLoaderVO>();
        }
        this.children.add(child);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Integer getLoadedCount() {
        return loadedCount;
    }

    public void setLoadedCount(Integer loadedCount) {
        this.loadedCount = loadedCount;
    }

    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    public List<ClassLoaderVO> getChildren() {
        return children;
    }

    public void setChildren(List<ClassLoaderVO> children) {
        this.children = children;
    }
}
