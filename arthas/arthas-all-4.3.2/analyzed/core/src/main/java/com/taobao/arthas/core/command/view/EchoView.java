package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.EchoModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code echo} 命令的终端渲染视图：将用户输入或管道内容原样回显到终端。
 *
 * @author gongdewei 2020/5/11
 */
public class EchoView extends ResultView<EchoModel> {
    /** 写出 echo 内容并换行 */
    @Override
    public void draw(CommandProcess process, EchoModel result) {
        process.write(result.getContent()).write("\n");
    }
}
