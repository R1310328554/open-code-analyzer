package com.taobao.arthas.core.command.model;

/**
 * shutdown 命令的结构化结果：告知客户端 Arthas Agent 是否已关闭及附带说明。
 * <p>
 * {@link #graceful} 为 true 表示正常退出（释放资源、断开会话）；false 可能为异常或强制终止。
 * {@link #message} 供 Web Console / Telnet 展示给用户的状态文本。
 *
 * @author gongdewei 2020/6/22
 */
public class ShutdownModel extends ResultModel {

    /** 是否为优雅关闭（与 stop 命令参数及 Session 清理策略相关） */
    private boolean graceful;

    /** 关闭结果的人类可读说明，可为空 */
    private String message;

    public ShutdownModel(boolean graceful, String message) {
        this.graceful = graceful;
        this.message = message;
    }

    @Override
    public String getType() {
        return "shutdown";
    }

    public boolean isGraceful() {
        return graceful;
    }

    public String getMessage() {
        return message;
    }
}
