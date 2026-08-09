package com.taobao.arthas.core.command.model;

import com.sun.management.VMOption;

import java.util.List;

/**
 * vmoption 命令的结构化结果：列出 HotSpot 可管理 VM 选项或报告修改结果。
 * <p>
 * 查询模式填充 {@link #vmOptions}（来自 {@code com.sun.management.VMOption}）；
 * 写模式（{@code vmoption name value}）填充 {@link #changeResult}，二者通常互斥。
 *
 * @author gongdewei 2020/4/15
 */
public class VMOptionModel extends ResultModel {

    /** 当前 JVM 全部或匹配的 VM 选项列表（只读查询） */
    private List<VMOption> vmOptions;

    /** 修改单个选项后的成功/失败摘要 */
    private ChangeResultVO changeResult;

    public VMOptionModel() {
    }

    public VMOptionModel(List<VMOption> vmOptions) {
        this.vmOptions = vmOptions;
    }

    public VMOptionModel(ChangeResultVO changeResult) {
        this.changeResult = changeResult;
    }

    @Override
    public String getType() {
        return "vmoption";
    }

    public List<VMOption> getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(List<VMOption> vmOptions) {
        this.vmOptions = vmOptions;
    }

    public ChangeResultVO getChangeResult() {
        return changeResult;
    }

    public void setChangeResult(ChangeResultVO changeResult) {
        this.changeResult = changeResult;
    }
}
