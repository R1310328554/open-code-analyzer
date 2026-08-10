package org.keycloak.protocol.oidc.refresh;

/**
 * refresh token 签发或处理过程中的运行时异常，携带 OAuth 风格 error 与 description。
 */
public class RefreshTokenException extends RuntimeException {

    private final String error;
    private final String errorDescription;

    /**
     * @param error OAuth error 代码
     * @param errorDescription 人类可读错误描述
     */
        this.error = error;
        this.errorDescription = errorDescription;
    }

    /** @return OAuth error 代码 */
    public String getError() {
        return error;
    }

    /** @return 错误描述 */
    public String getErrorDescription() {
        return errorDescription;
    }
}
