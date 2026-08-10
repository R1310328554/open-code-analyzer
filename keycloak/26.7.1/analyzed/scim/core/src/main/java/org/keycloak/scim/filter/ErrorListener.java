package org.keycloak.scim.filter;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * ANTLR 错误监听器，在过滤器解析过程中收集语法错误。
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ErrorListener extends BaseErrorListener {

    /** 是否检测到语法错误。 */
    private boolean hasErrors = false;
    /** 收集到的错误消息列表。 */
    private final List<String> errorMessages = new ArrayList<>();

    /** 记录 ANTLR 语法错误并追加到消息列表。 */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        hasErrors = true;
        errorMessages.add(String.format("position %d: %s", charPositionInLine, msg));
    }

    /** 返回是否检测到语法错误。 */
    public boolean hasErrors() {
        return hasErrors;
    }

    /** 返回收集到的全部错误消息。 */
    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
