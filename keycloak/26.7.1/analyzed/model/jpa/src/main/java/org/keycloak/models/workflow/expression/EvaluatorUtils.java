package org.keycloak.models.workflow.expression;

import java.util.stream.Collectors;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.workflow.WorkflowInvalidStateException;
import org.keycloak.models.workflow.expression.BooleanConditionParser.EvaluatorContext;
import org.keycloak.utils.StringUtil;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * 工作流布尔表达式解析与校验工具：构建 {@link EvaluatorContext}，并限制长度与嵌套深度。
 */
public class EvaluatorUtils {

    /** 表达式允许的最大字符数。 */
    public static final int MAX_EXPRESSION_LENGTH = 2048;
    /** 分组括号允许的最大嵌套层数。 */
    public static final int MAX_EXPRESSION_DEPTH = 10;

    /**
     * 将字符串表达式解析为 {@link EvaluatorContext}；无效时抛出 {@link WorkflowInvalidStateException} 并附带错误详情。
     *
     * @param expression 待解析的布尔表达式
     * @return 表示已解析表达式的 EvaluatorContext
     */
    public static EvaluatorContext createEvaluatorContext(String expression) {
        if (StringUtil.isBlank(expression)) {
            throw new WorkflowInvalidStateException("Expression cannot be null or empty");
        }
        if (expression.length() > MAX_EXPRESSION_LENGTH) {
            throw new WorkflowInvalidStateException(
                    "Expression exceeds maximum allowed length of %d characters".formatted(MAX_EXPRESSION_LENGTH));
        }
        validateExpressionDepth(expression);

        // 完整校验需实际解析表达式
        CharStream charStream = CharStreams.fromString(expression);
        BooleanConditionLexer lexer = new BooleanConditionLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        BooleanConditionParser parser = new BooleanConditionParser(tokens);

        // 替换默认错误监听器，收集格式错误表达式的全部解析错误
        ErrorListener errorListener = new ErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        // 解析并检查语法错误
        EvaluatorContext context = parser.evaluator();
        if (errorListener.hasErrors()) {
            String lineSeparator = System.lineSeparator();
            String errorDetails = errorListener.getErrorMessages().stream()
                    .map(error -> "- " + error)
                    .collect(Collectors.joining(lineSeparator));

            throw new WorkflowInvalidStateException(String.format("Invalid expression: %s%sError details:%s%s",
                    expression, lineSeparator, lineSeparator, errorDetails));
        }
        return context;
    }

    /**
     * 校验分组括号的嵌套深度不超过 {@link #MAX_EXPRESSION_DEPTH}。
     * 在 ANTLR 解析前执行，避免过深解析树引发 {@code StackOverflowError}。
     * 条件调用的括号（如 {@code has-role(admin)}）不计入嵌套，因其不触发递归语法规则。
     */
    private static void validateExpressionDepth(String expression) {
        int depth = 0;
        int maxDepth = 0;
        boolean wasIdentChar = false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            // 条件调用的括号，如 has-role(admin) —— 前面是标识符字符，跳过至匹配的 ')'
            if (c == '(' && wasIdentChar) {
                for (i++; i < expression.length(); i++) {
                    if (expression.charAt(i) == '\\' && i + 1 < expression.length()) {
                        i++; // 跳过转义字符
                        continue;
                    }
                    if (expression.charAt(i) == ')') {
                        break;
                    }
                }
                wasIdentChar = false;
                continue;
            }
            // 分组括号 —— 前面不是标识符字符，计入嵌套层数
            if (c == '(') {
                maxDepth = Math.max(maxDepth, ++depth);
                wasIdentChar = false;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
                wasIdentChar = false;
            } else {
                // 判断下一个 '(' 属于条件调用还是分组括号
                wasIdentChar = Character.isLetterOrDigit(c) || c == '-' || c == '_';
            }
        }
        if (maxDepth > MAX_EXPRESSION_DEPTH) {
            throw new WorkflowInvalidStateException(
                    "Expression exceeds maximum allowed nesting depth of %d".formatted(MAX_EXPRESSION_DEPTH));
        }
    }

    /**
     * 为 workflow 组件创建或复用缓存的 {@link EvaluatorContext}。
     *
     * @param workflowModel workflow 组件模型
     * @param expression   待解析的布尔表达式
     * @return 表示已解析表达式的 EvaluatorContext
     */
    public static EvaluatorContext createEvaluatorContext(ComponentModel workflowModel, String expression) {
        EvaluatorContext context = workflowModel.getNote(expression);
        if (context == null) {
            context = createEvaluatorContext(expression);
            workflowModel.setNote(expression, context);
        }
        return context;
    }
}
