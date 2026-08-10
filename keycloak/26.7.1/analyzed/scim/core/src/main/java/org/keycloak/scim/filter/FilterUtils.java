package org.keycloak.scim.filter;

import org.keycloak.utils.StringUtil;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;


/**
 * 使用 ANTLR 解析 SCIM 过滤表达式的工具类。
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class FilterUtils {

    /** 过滤表达式允许的最大字符长度。 */
    public static final int MAX_FILTER_LENGTH = 2048;
    /** 过滤表达式允许的最大嵌套深度。 */
    public static final int MAX_FILTER_DEPTH = 10;

    /**
     * 将 SCIM 过滤表达式字符串解析为抽象语法树（AST）。
     *
     * @param filterExpression 待解析的过滤表达式（RFC 7644 第 3.4.2.2 节）
     * @return 解析后的过滤器上下文（AST 根节点）
     * @throws ScimFilterException 过滤表达式存在语法错误时抛出
     */
    public static ScimFilterParser.FilterContext parseFilter(String filterExpression) {
        if (StringUtil.isBlank(filterExpression)) {
            throw new ScimFilterException("Filter expression cannot be null or empty");
        }
        if (filterExpression.length() > MAX_FILTER_LENGTH) {
            throw new ScimFilterException(
                    "Filter expression exceeds maximum allowed length of %d characters".formatted(MAX_FILTER_LENGTH));
        }
        validateFilterDepth(filterExpression);

        CharStream charStream = CharStreams.fromString(filterExpression);
        ScimFilterLexer lexer = new ScimFilterLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScimFilterParser parser = new ScimFilterParser(tokens);

        // 自定义错误监听器
        ErrorListener errorListener = new ErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        ScimFilterParser.FilterContext context = parser.filter();

        if (errorListener.hasErrors()) {
            String errors = String.join(", ", errorListener.getErrorMessages());
            throw new ScimFilterException("Invalid filter syntax: " + errors);
        }

        validateNullCompValues(context);
        return context;
    }

    /**
     * 从已解析的 {@code compValue} 节点提取比较值字符串。
     *
     * @param ctx 解析树中的比较值上下文
     * @return 提取的值；NULL 字面量返回 {@code null}
     */
    public static String extractCompValue(ScimFilterParser.CompValueContext ctx) {
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            return unescapeJsonString(raw.substring(1, raw.length() - 1));
        }
        if (ctx.TRUE() != null) return "true";
        if (ctx.FALSE() != null) return "false";
        if (ctx.NULL() != null) return null;
        if (ctx.NUMBER() != null) return ctx.NUMBER().getText();
        return null;
    }

    /**
     * 校验括号分组与值路径方括号的嵌套深度不超过 {@link #MAX_FILTER_DEPTH}。
     * <p>在 ANTLR 解析前执行，避免构建过深解析树导致 {@code StackOverflowError}。</p>
     */
    private static void validateFilterDepth(String filterExpression) {
        int depth = 0;
        int maxDepth = 0;
        boolean inString = false;
        for (int i = 0; i < filterExpression.length(); i++) {
            char c = filterExpression.charAt(i);
            // 跳过引号字符串内的字符——它们是值而非结构符号
            if (inString) {
                if (c == '\\' && i + 1 < filterExpression.length()) {
                    i++; // 跳过转义字符
                    continue;
                }
                if (c == '"') {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"' -> inString = true;
                // 括号分组与值路径方括号均会触发递归语法规则
                case '(', '[' -> maxDepth = Math.max(maxDepth, ++depth);
                case ')', ']' -> depth = Math.max(0, depth - 1);
            }
        }
        if (maxDepth > MAX_FILTER_DEPTH) {
            throw new ScimFilterException(
                    "Filter expression exceeds maximum allowed nesting depth of %d".formatted(MAX_FILTER_DEPTH));
        }
    }

    /** 校验比较运算符对 null 值的合法性（仅 {@code eq} 与 {@code ne} 允许）。 */
    private static void validateNullCompValues(ScimFilterParser.FilterContext filterCtx) {
        new ScimFilterParserBaseVisitor<Void>() {
            @Override
            public Void visitComparisonExpression(ScimFilterParser.ComparisonExpressionContext ctx) {
                if (ctx.compValue().NULL() != null) {
                    String operator = ctx.compareOp().getText().toLowerCase();
                    if (!operator.equals("eq") && !operator.equals("ne")) {
                        throw new ScimFilterException(
                                "Operator '%s' does not accept null values".formatted(operator));
                    }
                }
                return null;
            }
        }.visit(filterCtx);
    }

    /**
     * 按 RFC 8259 对 JSON 字符串值（不含外围引号）进行反转义。
     * <p>Unicode 转义序列由 ANTLR 词法分析器处理。</p>
     */
    public static String unescapeJsonString(String s) {
        if (s.indexOf('\\') == -1) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    default   -> sb.append('\\').append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
