package com.taobao.arthas.core.command.model;

/**
 * 类字段的视图对象，用于 jad、sc -d、getstatic 等命令展示字段元数据与当前值。
 * <p>
 * {@link #value} 为 {@link ObjectVO}，支持对象图展开深度控制；静态字段由 {@link #isStatic} 标识。
 *
 * @author gongdewei 2020/4/8
 */
public class FieldVO {
    /** 字段名 */
    private String name;
    /** 字段类型（全限定类名或基本类型名） */
    private String type;
    /** 访问修饰符字符串，如 private static final */
    private String modifier;
    /** 字段上的注解类型名列表 */
    private String[] annotations;
    /** 字段当前值（可能为 null 或嵌套 ObjectVO） */
    private ObjectVO value;
    /** 是否为 static 字段 */
    private boolean isStatic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public ObjectVO getValue() {
        return value;
    }

    public void setValue(ObjectVO value) {
        this.value = value;
    }

    public String[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String[] annotations) {
        this.annotations = annotations;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

}
