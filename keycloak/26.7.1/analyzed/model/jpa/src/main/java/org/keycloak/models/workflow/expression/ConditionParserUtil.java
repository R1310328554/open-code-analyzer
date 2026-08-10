package org.keycloak.models.workflow.expression;

/**
 * 条件表达式解析辅助工具：提取并反转义条件调用参数。
 */
final class ConditionParserUtil {

    private ConditionParserUtil() {
        throw new UnsupportedOperationException("Static utility class");
    }

    /** 从 parameter 语法节点提取参数字符串；无参数时返回 {@code null}。 */
    static String extractParameter(BooleanConditionParser.ParameterContext paramCtx) {
        // 情况 1：未使用括号，如 "user-logged-in"
        // 情况 2：空括号，如 "user-logged-in()"
        if (paramCtx == null || paramCtx.ParameterText() == null) {
            return null;
        }

        // 情况 3：带参数，如 "has-role(param)"
        String rawText = paramCtx.ParameterText().getText();
        return unEscapeParameter(rawText);
    }


    /**
     * 语法定义的转义序列为 {@code \)} 与 {@code \\}。
     *
     * @param rawText ParameterText 词法单元的原始文本。
     * @return 反转义后的参数字符串。
     */
    static String unEscapeParameter(String rawText) {
        // 将 \) 还原为 )，\\ 还原为 \
        // 注意：replaceAll 使用正则，反斜杠需双重转义
        return rawText.replace("\\)", ")")
                .replace("\\\\", "\\");
    }
}
