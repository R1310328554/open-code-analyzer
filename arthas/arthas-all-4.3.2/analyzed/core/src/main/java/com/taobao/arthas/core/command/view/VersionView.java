package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.VersionModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code version} 命令的终端渲染视图。
 * <p>
 * 将 {@link VersionModel#getVersion()} 中的 Arthas 版本号单行输出到终端。
 *
 * @author gongdewei 2020/3/27
 */
public class VersionView extends ResultView<VersionModel> {

    @Override
    @Override
    public void draw(CommandProcess process, VersionModel result) {
        // 单行 writeln，末尾自动补换行
        writeln(process, result.getVersion());
    }

}
