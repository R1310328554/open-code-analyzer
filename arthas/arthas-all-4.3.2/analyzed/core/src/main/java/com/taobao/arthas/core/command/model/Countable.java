package com.taobao.arthas.core.command.model;

/**
 * 可计数的结果项接口，供 {@link ResultModel} 及其嵌套 VO 统计展示条目数。
 * <p>
 * 实现类应在 {@link #size()} 中返回至少 1，以便前端分页与进度条正确渲染。
 *
 * Item countable for ResultModel
 * @author gongdewei 2020/6/8
 */
public interface Countable {

    /**
     * Get item size of this result model, the value of size is greater than or equal to 1
     * 返回本结果模型包含的逻辑条目数，取值 &gt;= 1
     * @return item size of this result model
     */
    int size();

}
