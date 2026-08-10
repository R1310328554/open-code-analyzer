package org.keycloak.protocol.oidc.token;

/**
 * 令牌拦截器异常。
 * <p>在 {@link TokenPostProcessor} 处理令牌时抛出，携带 OAuth 风格 {@code error} 与 {@code description}。</p>
 */
public class TokenInterceptorException extends RuntimeException {

    /** OAuth 错误码 */
    private String error;
    /** 错误描述 */
    private String description;

    /** @param error OAuth 错误码 @param description 错误描述 */
    public TokenInterceptorException(String error, String description) {
        this.error = error;
        this.description = description;
    }

    /** @return OAuth 错误码 */
    public String getError() {
        return error;
    }

    /** @return 错误描述 */
    public String getDescription() {
        return description;
    }
}
