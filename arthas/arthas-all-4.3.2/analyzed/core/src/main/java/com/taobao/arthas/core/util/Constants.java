package com.taobao.arthas.core.util;

import java.io.File;

import com.taobao.arthas.common.PidUtils;
import com.taobao.arthas.core.view.Ansi;

/**
 * Arthas Shell 与 CLI 全局常量定义。
 *
 * @author ralf0131 2016-12-28 16:20.
 */
public class Constants {

    private Constants() {
    }

    /**
     * 中断提示：按 Q 或 Ctrl+C 可中止当前操作
     */
    public static final String Q_OR_CTRL_C_ABORT_MSG = "Press Q or Ctrl+C to abort.";

    /**
     * 空字符串占位
     */
    public static final String EMPTY_STRING = "";

    /**
     * 默认命令提示符
     */
    public static final String DEFAULT_PROMPT = "$ ";

    /**
     * 带 ANSI 黄色的命令提示符
     * raw string: "[33m$ [m"
     */
    public static final String COLOR_PROMPT = Ansi.ansi().fg(Ansi.Color.YELLOW).a(DEFAULT_PROMPT).reset().toString();

    /**
     * 命令执行耗时变量名（用于结果 JSON 等）
     */
    public static final String COST_VARIABLE = "cost";

    /** 命令历史持久化文件路径：{@code ~/.arthas/history} */
    public static final String CMD_HISTORY_FILE = System.getProperty("user.home") + File.separator + ".arthas" + File.separator + "history";

    /**
     * 当前 attach 目标进程的 PID
     */
    public static final String PID = PidUtils.currentPid();

}
