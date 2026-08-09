package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * options 命令的结构化结果：查询全部配置或报告单次修改结果。
 * <p>
 * 无参数查询时填充 {@link #options} 列表；带 name=value 修改时填充
 * {@link #changeResult}，二者通常互斥，客户端按非空字段决定展示模式。
 *
 * @author gongdewei 2020/4/15
 */
public class OptionsModel extends ResultModel{
    /** 当前全部可配置项及其值（列表查询模式） */
    private List<OptionVO> options;
    /** 单次 set 操作的成功/失败及变更详情（修改模式） */
    private ChangeResultVO changeResult;

    public OptionsModel() {
    }

    public OptionsModel(List<OptionVO> options) {
        this.options = options;
    }

    public OptionsModel(ChangeResultVO changeResult) {
        this.changeResult = changeResult;
    }

    @Override
    public String getType() {
        return "options";
    }

    public List<OptionVO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionVO> options) {
        this.options = options;
    }

    public ChangeResultVO getChangeResult() {
        return changeResult;
    }

    public void setChangeResult(ChangeResultVO changeResult) {
        this.changeResult = changeResult;
    }
}
