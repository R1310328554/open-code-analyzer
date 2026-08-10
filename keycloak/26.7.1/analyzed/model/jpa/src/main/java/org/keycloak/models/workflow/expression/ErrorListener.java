package org.keycloak.models.workflow.expression;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * ANTLR 语法错误监听器：累积布尔条件表达式的解析错误信息。
 */
public class ErrorListener extends BaseErrorListener {
    private boolean hasErrors = false;
    /** 格式化后的错误消息列表（含行号与列号）。 */
    private final List<String> errorMessages = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        hasErrors = true;
        String error = String.format("Error at line %d:%d - %s", line, charPositionInLine, msg);
        errorMessages.add(error);
    }

    /** 解析过程中是否出现过语法错误。 */
    public boolean hasErrors() {
        return hasErrors;
    }

    /** 返回全部语法错误描述。 */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
