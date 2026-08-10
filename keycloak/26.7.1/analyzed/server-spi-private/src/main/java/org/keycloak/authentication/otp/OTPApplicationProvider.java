package org.keycloak.authentication.otp;

import org.keycloak.models.OTPPolicy;
import org.keycloak.provider.Provider;

/**
 * OTP 应用 Provider：描述第三方 OTP 应用（如 Google Authenticator）的名称与策略兼容性。
 */
public interface OTPApplicationProvider extends Provider {

    /** 应用展示名称。 */
    String getName();

    /** 是否支持给定 {@link OTPPolicy}（算法、位数等）。 */
    boolean supports(OTPPolicy policy);

}
