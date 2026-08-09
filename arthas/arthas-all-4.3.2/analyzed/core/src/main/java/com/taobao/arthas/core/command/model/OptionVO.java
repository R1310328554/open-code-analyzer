package com.taobao.arthas.core.command.model;

/**
 * Arthas 全局配置项的值对象，对应 options 命令展示/修改的单条选项。
 * <p>
 * {@link #level} 区分选项作用域（如全局、会话级）；{@link #type} 描述值类型以便
 * 客户端做输入校验；{@link #summary} 为简短说明，{@link #description} 为详细文档。
 *
 * @author gongdewei 2020/4/15
 */
public class OptionVO {
    /** 选项级别/作用域编码（由 Arthas 配置体系定义） */
    private int level;
    /** 值类型标识，如 boolean、int、String，供前端渲染对应控件 */
    private String type;
    /** 选项名称，与命令行 options 子命令参数一致 */
    private String name;
    /** 当前生效的配置值（字符串形式，由 type 决定解析方式） */
    private String value;
    /** 一行摘要，适合列表展示 */
    private String summary;
    /** 完整说明文本，可含使用建议与默认值描述 */
    private String description;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
