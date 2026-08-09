package com.taobao.arthas.core.command.model;

/**
 * CLI 命令 positional 参数的描述视图对象，供 help 命令展示参数名与约束。
 * <p>
 * 由 {@link com.taobao.arthas.core.command.basic1000.HelpCommand} 从
 * {@link com.taobao.middleware.cli.annotations.Argument} 元数据填充。
 *
 * @author gongdewei 2020/4/3
 */
public class ArgumentVO {
    /** 参数名（argName） */
    private String argName;
    /** 是否必填 */
    private boolean required;
    /** 是否允许多值（如可变参数列表） */
    private boolean multiValued;

    public ArgumentVO() {
    }

    /** 构造带完整约束的参数描述 */
    public ArgumentVO(String argName, boolean required, boolean multiValued) {
        this.argName = argName;
        this.required = required;
        this.multiValued = multiValued;
    }

    public String getArgName() {
        return argName;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public void setMultiValued(boolean multiValued) {
        this.multiValued = multiValued;
    }
}
