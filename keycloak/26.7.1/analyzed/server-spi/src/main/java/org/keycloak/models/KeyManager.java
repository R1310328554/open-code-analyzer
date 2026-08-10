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

package org.keycloak.models;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;
import javax.crypto.SecretKey;

import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.keys.RsaKeyMetadata;
import org.keycloak.keys.SecretKeyMetadata;

/**
 * 密钥管理器：提供 Realm 级签名、加密与 HMAC 密钥的查询与获取。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface KeyManager {

    /** @param realm Realm
     * @param use 密钥用途
     * @param algorithm 算法名称
     * @return 当前活动密钥 */
    KeyWrapper getActiveKey(RealmModel realm, KeyUse use, String algorithm);

    /** @param realm Realm
     * @param kid 密钥 ID
     * @param use 密钥用途
     * @param algorithm 算法名称
     * @return 匹配的密钥包装器 */
    KeyWrapper getKey(RealmModel realm, String kid, KeyUse use, String algorithm);

    /**
     * 返回 Realm 内所有密钥。
     * Returns all {@code KeyWrapper} for the given realm.
     * @param realm {@code RealmModel}.
     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.
     */
    Stream<KeyWrapper> getKeysStream(RealmModel realm);

    /**
     * 按用途与算法筛选 Realm 密钥。
     * Returns all {@code KeyWrapper} for the given realm that match given criteria.
     * @param realm {@code RealmModel}.
     * @param use {@code KeyUse}.
     * @param algorithm {@code String}.
     * @return Stream of all {@code KeyWrapper} in the realm. Never returns {@code null}.
     */
    Stream<KeyWrapper> getKeysStream(RealmModel realm, KeyUse use, String algorithm);

    @Deprecated
    ActiveRsaKey getActiveRsaKey(RealmModel realm);

    @Deprecated
    PublicKey getRsaPublicKey(RealmModel realm, String kid);

    @Deprecated
    Certificate getRsaCertificate(RealmModel realm, String kid);

    @Deprecated
    List<RsaKeyMetadata> getRsaKeys(RealmModel realm);

    @Deprecated
    ActiveHmacKey getActiveHmacKey(RealmModel realm);

    @Deprecated
    SecretKey getHmacSecretKey(RealmModel realm, String kid);

    @Deprecated
    List<SecretKeyMetadata> getHmacKeys(RealmModel realm);

    @Deprecated
    ActiveAesKey getActiveAesKey(RealmModel realm);

    @Deprecated
    SecretKey getAesSecretKey(RealmModel realm, String kid);

    @Deprecated
    List<SecretKeyMetadata> getAesKeys(RealmModel realm);

    /** 当前活动 RSA 密钥（含私钥、公钥与证书）。 */
    class ActiveRsaKey {
        private final String kid;
        private final PrivateKey privateKey;
        private final PublicKey publicKey;
        private final X509Certificate certificate;

        public ActiveRsaKey(String kid, PrivateKey privateKey, PublicKey publicKey, X509Certificate certificate) {
            this.kid = kid;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.certificate = certificate;
        }

        public ActiveRsaKey(KeyWrapper keyWrapper) {
            this(keyWrapper.getKid(), (PrivateKey) keyWrapper.getPrivateKey(), (PublicKey) keyWrapper.getPublicKey(), keyWrapper.getCertificate());
        }

        public String getKid() {
            return kid;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }
    }

    /** 当前活动 HMAC 密钥。 */
    class ActiveHmacKey {
        private final String kid;
        private final SecretKey secretKey;

        public ActiveHmacKey(String kid, SecretKey secretKey) {
            this.kid = kid;
            this.secretKey = secretKey;
        }

        public String getKid() {
            return kid;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }
    }

    /** 当前活动 AES 密钥。 */
    class ActiveAesKey {
        private final String kid;
        private final SecretKey secretKey;

        public ActiveAesKey(String kid, SecretKey secretKey) {
            this.kid = kid;
            this.secretKey = secretKey;
        }

        public String getKid() {
            return kid;
        }

        public SecretKey getSecretKey() {
            return secretKey;
        }
    }


}
