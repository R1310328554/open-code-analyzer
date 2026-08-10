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

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Map;

import org.keycloak.crypto.KeyUse;

/**
 * Realm 签名/加密密钥元数据的 REST 表示，用于 Keys API 查询当前活跃密钥。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeysMetadataRepresentation {

    /** 算法/用途到当前活跃密钥 kid 的映射。 */
    private Map<String, String> active;

    /** 所有密钥的元数据列表。 */
    private List<KeyMetadataRepresentation> keys;

    /** @return 活跃密钥映射 */
    public Map<String, String> getActive() {
        return active;
    }

    /** @param active 活跃密钥映射 */
    public void setActive(Map<String, String> active) {
        this.active = active;
    }

    /** @return 密钥元数据列表 */
    public List<KeyMetadataRepresentation> getKeys() {
        return keys;
    }

    /** @param keys 密钥元数据列表 */
    public void setKeys(List<KeyMetadataRepresentation> keys) {
        this.keys = keys;
    }

    /**
     * 单个密钥的元数据表示。
     */
    public static class KeyMetadataRepresentation {
        /** 密钥提供方 ID。 */
        private String providerId;
        /** 提供方优先级。 */
        private long providerPriority;

        /** 密钥 ID（kid）。 */
        private String kid;

        /** 密钥状态（如 ACTIVE、PASSIVE 等）。 */
        private String status;

        /** 密钥类型（如 RSA、EC 等）。 */
        private String type;
        /** 签名/加密算法名称。 */
        private String algorithm;

        /** PEM 编码的公钥。 */
        private String publicKey;
        /** PEM 编码的 X.509 证书。 */
        private String certificate;
        /** 密钥用途（签名或加密）。 */
        private KeyUse use;
        /** 密钥过期时间（Unix 毫秒）。 */
        private Long validTo;

        /** @return 密钥提供方 ID */
        public String getProviderId() {
            return providerId;
        }

        /** @param providerId 密钥提供方 ID */
        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        /** @return 提供方优先级 */
        public long getProviderPriority() {
            return providerPriority;
        }

        /** @param providerPriority 提供方优先级 */
        public void setProviderPriority(long providerPriority) {
            this.providerPriority = providerPriority;
        }

        /** @return 密钥 ID（kid） */
        public String getKid() {
            return kid;
        }

        /** @param kid 密钥 ID（kid） */
        public void setKid(String kid) {
            this.kid = kid;
        }

        /** @return 密钥状态 */
        public String getStatus() {
            return status;
        }

        /** @param status 密钥状态 */
        public void setStatus(String status) {
            this.status = status;
        }

        /** @return 密钥类型 */
        public String getType() {
            return type;
        }

        /** @param type 密钥类型 */
        public void setType(String type) {
            this.type = type;
        }

        /** @return 算法名称 */
        public String getAlgorithm() {
            return algorithm;
        }

        /** @param algorithm 算法名称 */
        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        /** @return PEM 公钥 */
        public String getPublicKey() {
            return publicKey;
        }

        /** @param publicKey PEM 公钥 */
        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        /** @return PEM 证书 */
        public String getCertificate() {
            return certificate;
        }

        /** @param certificate PEM 证书 */
        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        /** @return 密钥用途 */
        public KeyUse getUse() {
            return use;
        }

        /** @param use 密钥用途 */
        public void setUse(KeyUse use) {
            this.use = use;
        }

        /** @return 过期时间（毫秒） */
        public Long getValidTo() {
            return validTo;
        }

        /** @param validTo 过期时间（毫秒） */
        public void setValidTo(Long validTo) {
            this.validTo = validTo;
        }
    }
}
