package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.RowAffectModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * 通用「行影响」结果的终端渲染视图。
 * <p>
 * 单行输出 {@link RowAffectModel#affect()} 统计摘要，供多种命令复用
 * （如批量操作后的 matched/affected 计数）。
 *
 * @author gongdewei 2020/4/8
 */
public class RowAffectView extends ResultView<RowAffectModel> {
    @Override
    public void draw(CommandProcess process, RowAffectModel result) {
        process.write(result.affect() + "\n");
    }
}
