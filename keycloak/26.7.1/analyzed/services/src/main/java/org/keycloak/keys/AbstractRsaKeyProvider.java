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

package org.keycloak.keys;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.models.RealmModel;

/**
 * RSA 密钥提供者抽象基类：从组件配置加载 PEM 私钥与证书并封装为 {@link KeyWrapper}。
 * <p>算法按 keyUse 默认 RS256（签名）或 RSA-OAEP（加密）；密钥加载结果缓存在 model note 中避免重复解析。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class AbstractRsaKeyProvider implements KeyProvider {

    /** 密钥启用/活跃状态。 */
    private final KeyStatus status;

    /** 密钥组件配置模型。 */
    private final ComponentModel model;

    /** 已加载的 RSA 密钥包装对象。 */
    protected final KeyWrapper key;

    /** 密钥算法名称（如 RS256、RSA-OAEP）。 */
    private final String algorithm;

    /** 从组件配置加载 RSA 密钥；结果缓存在 model note 中避免重复加载。 */
    public AbstractRsaKeyProvider(RealmModel realm, ComponentModel model) {
        this.model = model;
        this.status = KeyStatus.from(model.get(Attributes.ACTIVE_KEY, true), model.get(Attributes.ENABLED_KEY, true));

        String defaultAlgorithmKey = KeyUse.ENC.name().equals(model.get(Attributes.KEY_USE)) ? JWEConstants.RSA_OAEP : Algorithm.RS256;
        this.algorithm = model.get(Attributes.ALGORITHM_KEY, defaultAlgorithmKey);

        if (model.hasNote(KeyWrapper.class.getName())) {
            key = model.getNote(KeyWrapper.class.getName());
        } else {
            key = loadKey(realm, model);
            model.setNote(KeyWrapper.class.getName(), key);
        }
    }

    /** 解码 PEM 私钥与证书，构建 RSA {@link KeyWrapper}。 */
    public KeyWrapper loadKey(RealmModel realm, ComponentModel model) {
        String privateRsaKeyPem = model.getConfig().getFirst(Attributes.PRIVATE_KEY_KEY);
        String certificatePem = model.getConfig().getFirst(Attributes.CERTIFICATE_KEY);

        PrivateKey privateKey = PemUtils.decodePrivateKey(privateRsaKeyPem);
        if (privateKey == null) {
            throw new RuntimeException("Key not found on the server. Check key for " + ImportedRsaKeyProviderFactory.ID + " in realm " + realm.getName());
        }
        PublicKey publicKey = KeyUtils.extractPublicKey(privateKey);

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        X509Certificate certificate = PemUtils.decodeCertificate(certificatePem);

        KeyUse keyUse = KeyUse.valueOf(model.get(Attributes.KEY_USE, KeyUse.SIG.name()).toUpperCase());

        return createKeyWrapper(keyPair, certificate, keyUse);
    }

    @Override
    /** @return 包含单个 RSA 密钥的流 */
    public Stream<KeyWrapper> getKeysStream() {
        return Stream.of(key);
    }

    /** 由 RSA 密钥对与证书构建 {@link KeyWrapper}（无证书链）。 */
    protected KeyWrapper createKeyWrapper(KeyPair keyPair, X509Certificate certificate, KeyUse keyUse) {
        return createKeyWrapper(keyPair, certificate, Collections.emptyList(), keyUse);
    }

    /** 由 RSA 密钥对、证书及可选证书链构建 {@link KeyWrapper}。 */
    protected KeyWrapper createKeyWrapper(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain,
        KeyUse keyUse) {
        KeyWrapper key = new KeyWrapper();

        key.setProviderId(model.getId());
        key.setProviderPriority(model.get("priority", 0l));

        key.setKid(model.get(Attributes.KID_KEY) != null ? model.get(Attributes.KID_KEY) : KeyUtils.createKeyId(keyPair.getPublic()));
        key.setUse(keyUse == null ? KeyUse.SIG : keyUse);
        key.setType(KeyType.RSA);
        key.setAlgorithm(algorithm);
        key.setStatus(status);
        key.setPrivateKey(keyPair.getPrivate());
        key.setPublicKey(keyPair.getPublic());
        key.setCertificate(certificate);

        if (!certificateChain.isEmpty()) {
            if (certificate != null && !certificate.equals(certificateChain.get(0))) {
                // 若证书链未包含终端实体证书，则将其插入链首
                certificateChain.add(0, certificate);
            }
            key.setCertificateChain(certificateChain);
        }

        return key;
    }

}
