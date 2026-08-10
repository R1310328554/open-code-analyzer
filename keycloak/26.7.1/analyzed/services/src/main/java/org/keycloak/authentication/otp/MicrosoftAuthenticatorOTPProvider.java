package org.keycloak.authentication.otp;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;

/**
 * Microsoft Authenticator 应用 Provider：在 OTP 配置 UI 中推荐 Microsoft Authenticator。
 * 要求 6 位数字、HmacSHA1 算法及 30 秒 TOTP 周期。
 */
public class MicrosoftAuthenticatorOTPProvider implements OTPApplicationProviderFactory, OTPApplicationProvider {

    @Override
    /** @return 自身作为单例 Provider */
    public OTPApplicationProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    /** @return Provider ID：microsoft-authenticator */
    public String getId() {
        return "microsoft-authenticator";
    }

    @Override
    /** @return 消息键 totpAppMicrosoftAuthenticatorName（本地化显示名） */
    public String getName() {
        return "totpAppMicrosoftAuthenticatorName";
    }

    @Override
    /** 仅支持 6 位、HmacSHA1、30 秒 TOTP 策略。 */
    public boolean supports(OTPPolicy policy) {
        if (policy.getDigits() != 6) {
            return false;
        }

        if (!policy.getAlgorithm().equals("HmacSHA1")) {
            return false;
        }

        return policy.getType().equals("totp") && policy.getPeriod() == 30;
    }

    @Override
    public void close() {
    }

}
