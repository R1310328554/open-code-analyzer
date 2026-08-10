/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.forms.login.freemarker.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.authentication.otp.OTPApplicationProvider;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.utils.TotpUtils;

/**
 * TOTP 配置/更新 FreeMarker Bean，用于 {@link UserModel.RequiredAction#CONFIGURE_TOTP} 必需操作页。
 * <p>提供密钥、QR 码、手动输入链接、领域 OTP 策略及已注册 OTP 凭证列表。</p>
 * Used for UpdateTotp required action
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class TotpBean {

    private KeycloakSession session;
    private final RealmModel realm;
    private final String totpSecret;
    private final String totpSecretEncoded;
    private final String totpSecretQrCode;
    private final boolean enabled;
    private UriBuilder uriBuilder;
    private final List<CredentialModel> otpCredentials;
    private final List<String> supportedApplications;
    private final UserModel user;

    /** 自动生成新 TOTP 密钥。 */
    public TotpBean(KeycloakSession session, RealmModel realm, UserModel user, UriBuilder uriBuilder) {
        this(session, realm, user, uriBuilder, null);
    }

    /** @param secret 指定密钥；null 时自动生成 */
    public TotpBean(KeycloakSession session, RealmModel realm, UserModel user, UriBuilder uriBuilder, String secret) {
        this.session = session;
        this.realm = realm;
        this.user = user;
        this.uriBuilder = uriBuilder;
        this.enabled = user.credentialManager().isConfiguredFor(OTPCredentialModel.TYPE);
        if (enabled) {
            otpCredentials = user.credentialManager().getStoredCredentialsByTypeStream(OTPCredentialModel.TYPE)
                    .collect(Collectors.toList());
        } else {
            otpCredentials = Collections.EMPTY_LIST;
        }
        if (secret == null) {
            this.totpSecret = HmacOTP.generateSecret(20);
        } else {
            this.totpSecret = secret;
        }
        this.totpSecretEncoded = TotpUtils.encode(totpSecret);
        this.totpSecretQrCode = TotpUtils.qrCode(session, totpSecret, realm, user);

        OTPPolicy otpPolicy = realm.getOTPPolicy();
        this.supportedApplications = session.getAllProviders(OTPApplicationProvider.class).stream()
                .filter(p -> p.supports(otpPolicy))
                .map(OTPApplicationProvider::getName)
                .collect(Collectors.toList());
    }

    /** @return 用户是否已配置 OTP 凭证 */
    public boolean isEnabled() {
        return enabled;
    }

    /** @return 原始 TOTP 密钥（Base32 解码前） */
    public String getTotpSecret() {
        return totpSecret;
    }

    /** @return Base32 编码后的密钥，供手动输入 */
    public String getTotpSecretEncoded() {
        return totpSecretEncoded;
    }

    /** @return otpauth URI，供 QR 码生成 */
    public String getTotpSecretQrCode() {
        return totpSecretQrCode;
    }

    /** @return 切换到手动输入模式的 action URL */
    public String getManualUrl() {
        return uriBuilder.replaceQueryParam("session_code").replaceQueryParam("mode", "manual")
            .replaceQueryParam("execution", UserModel.RequiredAction.CONFIGURE_TOTP.name()).build().toString();
    }

    /** @return 切换到 QR 码模式的 action URL */
    public String getQrUrl() {
        return uriBuilder.replaceQueryParam("session_code").replaceQueryParam("mode", "qr").build().toString();
    }

    /** @return 领域 OTP 策略（算法、位数、周期等） */
    public OTPPolicy getPolicy() {
        return realm.getOTPPolicy();
    }

    /** @return 与当前 OTP 策略兼容的认证器应用名称列表 */
    public List<String> getSupportedApplications() {
        return supportedApplications;
    }

    /** @return 用户已注册的 OTP 凭证列表（已启用时非空） */
    public List<CredentialModel> getOtpCredentials() {
        return otpCredentials;
    }

    /** @return 当前用户名 */
    public String getUsername() {
        return user.getUsername();
    }

}
