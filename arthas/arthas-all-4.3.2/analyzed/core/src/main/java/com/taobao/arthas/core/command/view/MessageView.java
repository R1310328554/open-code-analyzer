package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * 通用消息结果的终端渲染视图。
 * <p>
 * 将 {@link MessageModel#getMessage()} 单行写入终端，用于命令提示、错误或简单文本反馈。
 *
 * @author gongdewei 2020/4/2
 */
public class MessageView extends ResultView<MessageModel> {
    @Override
    public void draw(CommandProcess process, MessageModel result) {
        writeln(process, result.getMessage());
    }
}
