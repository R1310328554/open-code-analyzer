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

package org.keycloak.credential;

import org.keycloak.authentication.authenticators.browser.WebAuthnMetadataService;
import org.keycloak.authentication.requiredactions.WebAuthnPasswordlessRegisterFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.models.credential.WebAuthnCredentialModel;

import com.webauthn4j.converter.util.ObjectConverter;

/**
 * WebAuthn 无密码凭证提供者：继承双因素实现，使用无密码策略与 UI 分类。
 * <p>凭证类型为 {@link WebAuthnCredentialModel#TYPE_PASSWORDLESS}，读取 {@link WebAuthnPolicy} 的无密码配置。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnPasswordlessCredentialProvider extends WebAuthnCredentialProvider {

    /** @param session 当前会话 @param metadataService 认证器元数据 @param objectConverter WebAuthn4J 转换器 */
    public WebAuthnPasswordlessCredentialProvider(KeycloakSession session, WebAuthnMetadataService metadataService, ObjectConverter objectConverter) {
        super(session, metadataService, objectConverter);
    }

    @Override
    /** @return {@link WebAuthnCredentialModel#TYPE_PASSWORDLESS} */
    public String getType() {
        return WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    }

    @Override
    /** 返回无密码分类的凭据元数据与注册必需动作。 */
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.PASSWORDLESS)
                .displayName("webauthn-passwordless-display-name")
                .helpText("webauthn-passwordless-help-text")
                .iconCssClass("kcAuthenticatorWebAuthnPasswordlessClass")
                .createAction(WebAuthnPasswordlessRegisterFactory.PROVIDER_ID)
                .removeable(true)
                .build(getKeycloakSession());
    }

    @Override
    /** @return 当前 Realm 的无密码 WebAuthn 策略（非双因素策略）。 */
    protected WebAuthnPolicy getWebAuthnPolicy() {
        return getKeycloakSession().getContext().getRealm().getWebAuthnPolicyPasswordless();
    }
}
