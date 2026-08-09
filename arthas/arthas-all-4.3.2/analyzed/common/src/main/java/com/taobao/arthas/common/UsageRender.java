package com.taobao.arthas.common;

/**
 * 命令用法文本渲染：在启用 ANSI 时为标题行着色，提升 CLI 可读性。
 *
 * @author hengyunabc 2018-11-22
 */
public class UsageRender {

    private UsageRender() {
    }

    /**
     * 渲染 usage 字符串；彩色模式下高亮 {@code Usage: } 行及非缩进且以 {@code :} 结尾的节标题。
     *
     * @param usage 原始 usage 文本
     */
    public static String render(String usage) {
        if (AnsiLog.enableColor()) {
            StringBuilder sb = new StringBuilder(1024);
            String lines[] = usage.split("\\r?\\n");
            for (String line : lines) {
                if (line.startsWith("Usage: ")) {
                    sb.append(AnsiLog.green("Usage: "));
                    sb.append(line.substring("Usage: ".length()));
                } else if (!line.startsWith(" ") && line.endsWith(":")) {
                    sb.append(AnsiLog.green(line));
                } else {
                    sb.append(line);
                }
                sb.append('\n');
            }
            return sb.toString();
        } else {
            return usage;
        }
    }
}
