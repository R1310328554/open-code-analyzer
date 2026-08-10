/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.signing;

import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.utils.StringUtil;

/**
 * {@link CredentialSigner} 抽象基类，封装签名密钥解析与 JWS 签名上下文构建。
 * <p>子类仅需关注具体凭证格式（JWT、LD、SD-JWT）的签名逻辑。</p>
 */
public abstract class AbstractCredentialSigner<T> implements CredentialSigner<T> {

    protected final KeycloakSession keycloakSession;

    /** @param keycloakSession 当前 Keycloak 会话 */
    protected AbstractCredentialSigner(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    /**
     * 根据凭证构建配置重建 {@link SignatureSignerContext}。
     *
     * @param credentialBuildConfig 凭证构建配置（算法、密钥 ID 等）
     * @return 可用于签名的上下文
     * @throws CredentialSignerException 未配置签名算法或找不到密钥时
     */
    protected SignatureSignerContext getSigner(CredentialBuildConfig credentialBuildConfig) {
        if (credentialBuildConfig.getSigningAlgorithm() == null) {
            throw new CredentialSignerException(String.format(
                    "A signing algorithm must be configured for credential %s",
                    credentialBuildConfig.getCredentialConfigId()
            ));
        }

        // 1. signingKeyId 为空时使用域内活跃签名密钥
        // 2. 密钥轮换时 header kid 可能指向不同物理密钥；可通过 overrideKeyId 自定义 kid
        KeyWrapper signingKey = getKeyWithKidSubstitute(
                credentialBuildConfig.getSigningKeyId(),
                credentialBuildConfig.getSigningAlgorithm(),
                credentialBuildConfig.getOverrideKeyId()
        );

        SignatureProvider signatureProvider = keycloakSession
                .getProvider(SignatureProvider.class, credentialBuildConfig.getSigningAlgorithm());

        return signatureProvider.signer(signingKey);
    }

    /**
     * 按 keyId 查找密钥，未指定时返回给定 JWS 算法的活跃密钥。
     * <p>若提供 keyIdSubstitute，则克隆密钥并替换 kid，以便 JWS 头使用自定义标识。</p>
     *
     * @param keyId           密钥 ID，可为空
     * @param algorithm       JWS 签名算法
     * @param keyIdSubstitute 覆盖 JWS 头的 kid，可为空
     * @return 可用于签名的 {@link KeyWrapper}
     */
    protected KeyWrapper getKeyWithKidSubstitute(String keyId, String algorithm, String keyIdSubstitute) {
        KeyWrapper signingKey = getKey(keyId, algorithm);
        if (signingKey == null) {
            throw new CredentialSignerException(
                    String.format("No key for id '%s' and algorithm '%s' available.", keyId, algorithm));
        }

        if (keyIdSubstitute != null) {
            // 克隆密钥以免修改原始 kid，保证后续请求仍能按原 ID 查找
            signingKey = signingKey.cloneKey();
            signingKey.setKid(keyIdSubstitute);
        }

        return signingKey;
    }

    /**
     * 按 keyId 查找密钥；keyId 为空时返回域内该算法的活跃签名密钥。
     *
     * @param keyId     密钥 ID
     * @param algorithm JWS 算法
     * @return 匹配的 {@link KeyWrapper}，未找到时返回 null
     */
    protected KeyWrapper getKey(String keyId, String algorithm) {
        RealmModel realm = keycloakSession.getContext().getRealm();
        KeyManager keys = keycloakSession.keys();

        if (StringUtil.isBlank(keyId)) {
            // keyId 为空时使用活跃密钥；密钥轮换策略尚待完善
            return keys.getActiveKey(realm, KeyUse.SIG, algorithm);
        }

        return keys.getKey(realm, keyId, KeyUse.SIG, algorithm);
    }
}
