package com.taobao.arthas.core.command.model;

/**
 * JVM 信息条目：名称 / 值 / 可选说明的三元组。
 * <p>
 * 由 {@link JvmModel} 按分组（如 RUNTIME、CLASS-LOADING）聚合多条 JvmItemVO，
 * 供 jvm 命令以表格形式展示。
 *
 * @author gongdewei 2020/4/24
 */
public class JvmItemVO {
    /** 指标或属性名称（如 VM-NAME、UP-TIME） */
    private String name;
    /** 指标值，类型随条目而异（String、Long、Boolean 等） */
    private Object value;
    /** 可选的人类可读说明，部分条目无 desc */
    private String desc;

    public JvmItemVO(String name, Object value, String desc) {
        this.name = name;
        this.value = value;
        this.desc = desc;
    }

    public JvmItemVO(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
