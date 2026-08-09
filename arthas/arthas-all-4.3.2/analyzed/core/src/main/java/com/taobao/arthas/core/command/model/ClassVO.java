package com.taobao.arthas.core.command.model;

/**
 * 类的最小视图对象，承载全限定名与 ClassLoader 定位信息。
 * <p>
 * 作为 {@link ClassDetailVO}、{@link DumpClassVO} 等更详细 VO 的基类，
 * 供 sc、dump、getstatic 等命令在 JSON 结果中标识目标类。
 *
 * @author gongdewei 2020/4/8
 */
public class ClassVO {

    /** 类的全限定名 */
    private String name;
    /** ClassLoader 层级描述（hash、类型等字符串数组） */
    private String[] classloader;
    /** ClassLoader 实例的 hash 标识，用于在多个加载器中区分同名类 */
    private String classLoaderHash;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getClassloader() {
        return classloader;
    }

    public void setClassloader(String[] classloader) {
        this.classloader = classloader;
    }

    public String getClassLoaderHash() {
        return classLoaderHash;
    }

    public void setClassLoaderHash(String classLoaderHash) {
        this.classLoaderHash = classLoaderHash;
    }
}
