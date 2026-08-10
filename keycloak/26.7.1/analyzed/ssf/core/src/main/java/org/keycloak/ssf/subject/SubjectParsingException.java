package org.keycloak.ssf.subject;

import org.keycloak.ssf.SsfException;

/**
 * 主体标识符 JSON 解析或 format 校验失败时抛出的 SSF 异常。
 * <p>通常由 {@link SubjectIdJsonDeserializer} 在缺少/无效 {@code format} 或未知 format 时触发。</p>
 */
public class SubjectParsingException extends SsfException {

    public SubjectParsingException() {
    }

    public SubjectParsingException(String message) {
        super(message);
    }

    public SubjectParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
