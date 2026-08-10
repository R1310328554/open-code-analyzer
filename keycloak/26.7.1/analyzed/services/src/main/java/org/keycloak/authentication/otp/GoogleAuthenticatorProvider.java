package org.keycloak.authentication.otp;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;

/**
 * Google Authenticator 应用 Provider：在 OTP 配置 UI 中推荐 Google Authenticator。
 * TOTP 类型要求 30 秒周期；HOTP 类型无周期限制。
 */
public class GoogleAuthenticatorProvider implements OTPApplicationProviderFactory, OTPApplicationProvider {

    @Override
    /** @return 自身作为单例 Provider */
    public OTPApplicationProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    /** @return Provider ID：google */
    public String getId() {
        return "google";
    }

    @Override
    /** @return 消息键 totpAppGoogleName（本地化显示名） */
    public String getName() {
        return "totpAppGoogleName";
    }

    @Override
    /** TOTP 时要求 30 秒周期；HOTP 始终支持。 */
    public boolean supports(OTPPolicy policy) {
        if (policy.getType().equals("totp")) {
            return policy.getPeriod() == 30;
        }
        return true;
    }

    @Override
    public void close() {
    }

}
