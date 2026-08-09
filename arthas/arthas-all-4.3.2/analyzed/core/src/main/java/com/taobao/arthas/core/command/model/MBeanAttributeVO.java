package com.taobao.arthas.core.command.model;

/**
 * MBean 单个属性的取值视图：名称、值及可选的错误信息。
 * <p>
 * 读取 JMX 属性失败时 {@link #error} 非空而 {@link #value} 可能为 null，
 * 便于 mbean 命令区分「属性不存在」与「读取异常」。
 *
 * @author gongdewei 2020/4/26
 */
public class MBeanAttributeVO {
    /** JMX 属性名 */
    private String name;
    /** 属性当前值；读取失败或未设置时为 null */
    private Object value;
    /** 读取失败时的异常消息，成功时为 null */
    private String error;

    public MBeanAttributeVO(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public MBeanAttributeVO(String name, Object value, String error) {
        this.name = name;
        this.value = value;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
