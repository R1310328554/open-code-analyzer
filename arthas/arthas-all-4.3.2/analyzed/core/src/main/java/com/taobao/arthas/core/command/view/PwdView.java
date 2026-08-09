package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.PwdModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code pwd} 命令的终端渲染视图：输出 Arthas Shell 当前工作目录。
 * <p>
 * 单行输出 {@link PwdModel#getWorkingDir()}，与 Unix {@code pwd} 行为一致。
 *
 * @author gongdewei 2020/5/11
 */
public class PwdView extends ResultView<PwdModel> {
    @Override
    public void draw(CommandProcess process, PwdModel result) {
        process.write(result.getWorkingDir()).write("\n");
    }
}
