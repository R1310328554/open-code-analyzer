package org.keycloak.protocol.oidc.scope;

/**
 * 参数化 Client Scope 的参数值无效时抛出的运行时异常。
 * <p>由 {@link ParameterizedScopeTypeProvider#validateParameter} 等校验方法抛出。</p>
 */
public class InvalidScopeParameterException extends RuntimeException {

    /** @param message 错误描述 */
    public InvalidScopeParameterException(String message) {
        super(message);
    }

    /**
     * @param message 错误描述
     * @param cause 原始异常
     */
    public InvalidScopeParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
