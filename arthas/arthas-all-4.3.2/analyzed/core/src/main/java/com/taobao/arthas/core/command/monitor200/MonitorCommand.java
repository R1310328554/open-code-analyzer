package com.taobao.arthas.core.command.monitor200;


import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * {@code monitor} 命令：对匹配类/方法做字节码增强，周期性输出调用 QPS、成功率、平均 RT 等。
 * <p>
 * 继承 {@link EnhancerCommand} 完成类匹配与增强；监听器为 {@link MonitorAdviceListener}。
 * 支持条件表达式过滤、{@code -c} 统计周期、{@code -n} 输出次数上限、{@code -b} 前置条件求值。
 *
 * @author vlinux
 */
@Name("monitor")
@Summary("Monitor method execution statistics, e.g. total/success/failure count, average rt, fail rate, etc. ")
@Description("\nExamples:\n" +
        "  monitor org.apache.commons.lang.StringUtils isBlank\n" +
        "  monitor org.apache.commons.lang.StringUtils isBlank -c 5\n" +
        "  monitor org.apache.commons.lang.StringUtils isBlank params[0]!=null\n" +
        "  monitor -b org.apache.commons.lang.StringUtils isBlank params[0]!=null\n" +
        "  monitor -E org\\.apache\\.commons\\.lang\\.StringUtils isBlank\n" +
        Constants.WIKI + Constants.WIKI_HOME + "monitor")
public class MonitorCommand extends EnhancerCommand {

    /** 类名匹配模式（通配或 -E 正则） */
    private String classPattern;
    /** 方法名匹配模式 */
    private String methodPattern;
    /** OGNL 条件表达式，不满足的调用不参与统计 */
    private String conditionExpress;
    /** 统计输出周期（秒），默认 60 */
    private int cycle = 60;
    private boolean isRegEx = false;
    private int numberOfLimit = 100;
    private boolean isBefore = false;

    @Argument(argName = "class-pattern", index = 0)
    @Description("Path and classname of Pattern Matching")
    public void setClassPattern(String classPattern) {
        this.classPattern = StringUtils.normalizeClassName(classPattern);
    }

    @Argument(argName = "method-pattern", index = 1)
    @Description("Method of Pattern Matching")
    public void setMethodPattern(String methodPattern) {
        this.methodPattern = methodPattern;
    }

    @Argument(argName = "condition-express", index = 2, required = false)
    @Description(Constants.CONDITION_EXPRESS)
    public void setConditionExpress(String conditionExpress) {
        this.conditionExpress = conditionExpress;
    }

    @Option(shortName = "c", longName = "cycle")
    @Description("The monitor interval (in seconds), 60 seconds by default")
    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    @Option(shortName = "E", longName = "regex", flag = true)
    @Description("Enable regular expression to match (wildcard matching by default)")
    public void setRegEx(boolean regEx) {
        isRegEx = regEx;
    }

    @Option(shortName = "n", longName = "limits")
    @Description("Threshold of execution times")
    public void setNumberOfLimit(int numberOfLimit) {
        this.numberOfLimit = numberOfLimit;
    }

    @Option(shortName = "b", longName = "before", flag = true)
    @Description("Evaluate the condition-express before method invoke")
    public void setBefore(boolean before) {
        isBefore = before;
    }

    public String getClassPattern() {
        return classPattern;
    }

    public String getMethodPattern() {
        return methodPattern;
    }

    public String getConditionExpress() {
        return conditionExpress;
    }

    public int getCycle() {
        return cycle;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public int getNumberOfLimit() {
        return numberOfLimit;
    }

    public boolean isBefore() {
        return isBefore;
    }

    @Override
    protected Matcher getClassNameMatcher() {
        if (classNameMatcher == null) {
            classNameMatcher = SearchUtils.classNameMatcher(getClassPattern(), isRegEx());
        }
        return classNameMatcher;
    }

    @Override
    protected Matcher getClassNameExcludeMatcher() {
        if (classNameExcludeMatcher == null && getExcludeClassPattern() != null) {
            classNameExcludeMatcher = SearchUtils.classNameMatcher(getExcludeClassPattern(), isRegEx());
        }
        return classNameExcludeMatcher;
    }

    @Override
    protected Matcher getMethodNameMatcher() {
        if (methodNameMatcher == null) {
            methodNameMatcher = SearchUtils.classNameMatcher(getMethodPattern(), isRegEx());
        }
        return methodNameMatcher;
    }

    @Override
    /** 创建监听器并注册 suspend/resume 回调以控制定时器生命周期 */
    /** 创建监听器并注册 suspend/resume 回调以控制定时器生命周期 */
    @Override
    protected AdviceListener getAdviceListener(CommandProcess process) {
        final AdviceListener listener = new MonitorAdviceListener(this, process, GlobalOptions.verbose || this.verbose);
        /*
         * 通过handle回调，在suspend时停止timer，resume时重启timer
         */
        process.suspendHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                listener.destroy();
            }
        });
        process.resumeHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                listener.create();
            }
        });
        return listener;
    }
}
