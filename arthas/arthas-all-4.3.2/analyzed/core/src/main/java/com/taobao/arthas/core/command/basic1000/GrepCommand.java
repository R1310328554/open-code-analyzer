package com.taobao.arthas.core.command.basic1000;

import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.DefaultValue;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 管道 grep 命令的参数载体：单独执行时提示仅用于管道；实际过滤逻辑在 {@link com.taobao.arthas.core.shell.command.internal.GrepHandler}。
 *
 * @see com.taobao.arthas.core.shell.command.internal.GrepHandler
 */
@Name("grep")
@Summary("grep command for pipes." )
@Description(Constants.EXAMPLE +
        " sysprop | grep java \n" +
        " sysprop | grep java -n\n" +
        " sysenv | grep -v JAVA\n" +
        " sysenv | grep -e \"(?i)(JAVA|sun)\" -m 3  -C 2\n" +
        " sysenv | grep JAVA -A2 -B3\n" +
        " thread --all | grep -c HttpClient\n" +
        " thread | grep -m 10 -e  \"TIMED_WAITING|WAITING\"\n"
        + Constants.WIKI + Constants.WIKI_HOME + "grep")
public class GrepCommand extends AnnotatedCommand {
    /** 匹配模式（字面量或正则，取决于 -e 选项） */
    private String pattern;
    /** 是否忽略大小写（-i） */
    private boolean ignoreCase;

    /** 反向匹配：输出不匹配的行（-v） */
    private boolean invertMatch;

    /** 是否将 pattern 视为正则表达式（-e） */
    private boolean isRegEx = false;

    /** 输出时附带行号（-n） */
    private boolean showLineNumber = false;

    /** 仅统计匹配行数（-c） */
    private boolean count = false;

    /** 是否去除行尾空白，默认 true */
    private boolean trimEnd;

    /** 匹配行前显示的上下文行数（-B） */
    private int beforeLines;

    /** 匹配行后显示的上下文行数（-A） */
    private int afterLines;

    /** 匹配行前后各显示的上下文行数（-C） */
    private int context;

    /** 最多输出多少条匹配行（-m） */
    private int maxCount;

    @Argument(index = 0, argName = "pattern", required = true)
    @Description("Pattern")
    public void setOptionName(String pattern) {
        this.pattern = pattern;
    }

    @Option(shortName = "e", longName = "regex", flag = true)
    @Description("Enable regular expression to match")
    public void setRegEx(boolean regEx) {
        isRegEx = regEx;
    }

    @Option(shortName = "i", longName = "ignore-case", flag = true)
    @Description("Perform case insensitive matching.  By default, grep is case sensitive.")
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Option(shortName = "v", longName = "invert-match", flag = true)
    @Description("Select non-matching lines")
    public void setInvertMatch(boolean invertMatch) {
        this.invertMatch = invertMatch;
    }

    @Option(shortName = "n", longName = "line-number", flag = true)
    @Description("Print line number with output lines")
    public void setShowLineNumber(boolean showLineNumber) {
        this.showLineNumber = showLineNumber;
    }

    @Option(shortName = "c", longName = "count", flag = true, acceptValue = false)
    @Description("Print only a count of selected lines")
    public void setCount(boolean count) {
        this.count = count;
    }

    @Option(longName = "trim-end", flag = false)
    @DefaultValue("true")
    @Description("Remove whitespaces at the end of the line, default value true")
    public void setTrimEnd(boolean trimEnd) {
        this.trimEnd = trimEnd;
    }

    @Option(shortName = "B", longName = "before-context")
    @Description("Print NUM lines of leading context)")
    public void setBeforeLines(int beforeLines) {
        this.beforeLines = beforeLines;
    }

    @Option(shortName = "A", longName = "after-context")
    @Description("Print NUM lines of trailing context)")
    public void setAfterLines(int afterLines) {
        this.afterLines = afterLines;
    }

    @Option(shortName = "C", longName = "context")
    @Description("Print NUM lines of output context)")
    public void setContext(int context) {
        this.context = context;
    }

    @Option(shortName = "m", longName = "max-count")
    @Description("stop after NUM selected lines)")
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isInvertMatch() {
        return invertMatch;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public boolean isShowLineNumber() {
        return showLineNumber;
    }

    public boolean isCount() {
        return count;
    }

    public boolean isTrimEnd() {
        return trimEnd;
    }

    public int getBeforeLines() {
        return beforeLines;
    }

    public int getAfterLines() {
        return afterLines;
    }

    public int getContext() {
        return context;
    }

    public int getMaxCount() {
        return maxCount;
    }

    @Override
    public void process(CommandProcess process) {
        process.end(-1, "The grep command only for pipes. See 'grep --help'\n");
    }
}
