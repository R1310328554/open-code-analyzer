/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.saml;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;

/**
 * 从 SAML 元数据 HTTP 端点加载公钥。
 * <p>通过 {@link HttpClientProvider} 拉取元数据 URL，解析 IdP 或 SP 签名/加密证书。</p>
 *
 * @author rmartinc
 */
public class SamlMetadataPublicKeyLoader extends SamlAbstractMetadataPublicKeyLoader {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(SamlMetadataPublicKeyLoader.class);
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** SAML 元数据描述符 URL */
    private final String metadataUrl;

    /** 默认加载 IdP 密钥 @param session 会话 @param metadataUrl 元数据 URL */
    public SamlMetadataPublicKeyLoader(KeycloakSession session, String metadataUrl) {
        this(session, metadataUrl, true);
    }

    /**
     * @param session Keycloak 会话
     * @param metadataUrl 元数据端点 URL
     * @param forIdP true 解析 IdP 描述符，false 解析 SP
     */
    public SamlMetadataPublicKeyLoader(KeycloakSession session, String metadataUrl, boolean forIdP) {
        super(forIdP);
        this.session = session;
        this.metadataUrl = metadataUrl;
    }

    /** 从元数据 URL HTTP GET 获取 EntityDescriptor XML @return 元数据字符串 */
    @Override
    protected String getKeys() throws Exception {
        logger.debugf("loading keys from metadata endpoint %s", metadataUrl);
        return session.getProvider(HttpClientProvider.class).getString(metadataUrl);
    }
}
