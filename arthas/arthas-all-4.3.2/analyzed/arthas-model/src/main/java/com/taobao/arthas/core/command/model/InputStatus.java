package com.taobao.arthas.core.command.model;

/**
 * Web UI 命令输入区域的可用状态枚举。
 *
 * @author gongdewei 2020/4/14
 */
public enum InputStatus {
    /**
     * 允许输入新命令。
     */
    ALLOW_INPUT,

    /**
     * 允许中断当前正在运行的任务。
     */
    ALLOW_INTERRUPT,

    /**
     * 禁止输入新命令，也不允许中断。
     */
    DISABLED
}
