package org.keycloak.protocol.oidc;

/**
 * Logout Token 校验结果码。
 * <p>用于后端/前端登出流程中对 IdP 下发的 logout token 做结构化错误分类。</p>
 */
public enum LogoutTokenValidationCode {

    /** 校验成功。 */
    VALIDATION_SUCCESS(""),
    /** 解码 logout token 失败。 */
    DECODE_TOKEN_FAILED("The decode of the logoutToken failed"),
    /** 未找到对应身份提供者。 */
    COULD_NOT_FIND_IDP("No Identity Provider has been found"),
    /** 与 IdP 联合校验 logout token 失败。 */
    TOKEN_VERIFICATION_WITH_IDP_FAILED("LogoutToken verification with identity provider failed"),
    /** 缺少 sid 或 sub 声明。 */
    MISSING_SID_OR_SUBJECT("Missing sid or sub claim"),
    /** event 声明不符合后端登出预期。 */
    BACKCHANNEL_LOGOUT_EVENT_MISSING("The LogoutToken event claim is not as expected"),
    /** logout token 含不允许的 nonce 声明。 */
    NONCE_CLAIM_IN_TOKEN("The LogoutToken contains a nonce claim which is not allowed"),
    /** 缺少 iat 声明。 */
    MISSING_IAT_CLAIM("The LogoutToken doesn't contain an iat claim"),
    /** 缺少 jti（令牌 ID）。 */
    LOGOUT_TOKEN_ID_MISSING("The logoutToken jti is missing");

    /** 人类可读错误信息（英文，供日志/事件）。 */
    private String errorMessage;

    LogoutTokenValidationCode(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** @return 错误描述 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** @return 包装为本状态码的 {@link LogoutTokenValidationContext} */
    LogoutTokenValidationContext toCtx() {
        return new LogoutTokenValidationContext(this);
    }
}
