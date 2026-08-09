package com.taobao.arthas.core.command.model;

/**
 * Web UI 命令输入区域状态的推送模型。
 * <p>
 * 通过 {@link #inputStatus} 告知前端当前是否可输入、可中断或完全禁用。
 *
 * @author gongdewei 2020/4/14
 */
public class InputStatusModel extends ResultModel {

    /** 当前输入区域状态。 */
    private InputStatus inputStatus;

    public InputStatusModel(InputStatus inputStatus) {
        this.inputStatus = inputStatus;
    }

    public InputStatus getInputStatus() {
        return inputStatus;
    }

    public void setInputStatus(InputStatus inputStatus) {
        this.inputStatus = inputStatus;
    }

    @Override
    public String getType() {
        return "input_status";
    }

}
