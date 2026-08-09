package com.taobao.arthas.core.command.view;

import com.sun.management.VMOption;
import com.taobao.arthas.core.command.model.VMOptionModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.text.Decoration;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

import static com.taobao.text.ui.Element.label;

/**
 * {@code vmoption} 命令的终端渲染视图。
 * <p>
 * 查询模式输出 JVM 诊断选项四列表格（KEY/VALUE/ORIGIN/WRITEABLE）；
 * 修改模式展示 {@link ChangeResultVO} 修改前后对比。
 *
 * @author gongdewei 2020/4/15
 */
public class VMOptionView extends ResultView<VMOptionModel> {

    @Override
    public void draw(CommandProcess process, VMOptionModel result) {
        // 列表查询：渲染全部 VMOption
            process.write(renderVMOptions(result.getVmOptions(), process.width()));
        } else if (result.getChangeResult() != null) {
            // set 模式：展示 NAME/BEFORE/AFTER 对比表
            TableElement table = ViewRenderUtil.renderChangeResult(result.getChangeResult());
            process.write(RenderUtil.render(table, process.width()));
        }
    }

    /** 将诊断 VM 选项列表渲染为四列表格字符串 */
    private static String renderVMOptions(List<VMOption> diagnosticOptions, int width) {
        TableElement table = new TableElement(1, 1, 1, 1).leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("KEY").style(Decoration.bold.bold()),
                label("VALUE").style(Decoration.bold.bold()),
                label("ORIGIN").style(Decoration.bold.bold()),
                label("WRITEABLE").style(Decoration.bold.bold()));

        for (VMOption option : diagnosticOptions) {
            table.row(option.getName(), option.getValue(), "" + option.getOrigin(), "" + option.isWriteable());
        }

        return RenderUtil.render(table, width);
    }
}
