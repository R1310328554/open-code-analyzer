package com.taobao.arthas.core.util.usage;

import com.taobao.middleware.cli.Argument;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.Option;
import com.taobao.middleware.cli.UsageMessageFormatter;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.Style;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;


import java.util.Collections;

import static com.taobao.text.ui.Element.row;
import static com.taobao.text.ui.Element.label;

/**
 * 带样式（颜色、粗体）的 CLI 用法说明格式化器，用于命令帮助输出。
 *
 * @author ralf0131 2016-12-14 22:16.
 */
public class StyledUsageFormatter extends UsageMessageFormatter {

    /** 高亮文字使用的颜色 */
    private Color fontColor;

    /**
     * @param fontColor 标题、选项名等高亮元素的颜色
     */
    public StyledUsageFormatter(Color fontColor) {
        this.fontColor = fontColor;
    }

    /**
     * 生成指定 CLI 的带样式用法说明字符串。
     *
     * @param cli   命令定义
     * @param width 渲染宽度
     * @return 格式化后的用法文本；cli 为 null 时返回空串
     */
    public static String styledUsage(CLI cli, int width) {
        if(cli == null) {
            return "";
        }
        StringBuilder usageBuilder = new StringBuilder();
        UsageMessageFormatter formatter = new StyledUsageFormatter(Color.green);
        formatter.setWidth(width);
        cli.usage(usageBuilder, formatter);
        return usageBuilder.toString();
    }

    /**
     * 将 USAGE、SUMMARY、DESCRIPTION、OPTIONS 等段落渲染为表格并写入 builder。
     */
    @Override
    public void usage(StringBuilder builder, String prefix, CLI cli) {

        TableElement table = new TableElement(1, 2).leftCellPadding(1).rightCellPadding(1);

        table.add(row().add(label("USAGE:").style(getHighlightedStyle())));
        table.add(row().add(label(computeUsageLine(prefix, cli))));
        table.add(row().add(""));
        table.add(row().add(label("SUMMARY:").style(getHighlightedStyle())));
        table.add(row().add(label("  " + cli.getSummary())));

        if (cli.getDescription() != null) {
            String[] descLines = cli.getDescription().split("\\n");
            for (String line: descLines) {
                if (shouldBeHighlighted(line)) {
                    table.add(row().add(label(line).style(getHighlightedStyle())));
                } else {
                    table.add(row().add(label(line)));
                }
            }
        }

        if (!cli.getOptions().isEmpty() || !cli.getArguments().isEmpty()) {
            table.add(row().add(""));
            table.row(label("OPTIONS:").style(getHighlightedStyle()));
            for (Option option : cli.getOptions()) {
                StringBuilder optionSb = new StringBuilder(32);

                // 短选项名
                if (isNullOrEmpty(option.getShortName())) {
                    optionSb.append("   ");
                } else {
                    optionSb.append('-').append(option.getShortName());
                    if (isNullOrEmpty(option.getLongName())) {
                        optionSb.append(' ');
                    } else {
                        optionSb.append(',');
                    }
                }
                // 长选项名
                if (!isNullOrEmpty(option.getLongName())) {
                    optionSb.append(" --").append(option.getLongName());
                }

                if (option.acceptValue()) {
                    optionSb.append(" <value>");
                }

                table.add(row().add(label(optionSb.toString()).style(getHighlightedStyle()))
                                .add(option.getDescription()));
            }

            for (Argument argument: cli.getArguments()) {
                table.add(row().add(label("<" + argument.getArgName() + ">").style(getHighlightedStyle()))
                        .add(argument.getDescription()));
            }
        }

        builder.append(RenderUtil.render(table, getWidth()));
    }

    /** @return 粗体 + 指定颜色的复合样式 */
    private Style.Composite getHighlightedStyle() {
        return Style.style(Decoration.bold, fontColor);
    }

    /**
     * 拼接单行命令用法：前缀 + 命令名 + 选项占位 + 参数占位。
     */
    public String computeUsageLine(String prefix, CLI cli) {
        // 初始化缓冲区
        StringBuilder buff;
        if (prefix == null) {
            buff = new StringBuilder("  ");
        } else {
            buff = new StringBuilder("  ").append(prefix);
            if (!prefix.endsWith(" ")) {
                buff.append(" ");
            }
        }

        buff.append(cli.getName()).append(" ");

        if (getOptionComparator() != null) {
            Collections.sort(cli.getOptions(), getOptionComparator());
        }

        // 依次追加各选项占位
        for (Option option : cli.getOptions()) {
            appendOption(buff, option);
            buff.append(" ");
        }

        // 依次追加各参数占位
        for (Argument arg : cli.getArguments()) {
            appendArgument(buff, arg, arg.isRequired());
            buff.append(" ");
        }

        return buff.toString();
    }

    /** 不以空格开头的描述行视为小节标题，需要高亮 */
    private boolean shouldBeHighlighted(String line) {
        return !line.startsWith(" ");
    }

}
