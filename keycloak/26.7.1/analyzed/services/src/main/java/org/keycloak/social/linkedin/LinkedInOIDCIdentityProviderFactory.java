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
package org.keycloak.social.linkedin;

import java.io.IOException;
import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.representations.OIDCConfigurationRepresentation;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * LinkedIn OpenID Connect 身份提供者工厂。
 * <p>专用于 <b>Sign In with LinkedIn using OpenID Connect</b> 产品应用。
 * LinkedIn 与默认 OIDC 实现存在两处兼容性问题：</p>
 *
 * <ol>
 * <li>JWKS 端点返回的签名密钥缺少 {@code use} 声明。</li>
 * <li>授权请求中的 nonce 不会回显到 ID Token 中。</li>
 * </ol>
 *
 * <p>本工厂通过自定义公钥加载与禁用 nonce 校验来规避上述问题。</p>
 *
 * @author rmartinc
 */
public class LinkedInOIDCIdentityProviderFactory extends AbstractIdentityProviderFactory<LinkedInOIDCIdentityProvider> implements SocialIdentityProviderFactory<LinkedInOIDCIdentityProvider> {

    /** LinkedIn OIDC IdP 的 provider id。 */
    public static final String PROVIDER_ID = "linkedin-openid-connect";
    /** LinkedIn OIDC 发现文档 URL。 */
    public static final String WELL_KNOWN_URL = "https://www.linkedin.com/oauth/.well-known/openid-configuration";

    /** 缓存的 OIDC 发现元数据（静态，进程内共享）。 */
    private static OIDCConfigurationRepresentation metadata;

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "LinkedIn";
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * 创建 LinkedIn OIDC IdP 实例。
     * <p>从 well-known 端点拉取 issuer、端点 URL 与 JWKS 地址，并禁用 nonce 校验。</p>
     */
    @Override
    public LinkedInOIDCIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        OIDCConfigurationRepresentation local = metadata;
        if (local == null) {
            local = getWellKnownMetadata(session);
            if (local.getIssuer() == null || local.getTokenEndpoint() == null || local.getAuthorizationEndpoint()== null || local.getJwksUri() == null) {
                throw new RuntimeException("Invalid data in the OIDC LinkedIn well-known address.");
            }
            metadata = local;
        }
        OIDCIdentityProviderConfig config = new OIDCIdentityProviderConfig(model);
        config.setIssuer(local.getIssuer());
        config.setAuthorizationUrl(local.getAuthorizationEndpoint());
        config.setTokenUrl(local.getTokenEndpoint());
        if (local.getUserinfoEndpoint() != null) {
            config.setUserInfoUrl(local.getUserinfoEndpoint());
        }
        config.setUseJwksUrl(true);
        config.setJwksUrl(local.getJwksUri());
        config.setValidateSignature(true);
        config.setDisableNonce(true); // LinkedIn 未正确回显 nonce
        return new LinkedInOIDCIdentityProvider(session, config);
    }

    /** 创建默认 OIDC 配置对象。 */
    @Override
    public OIDCIdentityProviderConfig createConfig() {
        return new OIDCIdentityProviderConfig();
    }

    /** 从 LinkedIn well-known 端点获取 OIDC 发现元数据。 */
    private static OIDCConfigurationRepresentation getWellKnownMetadata(KeycloakSession session) {
        try (SimpleHttpResponse response = SimpleHttp.create(session).doGet(WELL_KNOWN_URL)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .asResponse()) {
            if (Response.Status.fromStatusCode(response.getStatus()).getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RuntimeException("Error calling the OIDC LinkedIn well-known address. Http status " + response.getStatus());
            }
            return response.asJson(OIDCConfigurationRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException("Error calling the OIDC LinkedIn well-known address.", e);
        }
    }

    /** 返回 IdP 可配置属性列表（当前无额外参数）。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        // 可按需在此添加通用 OIDC 配置项
        return ProviderConfigurationBuilder.create()
                .build();
    }
}
