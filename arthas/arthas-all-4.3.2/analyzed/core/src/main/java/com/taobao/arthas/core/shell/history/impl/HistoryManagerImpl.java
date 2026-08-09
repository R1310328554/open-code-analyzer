package com.taobao.arthas.core.shell.history.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.history.HistoryManager;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link HistoryManager} 默认实现：内存环形缓冲 + 文件持久化。
 * <p>
 * 超过 {@link #MAX_HISTORY_SIZE} 条时丢弃最旧记录；读写均同步以保证线程安全。
 *
 * @see io.termd.core.readline.Readline#history
 * @author gongdewei 2020/4/8
 */
public class HistoryManagerImpl implements HistoryManager {
    /** 内存中保留的最大历史条数，超出时移除队首最旧条目 */
    private static final int MAX_HISTORY_SIZE = 500;

    private static final Logger logger = LoggerFactory.getLogger(HistoryManagerImpl.class);

    /** 命令行历史列表，按执行顺序排列 */
    private List<String> history = new ArrayList<String>();

    public HistoryManagerImpl() {
    }

    @Override
    /** 将当前历史写入 {@link Constants#CMD_HISTORY_FILE}，失败时记录日志 */
    public synchronized void saveHistory() {
        try {
            FileUtils.saveCommandHistoryString(history, new File(Constants.CMD_HISTORY_FILE));
        } catch (Throwable e) {
            logger.error("save command history failed", e);
        }
    }

    @Override
    /** 从命令历史文件加载到内存，失败时保留空列表并记录日志 */
    public synchronized void loadHistory() {
        try {
            history = FileUtils.loadCommandHistoryString(new File(Constants.CMD_HISTORY_FILE));
        } catch (Throwable e) {
            logger.error("load command history failed", e);
        }
    }

    @Override
    /** 清空内存历史，不删除磁盘文件 */
    public synchronized void clearHistory() {
        this.history.clear();
    }

    @Override
    /** 追加命令行；若已达上限则循环移除最旧条目后再添加 */
    public synchronized void addHistory(String commandLine) {
        // 超出容量时丢弃最早的历史记录
        while (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(commandLine);
    }

    @Override
    /** @return 历史列表的防御性副本，避免外部直接修改内部状态 */
    public synchronized List<String> getHistory() {
        return new ArrayList<String>(history);
    }

    @Override
    /** 整体替换内存历史（通常用于 load 或外部导入） */
    public synchronized void setHistory(List<String> history) {
        this.history = history;
    }

}
