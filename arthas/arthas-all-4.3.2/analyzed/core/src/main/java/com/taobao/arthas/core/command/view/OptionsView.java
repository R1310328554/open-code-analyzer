package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.OptionVO;
import com.taobao.arthas.core.command.model.OptionsModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.Collection;

import static com.taobao.text.ui.Element.label;

/**
 * {@code options} 命令的终端渲染视图。
 * <p>
 * 无参数时列出全部 Arthas 全局选项；set 子命令后展示变更结果表
 *（{@link ViewRenderUtil#renderChangeResult}）。
 *
 * @author gongdewei 2020/4/15
 */
public class OptionsView extends ResultView<OptionsModel> {
    @Override
    public void draw(CommandProcess process, OptionsModel result) {
        // 查询模式：LEVEL/TYPE/NAME/VALUE/SUMMARY/DESCRIPTION 六列表
        if (result.getOptions() != null) {
            process.write(RenderUtil.render(drawShowTable(result.getOptions()), process.width()));
        } else if (result.getChangeResult() != null) {
            // set 模式：展示修改前后对比
            TableElement table = ViewRenderUtil.renderChangeResult(result.getChangeResult());
            process.write(RenderUtil.render(table, process.width()));
        }
    }

    /** 构建 options 列表表格元素 */
    private Element drawShowTable(Collection<OptionVO> options) {
        TableElement table = new TableElement(1, 1, 2, 1, 3, 6)
                .leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("LEVEL").style(Decoration.bold.bold()),
                label("TYPE").style(Decoration.bold.bold()),
                label("NAME").style(Decoration.bold.bold()),
                label("VALUE").style(Decoration.bold.bold()),
                label("SUMMARY").style(Decoration.bold.bold()),
                label("DESCRIPTION").style(Decoration.bold.bold()));

        for (final OptionVO optionVO : options) {
            table.row("" + optionVO.getLevel(),
                    optionVO.getType(),
                    optionVO.getName(),
                    optionVO.getValue(),
                    optionVO.getSummary(),
                    optionVO.getDescription());
        }
        return table;
    }

}
