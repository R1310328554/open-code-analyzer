package com.taobao.arthas.core.shell.cli;

/**
 * CLI 长/短选项值的自定义补全处理器。
 * <p>
 * 当用户输入 {@code --option value} 且光标位于 value 位置时，
 * {@link CompletionUtils#completeOptions(Completion, java.util.List)} 按 token 匹配并委派本接口。
 *
 * @author hengyunabc 2021-04-29
 */
public interface OptionCompleteHandler {
    /** 判断给定 token 是否为该处理器负责的选项名（如 {@code --classPattern}） */
    boolean matchName(String token);

    /** 执行选项值补全；返回 true 表示已处理完毕 */
    boolean complete(Completion completion);
}
