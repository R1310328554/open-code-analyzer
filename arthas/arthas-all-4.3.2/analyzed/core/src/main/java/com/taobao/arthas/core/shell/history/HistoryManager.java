package com.taobao.arthas.core.shell.history;

import java.util.List;

/**
 * Shell 命令行历史记录管理接口。
 * <p>
 * 负责内存中历史条目的增删查改，以及持久化到本地文件；供 readline 上下键翻阅历史使用。
 *
 * @author gongdewei 2020/4/8
 */
public interface HistoryManager {

    /** 追加一条已执行的命令行到历史列表 */
    void addHistory(String commandLine);

    /** @return 当前历史记录的副本列表 */
    List<String> getHistory();

    /** 用外部列表整体替换内存中的历史记录 */
    void setHistory(List<String> history);

    /** 将内存历史持久化到磁盘文件 */
    void saveHistory();

    /** 从磁盘文件加载历史到内存 */
    void loadHistory();

    /** 清空内存中的全部历史条目 */
    void clearHistory();
}
