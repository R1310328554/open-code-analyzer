package com.taobao.arthas.core.shell.command.internal;

import java.util.List;
import java.util.regex.Pattern;

import com.taobao.arthas.core.command.basic1000.GrepCommand;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.annotations.CLIConfigurator;

/**
 * Shell 管道中的 grep 过滤器，按关键字或正则匹配行并支持上下文与行号。
 * <p>
 * 由 {@link StdoutHandler#inject} 根据管道首 Token 解析 {@link GrepCommand} 参数后构造；
 * {@code -c} 计数模式返回 {@link GrepCountHandler} 实现 {@link StatisticsFunction}。
 *
 * @author beiwei30 on 12/12/2016.
 */
public class GrepHandler extends StdoutHandler {
    public static final String NAME = "grep";

    private String keyword;
    private boolean ignoreCase;
    /** 为 true 时输出不匹配行（grep -v） */

    private final boolean invertMatch;

    private final Pattern pattern;

    /** 为 true 时在每行前输出行号（grep -n） */

    private final boolean showLineNumber;

    private boolean trimEnd;

    /** 匹配行前保留的上下文行数（-B） */

    private final Integer beforeLines;
    /** 匹配行后保留的上下文行数（-A） */

    private final Integer afterLines;

    /** 最多输出多少条匹配行后停止（-m） */

    protected final Integer maxCount;

    private static CLI cli = null;

    /** 从管道 Token 解析 grep 参数并返回对应 {@link StdoutHandler} 实例 */
    public static StdoutHandler inject(List<CliToken> tokens) {
        List<String> args = StdoutHandler.parseArgs(tokens, NAME);

        GrepCommand grepCommand = new GrepCommand();
        if (cli == null) {
            cli = CLIConfigurator.define(GrepCommand.class, true);
        }
        CommandLine commandLine = cli.parse(args, true);

        try {
            CLIConfigurator.inject(commandLine, grepCommand);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        int context = grepCommand.getContext();
        int beforeLines = grepCommand.getBeforeLines();
        int afterLines = grepCommand.getAfterLines();
        if (context > 0) {
            if (beforeLines < 1) {
                beforeLines = context;
            }
            if (afterLines < 1) {
                afterLines = context;
            }
        }
        // -c 模式：仅统计匹配行数，不输出正文
        if (grepCommand.isCount()) {
            return new GrepCountHandler(grepCommand.getPattern(), grepCommand.isIgnoreCase(),
                            grepCommand.isInvertMatch(), grepCommand.isRegEx(), grepCommand.isTrimEnd(),
                            grepCommand.getMaxCount());
        }
        return new GrepHandler(grepCommand.getPattern(), grepCommand.isIgnoreCase(), grepCommand.isInvertMatch(),
                        grepCommand.isRegEx(), grepCommand.isShowLineNumber(), grepCommand.isTrimEnd(), beforeLines,
                        afterLines, grepCommand.getMaxCount());
    }

    GrepHandler(String keyword, boolean ignoreCase, boolean invertMatch, boolean regexpMode,
                    boolean showLineNumber, boolean trimEnd, int beforeLines, int afterLines, int maxCount) {
        this.ignoreCase = ignoreCase;
        this.invertMatch = invertMatch;
        this.showLineNumber = showLineNumber;
        this.trimEnd = trimEnd;
        this.beforeLines = beforeLines > 0 ? beforeLines : 0;
        this.afterLines = afterLines > 0 ? afterLines : 0;
        this.maxCount = maxCount > 0 ? maxCount : 0;
        // 正则模式：编译 Pattern；否则使用子串 contains 匹配
        if (regexpMode) {
            final int flags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
            this.pattern = Pattern.compile(keyword, flags);
        } else {
            this.pattern = null;
        }
        this.keyword = ignoreCase ? keyword.toLowerCase() : keyword;
    }

    @Override
    public String apply(String input) {
        StringBuilder output = new StringBuilder();
        String[] lines = input.split("\n");
        int continueCount = 0;
        int lastStartPos = 0;
        int lastContinueLineNum = -1;
        int matchCount = 0;
        for (int lineNum = 0; lineNum < lines.length;) {
            String line = null;
            if (this.trimEnd) {
                line = StringUtils.stripEnd(lines[lineNum], null);
            } else {
                line = lines[lineNum];
            }
            lineNum++;

            // 命中：处理 -B/-A 上下文并追加当前行
            if (isSelectedLine(line)) {
                matchCount++;
                if (beforeLines > continueCount) {
                    int n = lastContinueLineNum == -1 ? (beforeLines >= lineNum ? 1 : lineNum - beforeLines)
                                    : lineNum - beforeLines - continueCount;
                    if (n >= lastContinueLineNum || lastContinueLineNum == -1) {
                        StringBuilder beforeSb = new StringBuilder();
                        for (int i = n; i < lineNum; i++) {
                            appendLine(beforeSb, i, lines[i - 1]);
                        }
                        output.insert(lastStartPos, beforeSb);
                    }
                } // end handle before lines

                lastStartPos = output.length();
                appendLine(output, lineNum, line);

                if (afterLines > continueCount) {
                    int last = lineNum + afterLines - continueCount;
                    if (last > lines.length) {
                        last = lines.length;
                    }
                    for (int i = lineNum; i < last; i++) {
                        appendLine(output, i + 1, lines[i]);
                        lineNum++;
                        continueCount++;
                        lastStartPos = output.length();
                    }
                } // end handle afterLines

                continueCount++;
                if (maxCount > 0 && matchCount >= maxCount) {
                    break;
                }
            } else { // 未命中：重置连续上下文计数

                if (continueCount > 0) {
                    lastContinueLineNum = lineNum - 1;
                    continueCount = 0;
                }
            }
        }
        final String str = output.toString();
        return str;
    }

    protected String prepareLine(String line) {
        if (this.trimEnd) {
            return StringUtils.stripEnd(line, null);
        }
        return line;
    }

    /** 判断一行是否应被选中（含 invertMatch 取反逻辑） */
    protected boolean isSelectedLine(String line) {
        final boolean match;
        if (pattern == null) {
            match = (ignoreCase ? line.toLowerCase() : line).contains(keyword);
        } else {
            match = pattern.matcher(line).find();
        }
        return invertMatch != match;
    }

    protected void appendLine(StringBuilder output, int lineNum, String line) {
        if (showLineNumber) {
            output.append(lineNum).append(':');
        }
        output.append(line).append('\n');
    }

    /** grep -c：累积匹配行数，最终通过 {@link StatisticsFunction#result()} 输出 */
    private static class GrepCountHandler extends GrepHandler implements StatisticsFunction {

        private int count;

        GrepCountHandler(String keyword, boolean ignoreCase, boolean invertMatch, boolean regexpMode,
                        boolean trimEnd, int maxCount) {
            super(keyword, ignoreCase, invertMatch, regexpMode, false, trimEnd, 0, 0, maxCount);
        }

        @Override
        public String apply(String input) {
            if (input == null) {
                return null;
            }

            String[] lines = input.split("\n");
            for (String rawLine : lines) {
                if (maxCount > 0 && count >= maxCount) {
                    break;
                }
                String line = prepareLine(rawLine);
                if (isSelectedLine(line)) {
                    count++;
                }
            }
            return null;
        }

        @Override
        public String result() {
            return count + "\n";
        }
    }
}
