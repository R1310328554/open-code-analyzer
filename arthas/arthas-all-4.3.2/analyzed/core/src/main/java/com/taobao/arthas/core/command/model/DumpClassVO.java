package com.taobao.arthas.core.command.model;

/**
 * 已 dump 的类视图，在 {@link ClassVO} 基础上增加字节码文件落盘路径。
 * <p>
 * {@link #location} 为绝对或相对路径，供用户直接打开 .class 文件分析。
 *
 * Dumped class VO
 * @author gongdewei 2020/7/9
 */
public class DumpClassVO extends ClassVO {
    /** dump 生成的 .class 文件路径 */
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
