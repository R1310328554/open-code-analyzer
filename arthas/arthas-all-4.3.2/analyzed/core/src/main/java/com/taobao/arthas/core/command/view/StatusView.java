package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.StatusModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code status} 命令的终端渲染视图：输出 Agent 运行状态文本。
 * <p>
 * 仅当 {@link StatusModel#getMessage()} 非空时写入一行；空消息时不输出，
 * 避免无意义的空白行。
 *
 * @author gongdewei 2020/3/27
 */
public class StatusView extends ResultView<StatusModel> {

    @Override
    public void draw(CommandProcess process, StatusModel result) {
        if (result.getMessage() != null) {
            writeln(process, result.getMessage());
        }
    }

}
