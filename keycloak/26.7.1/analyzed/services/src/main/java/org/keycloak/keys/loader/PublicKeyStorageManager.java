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

package org.keycloak.keys.loader;

import java.security.PublicKey;

import org.keycloak.broker.jwtauthorizationgrant.JWTAuthorizationGrantConfig;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * 公钥存储管理器：通过 {@link PublicKeyStorageProvider} 缓存并解析客户端与身份提供方 JWT 验证公钥。
 * <p>按 kid/算法选择 {@link ClientPublicKeyLoader}、{@link OIDCIdentityProviderPublicKeyLoader} 或 {@link HardcodedPublicKeyLoader}。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeyStorageManager {

    private static final Logger logger = Logger.getLogger(PublicKeyStorageManager.class);

    /** 从 JWS 头 kid/算法解析客户端 {@link PublicKey}；未找到时返回 null。 */
    public static PublicKey getClientPublicKey(KeycloakSession session, ClientModel client, JWSInput input) {
        KeyWrapper keyWrapper = getClientPublicKeyWrapper(session, client, input);
        PublicKey publicKey = null;
        if (keyWrapper != null) {
            publicKey = (PublicKey)keyWrapper.getPublicKey();
        }
        return publicKey;
    }

    /** 按 JWS 头 kid/算法从缓存或 {@link ClientPublicKeyLoader} 获取客户端 {@link KeyWrapper}。 */
    public static KeyWrapper getClientPublicKeyWrapper(KeycloakSession session, ClientModel client, JWSInput input) {
        String kid = input.getHeader().getKeyId();
        String alg = input.getHeader().getRawAlgorithm();
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getClientModelCacheKey(client.getRealm().getId(), client.getId());
        ClientPublicKeyLoader loader = new ClientPublicKeyLoader(session, client);
        return keyStorage.getPublicKey(modelKey, kid, alg, loader);
    }

    /** 按指定 JWK 用途与算法获取客户端首个匹配 {@link KeyWrapper}。 */
    public static KeyWrapper getClientPublicKeyWrapper(KeycloakSession session, ClientModel client, JWK.Use keyUse, String algAlgorithm) {
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
        String modelKey = PublicKeyStorageUtils.getClientModelCacheKey(client.getRealm().getId(), client.getId(), keyUse);
        ClientPublicKeyLoader loader = new ClientPublicKeyLoader(session, client, keyUse);
        return keyStorage.getFirstPublicKey(modelKey, algAlgorithm, loader);
    }

    /** 从 JWS 头提取 kid/算法并委托 {@link #getIdentityProviderKeyWrapper(KeycloakSession, RealmModel, JWTAuthorizationGrantConfig, String, String)}。 */
    public static KeyWrapper getIdentityProviderKeyWrapper(KeycloakSession session, RealmModel realm, JWTAuthorizationGrantConfig idpConfig, JWSInput input) {
        String kid = input.getHeader().getKeyId();
        String alg = input.getHeader().getRawAlgorithm();
        return getIdentityProviderKeyWrapper(session, realm, idpConfig, kid, alg);
    }

    /** 按 IdP 配置选择 JWKS/硬编码加载器，从公钥缓存解析身份提供方 {@link KeyWrapper}。 */
    public static KeyWrapper getIdentityProviderKeyWrapper(KeycloakSession session, RealmModel realm, JWTAuthorizationGrantConfig idpConfig, String kid, String alg) {
        PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);

        String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(realm.getId(), idpConfig.getInternalId());
        PublicKeyLoader loader;
        if (idpConfig.isUseJwksUrl()) {
            loader = new OIDCIdentityProviderPublicKeyLoader(session, idpConfig);
        } else {
            String pem = idpConfig.getPublicKeySignatureVerifier();
            if (StringUtil.isNotBlank(pem) && pem.trim().startsWith("{")) {
                loader = new OIDCIdentityProviderPublicKeyLoader(session, idpConfig);
            } else if (StringUtil.isNotBlank(pem)) {
                loader = new HardcodedPublicKeyLoader(
                        StringUtil.isNotBlank(idpConfig.getPublicKeySignatureVerifierKeyId())
                                ? idpConfig.getPublicKeySignatureVerifierKeyId().trim()
                                : kid, pem, alg);
            } else {
                logger.warnf("No public key saved on identityProvider %s", idpConfig.getAlias());
                return null;
            }
        }

        return keyStorage.getPublicKey(modelKey, kid, alg, loader);
    }
}
