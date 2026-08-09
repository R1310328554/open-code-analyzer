package com.taobao.arthas.core.command.model;

/**
 * 配置或属性变更前后对比视图，记录名称、旧值与新值。
 * <p>
 * 用于 sysprop、vmoption 等命令展示修改结果，便于审计单次变更。
 *
 * @author gongdewei 2020/4/16
 */
public class ChangeResultVO {
    /** 被修改项名称（属性名、选项名等） */
    private String name;
    /** 修改前的值 */
    private Object beforeValue;
    /** 修改后的值 */
    private Object afterValue;

    public ChangeResultVO() {
    }

    /** 构造一次变更的三元组记录 */
    public ChangeResultVO(String name, Object beforeValue, Object afterValue) {
        this.name = name;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(Object beforeValue) {
        this.beforeValue = beforeValue;
    }

    public Object getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(Object afterValue) {
        this.afterValue = afterValue;
    }
}

