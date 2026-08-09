package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * Telnet/TTY 终端侧命令结果的抽象渲染器。
 * <p>
 * 每个具体命令对应一个 {@code ResultView} 子类，实例无状态、可复用；
 * 由 {@link ResultViewResolver} 按 {@link ResultModel} 类型分发。
 *
 * @author gongdewei 2020/3/27
 */
public abstract class ResultView<T extends ResultModel> {

    /**
     * 将模型格式化为人类可读文本并写入终端输出流。
     *
     * @param process 命令输出通道（宽度、编码等上下文）
     * @param result  命令执行产出的结果模型
     */
    public abstract void draw(CommandProcess process, T result);

    /**
     * 写入一行文本并在末尾追加换行符。
     *
     * @param process 命令输出通道
     * @param str     待输出内容（不含尾部换行）
     */
    protected void writeln(CommandProcess process, String str) {
        process.write(str).write("\n");
    }
}
