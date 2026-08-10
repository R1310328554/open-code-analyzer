package org.keycloak.authentication.otp;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;

/**
 * FreeOTP 应用 Provider：在 OTP 配置 UI 中提供 FreeOTP 作为推荐认证器应用选项。
 * 支持所有 {@link OTPPolicy} 配置。
 */
public class FreeOTPProvider implements OTPApplicationProviderFactory, OTPApplicationProvider {

    @Override
    /** @return 自身作为单例 Provider */
    public OTPApplicationProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    /** @return Provider ID：freeotp */
    public String getId() {
        return "freeotp";
    }

    @Override
    /** @return 消息键 totpAppFreeOTPName（本地化显示名） */
    public String getName() {
        return "totpAppFreeOTPName";
    }

    @Override
    /** @return 始终支持任意 OTP 策略 */
    public boolean supports(OTPPolicy policy) {
        return true;
    }

    @Override
    public void close() {
    }

}
