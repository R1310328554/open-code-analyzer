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

import java.security.Key;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.function.Predicate;

import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.rotation.KeyLocator;

/**
 * 基于 SAML 元数据的 {@link KeyLocator}。
 * <p>通过 {@link PublicKeyStorageProvider} 缓存 {@link PublicKeyLoader} 加载的公钥，支持按 kid 或密钥材料查找，并在证书过期时刷新。</p>
 *
 * @author rmartinc
 */
public class SamlMetadataKeyLocator implements KeyLocator {

    /** 公钥存储模型键（如客户端/IdP 标识） */
    private final String modelKey;
    /** 元数据公钥加载器 */
    private final PublicKeyLoader loader;
    /** 公钥缓存存储提供者 */
    private final PublicKeyStorageProvider keyStorage;
    /** 期望的密钥用途（SIG/ENC） */
    private final KeyUse use;

    /**
     * @param modelKey 缓存键
     * @param loader 公钥加载器
     * @param use 密钥用途过滤
     * @param keyStorage 公钥存储提供者
     */
    public SamlMetadataKeyLocator(String modelKey, PublicKeyLoader loader, KeyUse use, PublicKeyStorageProvider keyStorage) {
        this.modelKey = modelKey;
        this.loader = loader;
        this.keyStorage = keyStorage;
        this.use = use;
    }

    /** 按 Key ID 获取公钥 @param kid 密钥标识 @return 公钥或 null */
    @Override
    public Key getKey(String kid) throws KeyManagementException {
        if (kid == null) {
            return null;
        }
        // 按 kid 查找，过期或缺失时通过 loader 重载
        KeyWrapper keyWrapper = keyStorage.getFirstPublicKey(modelKey, sameKidPredicate(kid), loader);
        return keyWrapper != null? keyWrapper.getPublicKey() : null;
    }

    /** 按密钥字节匹配获取缓存公钥 @param key 参考公钥 @return 匹配的公钥或 null */
    @Override
    public Key getKey(Key key) throws KeyManagementException {
        if (key == null) {
            return null;
        }
        // 按密钥材料查找，过期或缺失时重载
        KeyWrapper keyWrapper = keyStorage.getFirstPublicKey(modelKey, sameKeyPredicate(key), loader);
        return keyWrapper != null? keyWrapper.getPublicKey() : null;
    }

    /** 强制刷新元数据公钥缓存 */
    @Override
    public void refreshKeyCache() {
        keyStorage.reloadKeys(modelKey, loader);
    }

    /** @return 符合用途且证书有效的公钥迭代器 */
    @Override
    public Iterator<Key> iterator() {
        // 遍历缓存密钥（证书无效时由存储层触发刷新）
        return keyStorage.getKeys(modelKey, loader)
                .stream()
                .filter(k -> isSameUse(k) && isValidCertificate(k))
                .map(KeyWrapper::getPublicKey)
                .iterator();
    }

    private Predicate<KeyWrapper> sameKidPredicate(String kid) {
        return keyWrapper -> isSameKid(keyWrapper, kid);
    }

    private boolean isSameKid(KeyWrapper keyWrapper, String kid) {
        String k = keyWrapper.getKid();
        if (k == null) {
            return false;
        }
        return k.equals(kid) && isSameUse(keyWrapper) && isValidCertificate(keyWrapper);
    }

    private Predicate<KeyWrapper> sameKeyPredicate(Key key) {
        return keyWrapper -> isSameKey(keyWrapper, key);
    }

    private boolean isSameKey(KeyWrapper keyWrapper, Key key) {
        Key k = keyWrapper.getPublicKey();
        if (k == null) {
            return false;
        }
        return isSameUse(keyWrapper)
                && key.getAlgorithm().equals(k.getAlgorithm())
                && MessageDigest.isEqual(k.getEncoded(), key.getEncoded())
                && isValidCertificate(keyWrapper);
    }

    private boolean isSameUse(KeyWrapper k) {
        if (k == null) {
            return false;
        }
        // use 为 null 表示签名与加密均可
        return k.getUse() == null || k.getUse().equals(this.use);
    }

    private boolean isValidCertificate(KeyWrapper key) {
        if (key == null || key.getCertificate() == null) {
            return false;
        }
        try {
            key.getCertificate().checkValidity();
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }
}
