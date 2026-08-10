/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.trust;

import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.broker.provider.TrustMaterialIdentityProvider;
import org.keycloak.broker.provider.TrustMaterialRequest;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.keys.loader.HardcodedPublicKeyLoader;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.JWKSServerUtils;
import org.keycloak.util.Strings;
import org.keycloak.utils.StringUtil;

/**
 * 默认信任材料身份代理：从 JWKS URL 或内嵌公钥解析签名密钥，
 * 供 OID4VC VCI 与 ABCA 等场景校验外部 JWT/断言。
 */
public class DefaultTrustIdentityProvider implements TrustMaterialIdentityProvider<DefaultTrustIdentityProviderConfig> {

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 信任 IdP 配置。 */
    private final DefaultTrustIdentityProviderConfig config;

    /** @param session 当前 Keycloak 会话
     * @param config 信任材料 IdP 配置 */
    public DefaultTrustIdentityProvider(KeycloakSession session, DefaultTrustIdentityProviderConfig config) {
        this.session = session;
        this.config = config;
    }

    /** @return 当前信任 IdP 配置 */
    @Override
    public DefaultTrustIdentityProviderConfig getConfig() {
        return config;
    }

    /** 按 kid/算法从公钥缓存或加载器解析 JWK，并经 {@link TrustKeyUtil} 过滤。 */
    @Override
    public Stream<JWK> resolveKeys(TrustMaterialRequest request) {
        PublicKeyLoader loader = getPublicKeyLoader(request);
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), config.getInternalId());
        Stream<KeyWrapper> keys = Strings.isEmpty(request.getKid())
                ? keyStorage.getKeys(modelKey, loader).stream()
                : Stream.of(keyStorage.getPublicKey(modelKey, request.getKid(), request.getAlgorithm(), loader))
                  .filter(Objects::nonNull);

        return TrustKeyUtil.filterKeys(keys.map(JWKSServerUtils::toJwk), request);
    }

    /** 启用 JWKS URL 时强制刷新公钥缓存；否则返回 false。 */
    @Override
    public boolean reloadKeys() {
        if (!config.isEnabled() || !config.isUseJwksUrl()
                || StringUtil.isBlank(config.getTrustedJwksUrl())) {
            return false;
        }

        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), config.getInternalId());
        return keyStorage.reloadKeys(modelKey, new DefaultTrustMaterialPublicKeyLoader(session, config));
    }

    /** 无额外资源需释放。 */
    @Override
    public void close() {
    }

    /** 根据配置选择硬编码 PEM/JWK 或 HTTP JWKS 加载器。 */
    private PublicKeyLoader getPublicKeyLoader(TrustMaterialRequest request) {
        if (!config.isUseJwksUrl() && StringUtil.isNotBlank(config.getTrustedJwks())
                && !config.getTrustedJwks().trim().startsWith("{")) {
            String kid = StringUtil.isNotBlank(request.getKid()) ? request.getKid() : config.getTrustedJwksKeyId();
            return new HardcodedPublicKeyLoader(
                    StringUtil.isNotBlank(kid) ? kid.trim() : null,
                    config.getTrustedJwks(),
                    request.getAlgorithm());
        }

        return new DefaultTrustMaterialPublicKeyLoader(session, config);
    }
}
