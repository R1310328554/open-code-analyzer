package org.keycloak.scim.client;

import org.keycloak.scim.protocol.response.ErrorResponse;

/**
 * SCIM 客户端运行时异常，可携带原始响应体或结构化 {@link ErrorResponse}。
 */
public class ScimClientException extends RuntimeException {

    /** 原始 HTTP 响应体（若有）。 */
    private final String response;
    /** 解析后的 SCIM 错误响应。 */
    private ErrorResponse error;

    public ScimClientException(String message, Throwable cause, String response) {
        super(message, cause);
        this.response = response;
    }

    public ScimClientException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public ScimClientException(String message, String response) {
        this(message, null, response);
    }

    public <T> ScimClientException(String message, ErrorResponse error) {
        this(message, null, null);
        this.error = error;
    }

    /** 返回原始响应字符串。 */
    public String getResponse() {
        return response;
    }

    /** 返回 SCIM {@link ErrorResponse}，可能为 null。 */
    public ErrorResponse getError() {
        return error;
    }
}
