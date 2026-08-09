package com.taobao.arthas.core.command.model;


/**
 * Arthas 命令行选项的视图对象，描述 help 输出中的单个 flag/option。
 * <p>
 * 由 CLI 元数据转换而来，供 {@link CommandVO} 与 {@link HelpModel} 序列化为 JSON。
 *
 * @author gongdewei 2020/4/3
 */
public class CommandOptionVO {
    /**
     * the option long name.
     * 长选项名，如 --help
     */
    private String longName;

    /**
     * the option short name.
     * 短选项名，如 -h
     */
    private String shortName;

    /**
     * The option description.
     * 选项说明文本，展示在 help 详情中
     */
    private String description;

    /**
     * whether or not the option receives a single value or  multiple values.
     * 是否接受参数值（true 表示需跟值，false 为纯开关）
     */
    private boolean acceptValue;

    public CommandOptionVO() {
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAcceptValue() {
        return acceptValue;
    }

    public void setAcceptValue(boolean acceptValue) {
        this.acceptValue = acceptValue;
    }
}
