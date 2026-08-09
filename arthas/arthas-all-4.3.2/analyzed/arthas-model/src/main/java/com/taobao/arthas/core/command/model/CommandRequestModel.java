package com.taobao.arthas.core.command.model;

/**
 * 命令异步执行过程的状态模型，表示请求受理进度而非命令最终执行结果。
 * <p>
 * 通过 {@link #state} 描述当前阶段，{@link #command} 标识命令名，{@link #message} 携带附加说明。
 *
 * @author gongdewei 2020/4/2
 */
public class CommandRequestModel extends ResultModel {

    /** 异步执行状态（如 submitted、running 等）。 */
    private String state;
    /** 关联的命令名称。 */
    private String command;
    /** 状态附加消息或错误提示。 */
    private String message;

    public CommandRequestModel() {
    }

    public CommandRequestModel(String command, String state) {
        this.command = command;
        this.state = state;
    }

    public CommandRequestModel(String command, String state, String message) {
        this.state = state;
        this.command = command;
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return "command";
    }
}
