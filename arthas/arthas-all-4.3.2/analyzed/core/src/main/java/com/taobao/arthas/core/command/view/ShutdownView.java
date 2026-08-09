package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ShutdownModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code shutdown} 命令的终端渲染视图：输出关闭 Arthas Agent 的提示信息。
 * <p>
 * 内容由 {@link ShutdownModel#getMessage()} 决定，通常为成功/失败说明。
 *
 * @author gongdewei 2020/6/22
 */
public class ShutdownView extends ResultView<ShutdownModel> {
    @Override
    public void draw(CommandProcess process, ShutdownModel result) {
        process.write(result.getMessage()).write("\n");
    }
}
