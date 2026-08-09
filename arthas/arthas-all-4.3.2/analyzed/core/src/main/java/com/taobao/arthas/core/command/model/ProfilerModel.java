package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * profiler 命令的结构化结果：async-profiler 各子操作的执行反馈。
 * <p>
 * 支持 start/stop/status/list 等多种 action；无参数调用时 {@link #supportedActions}
 * 列出可用动作。stop/dump 完成后 {@link #outputFile} 与 {@link #duration} 描述
 * 采样产物路径与耗时，{@link #format} 对应命令行 {@code -o/--format} 输出格式。
 *
 * @author gongdewei 2020/4/27
 */
public class ProfilerModel extends ResultModel {

    /** 本次执行的 profiler 子命令，如 start、stop、status */
    private String action;
    /** 子命令附加参数字符串（原样回显或供客户端展示） */
    private String actionArg;
    /**
     * profiler stop/dump 输出格式（对应命令行 --format/-o）
     */
    private String format;
    /** 命令执行的人类可读结果摘要或错误信息 */
    private String executeResult;
    /** 当前环境支持的全部 profiler 动作名（help/list 场景） */
    private Collection<String> supportedActions;
    /** 采样结果文件路径（stop/dump 成功时） */
    private String outputFile;
    /** 采样持续时间（毫秒），stop 后回填 */
    private Long duration;

    public ProfilerModel() {
    }

    public ProfilerModel(Collection<String> supportedActions) {
        this.supportedActions = supportedActions;
    }

    @Override
    public String getType() {
        return "profiler";
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionArg() {
        return actionArg;
    }

    public void setActionArg(String actionArg) {
        this.actionArg = actionArg;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Collection<String> getSupportedActions() {
        return supportedActions;
    }

    public void setSupportedActions(Collection<String> supportedActions) {
        this.supportedActions = supportedActions;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
