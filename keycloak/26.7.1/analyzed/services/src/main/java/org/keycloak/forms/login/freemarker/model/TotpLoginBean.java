/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.forms.login.freemarker.model;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.authentication.authenticators.browser.OTPFormAuthenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.OTPCredentialProvider;
import org.keycloak.credential.OTPCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;

/**
 * TOTP 登录 FreeMarker Bean：展示用户 OTP 凭证列表与当前选中凭证。
 * <p>用于 OTP 表单认证器，支持多 OTP 设备切换。</p>
 * Used for TOTP login
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TotpLoginBean {

    private final String selectedCredentialId;
    private final List<OTPCredential> userOtpCredentials;
    private OTPPolicy policy;

    /** @param selectedCredentialId 当前选中的 OTP 凭证 ID；null/空时使用默认凭证 */
    public TotpLoginBean(KeycloakSession session, RealmModel realm, UserModel user, String selectedCredentialId) {

        this.userOtpCredentials = user.credentialManager().getStoredCredentialsByTypeStream(OTPCredentialModel.TYPE)
                .map(OTPCredential::new)
                .collect(Collectors.toList());

        // 用户尚未在 UI 手动选择 OTP 凭证，则使用优先级最高的默认凭证
        if (selectedCredentialId == null || selectedCredentialId.isEmpty()) {
            OTPCredentialProvider otpCredentialProvider = (OTPCredentialProvider)session.getProvider(CredentialProvider.class, OTPCredentialProviderFactory.PROVIDER_ID);
            OTPCredentialModel otpCredential = otpCredentialProvider
                    .getDefaultCredential(session, realm, user);

            selectedCredentialId = otpCredential==null ? null : otpCredential.getId();
        }

        this.selectedCredentialId = selectedCredentialId;
        this.policy = realm.getOTPPolicy();
    }


    /** @return 用户全部 OTP 凭证视图列表 */
    public List<OTPCredential> getUserOtpCredentials() {
        return userOtpCredentials;
    }

    /** @return 当前选中 OTP 凭证 ID */
    public String getSelectedCredentialId() {
        return selectedCredentialId;
    }

    /** @return 领域 OTP 策略 */
    public OTPPolicy getPolicy() {
        return policy;
    }

    /** 单个 OTP 凭证的模板视图。 */
    public static class OTPCredential {

        private final String id;
        private final String userLabel;

        /** @param credentialModel 底层凭证模型 */
        public OTPCredential(CredentialModel credentialModel) {
            this.id = credentialModel.getId();
            // TODO：未命名 OTP 凭证在 UI 中应以灰色展示
            this.userLabel = credentialModel.getUserLabel() == null || credentialModel.getUserLabel().isEmpty() ? OTPFormAuthenticator.UNNAMED : credentialModel.getUserLabel();
        }

        /** @return 凭证 ID */
        public String getId() {
            return id;
        }

        /** @return 用户自定义标签；未命名时返回 {@link OTPFormAuthenticator#UNNAMED} */
        public String getUserLabel() {
            return userLabel;
        }
    }
}
