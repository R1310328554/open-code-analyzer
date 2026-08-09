package com.taobao.arthas.core.command.monitor200;

import java.util.Arrays;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.view.ObjectView;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.DefaultValue;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * {@code watch} 方法观测命令：在方法 before/return/throw 切点求值 OGNL 并输出参数、返回值或异常。
 * <p>
 * 默认在方法结束后观测（-f）；-b/-s/-e 可分别指定 before/成功返回/抛异常切点；
 * {@code -M} 限制结果字节数，{@code -x} 控制对象展开深度。
 */
@Name("watch")
@Summary("Display the input/output parameter, return object, and thrown exception of specified method invocation")
@Description(Constants.EXPRESS_DESCRIPTION + "\nExamples:\n" +
        "  watch org.apache.commons.lang.StringUtils isBlank\n" +
        "  watch org.apache.commons.lang.StringUtils isBlank '{params, target, returnObj, throwExp}' -x 2\n" +
        "  watch *StringUtils isBlank params[0] params[0].length==1\n" +
        "  watch *StringUtils isBlank params '#cost>100'\n" +
        "  watch -f *StringUtils isBlank params\n" +
        "  watch *StringUtils isBlank params[0]\n" +
        "  watch -E -b org\\.apache\\.commons\\.lang\\.StringUtils isBlank params[0]\n" +
        "  watch javax.servlet.Filter * --exclude-class-pattern com.demo.TestFilter\n" +
        "  watch OuterClass$InnerClass\n" +
        Constants.WIKI + Constants.WIKI_HOME + "watch")
public class WatchCommand extends EnhancerCommand {

    /** 目标类名匹配模式 */
    private String classPattern;
    /** 目标方法名匹配模式 */
    private String methodPattern;
    /** OGNL 观测表达式，默认 {@code {params, target, returnObj}} */
    private String express;
    /** OGNL 条件表达式，满足时才输出 */
    private String conditionExpress;
    /** -b：方法进入前观测 */
    private boolean isBefore = false;
    /** -f：方法退出时观测（默认行为由 CLI 解析决定） */
    private boolean isFinish = false;
    /** -e：抛异常后观测 */
    private boolean isException = false;
    /** -s：正常返回后观测 */
    private boolean isSuccess = false;
    /** 对象展开深度（-x） */
    private Integer expand = 1;
    /** 结果字节上限（-M） */
    private Integer sizeLimit;
    /** 是否启用正则匹配（-E） */
    private boolean isRegEx = false;
    /** 最大观测输出次数（-n） */
    private int numberOfLimit = 100;
    
    @Argument(index = 0, argName = "class-pattern")
    @Description("The full qualified class name you want to watch")
    public void setClassPattern(String classPattern) {
        this.classPattern = StringUtils.normalizeClassName(classPattern);
    }

    @Argument(index = 1, argName = "method-pattern")
    @Description("The method name you want to watch")
    public void setMethodPattern(String methodPattern) {
        this.methodPattern = methodPattern;
    }

    @Argument(index = 2, argName = "express", required = false)
    @DefaultValue("{params, target, returnObj}")
    @Description("The content you want to watch, written by ognl. Default value is '{params, target, returnObj}'\n" + Constants.EXPRESS_EXAMPLES)
    public void setExpress(String express) {
        this.express = express;
    }

    @Argument(index = 3, argName = "condition-express", required = false)
    @Description(Constants.CONDITION_EXPRESS)
    public void setConditionExpress(String conditionExpress) {
        this.conditionExpress = conditionExpress;
    }

    @Option(shortName = "b", longName = "before", flag = true)
    @Description("Watch before invocation")
    public void setBefore(boolean before) {
        isBefore = before;
    }

    @Option(shortName = "f", longName = "finish", flag = true)
    @Description("Watch after invocation, enable by default")
    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    @Option(shortName = "e", longName = "exception", flag = true)
    @Description("Watch after throw exception")
    public void setException(boolean exception) {
        isException = exception;
    }

    @Option(shortName = "s", longName = "success", flag = true)
    @Description("Watch after successful invocation")
    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    @Option(shortName = "M", longName = "sizeLimit")
    @Description("Upper size limit in bytes for the result (must be greater than 0, default value comes from options object-size-limit)")
    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Option(shortName = "x", longName = "expand")
    @Description("Expand level of object (1 by default), the max value is " + ObjectView.MAX_DEEP)
    public void setExpand(Integer expand) {
        this.expand = expand;
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

    @Override
    @Option(shortName = "c", longName = "classloader")
    @Description("The hash code of the special class's classLoader")
    public void setHashCode(String hashCode) {
        super.setHashCode(hashCode);
    }

    public String getClassPattern() {
        return classPattern;
    }

    public String getMethodPattern() {
        return methodPattern;
    }

    public String getExpress() {
        return express;
    }

    public String getConditionExpress() {
        return conditionExpress;
    }

    public boolean isBefore() {
        return isBefore;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public boolean isException() {
        return isException;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Integer getExpand() {
        return expand;
    }

    public Integer getSizeLimit() {
        return sizeLimit;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public int getNumberOfLimit() {
        return numberOfLimit;
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

    /** 校验 sizeLimit 后进入父类 enhance 流程 */
    @Override
    public void process(CommandProcess process) {
        String validateError = validateSizeLimit(sizeLimit);
        if (validateError != null) {
            process.end(-1, validateError);
            return;
        }
        super.process(process);
    }

    @Override
    protected AdviceListener getAdviceListener(CommandProcess process) {
        return new WatchAdviceListener(this, process, GlobalOptions.verbose || this.verbose);
    }

    @Override
    protected void completeArgument3(Completion completion) {
        CompletionUtils.complete(completion, Arrays.asList(EXPRESS_EXAMPLES));
    }

    /** sizeLimit 必须大于 0，否则返回错误提示 */
    static String validateSizeLimit(Integer sizeLimit) {
        if (sizeLimit != null && sizeLimit.intValue() <= 0) {
            return "sizeLimit must be greater than 0.";
        }
        return null;
    }
}
