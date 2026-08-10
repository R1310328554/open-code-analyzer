/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.credential;

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.browser.WebAuthnMetadataService;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

import com.webauthn4j.converter.util.ObjectConverter;

/**
 * WebAuthn 双因素凭证 {@link WebAuthnCredentialProvider} 的 SPI 工厂。
 * <p>懒加载 {@link ObjectConverter} 与 {@link WebAuthnMetadataService}；仅在 {@link Profile.Feature#WEB_AUTHN} 启用时注册。</p>
 */
public class WebAuthnCredentialProviderFactory implements CredentialProviderFactory<WebAuthnCredentialProvider>, EnvironmentDependentProviderFactory {

    /** SPI 工厂标识：{@code keycloak-webauthn}。 */
    public static final String PROVIDER_ID = "keycloak-webauthn";

    /** 双重检查锁懒加载的 WebAuthn4J 对象转换器。 */
    private volatile ObjectConverter converter;
    /** 双重检查锁懒加载的认证器元数据服务。 */
    private volatile WebAuthnMetadataService metadataService;

    @Override
    /** @param session 当前会话 @return WebAuthn 双因素凭证提供者 */
    public CredentialProvider create(KeycloakSession session) {
        return new WebAuthnCredentialProvider(session, getMetadataService(), createOrGetObjectConverter());
    }

    /** 线程安全地获取或创建 {@link ObjectConverter} 单例。 */
    protected ObjectConverter createOrGetObjectConverter() {
        if (converter == null) {
            synchronized (this) {
                if (converter == null) {
                    converter = new ObjectConverter();
                }
            }
        }
        return converter;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 是否启用 WEB_AUTHN 特性 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN);
    }

    /** 线程安全地获取或创建 {@link WebAuthnMetadataService} 单例。 */
    protected WebAuthnMetadataService getMetadataService() {
        if (metadataService == null) {
            synchronized (this) {
                if (metadataService == null) {
                    this.metadataService = new WebAuthnMetadataService();
                }
            }
        }
        return this.metadataService;
    }
}
