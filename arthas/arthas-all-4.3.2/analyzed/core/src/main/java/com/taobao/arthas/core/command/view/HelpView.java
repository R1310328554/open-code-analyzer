package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.CommandVO;
import com.taobao.arthas.core.command.model.HelpModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.usage.StyledUsageFormatter;
import com.taobao.middleware.cli.CLI;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.Style;
import com.taobao.text.ui.Element;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;

import static com.taobao.text.ui.Element.label;
import static com.taobao.text.ui.Element.row;

/**
 * {@code help} 命令的终端渲染视图。
 * <p>
 * 无参数时输出全部命令的 NAME/DESCRIPTION 表格；指定命令名时输出该命令的详细用法
 *（选项、参数说明），宽度受 {@link CommandProcess#width()} 约束。
 *
 * @author gongdewei 2020/4/3
 */
public class HelpView extends ResultView<HelpModel> {

    @Override
    @Override
    public void draw(CommandProcess process, HelpModel result) {
        if (result.getCommands() != null) {
            // 总览模式：渲染命令列表表格
            String message = RenderUtil.render(mainHelp(result.getCommands()), process.width());
            process.write(message);
        } else if (result.getDetailCommand() != null) {
            // 单命令详情：带样式的 usage 文本
            process.write(commandHelp(result.getDetailCommand().cli(), process.width()));
        }
    }

    /** 构建命令总览表：绿色命令名 + 摘要列 */
    private static Element mainHelp(List<CommandVO> commands) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(new LabelElement("NAME").style(Style.style(Decoration.bold)), new LabelElement("DESCRIPTION"));
        for (CommandVO commandVO : commands) {
            table.add(row().add(label(commandVO.getName()).style(Style.style(Color.green))).add(label(commandVO.getSummary())));
        }
        return table;
    }

    /** 将 CLI 元数据格式化为终端宽度的 usage 字符串 */
    private static String commandHelp(CLI command, int width) {
        return StyledUsageFormatter.styledUsage(command, width);
    }
}
