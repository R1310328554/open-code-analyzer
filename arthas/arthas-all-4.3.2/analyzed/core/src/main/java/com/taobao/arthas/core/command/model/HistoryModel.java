package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * history 命令的结构化结果：返回当前会话已执行命令的历史记录列表。
 * <p>
 * 每条记录通常为命令字符串；Web Console / Telnet 客户端据此渲染命令回溯面板。
 *
 * @author gongdewei 2020/4/8
 */
public class HistoryModel extends ResultModel {

    /** 按时间顺序排列的命令历史条目（最新通常在末尾，取决于 Session 实现） */
    private List<String> history;

    public HistoryModel() {
    }

    public HistoryModel(List<String> history) {
        this.history = history;
    }

    public List<String> getHistory() {
        return history;
    }

    @Override
    public String getType() {
        return "history";
    }
}
