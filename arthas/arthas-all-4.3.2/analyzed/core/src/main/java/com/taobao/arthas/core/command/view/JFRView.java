package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.JFRModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code jfr} 命令的终端渲染视图。
 * <p>
 * 将 {@link JFRModel#getJfrOutput()} 中的 JFR 诊断文本原样写入终端，
 * 不做额外排版（内容由 JFRCommand 侧生成）。
 *
 * @author longxu 2022/7/25
 */
public class JFRView extends ResultView<JFRModel>{
    @Override
    public void draw(CommandProcess process, JFRModel result) {
        // 单行 writeln，避免末尾缺少换行
        writeln(process, result.getJfrOutput());
    }
}
