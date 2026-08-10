/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.stream.Stream;
import javax.crypto.SecretKey;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;

/**
 * 自动生成对称密钥（SecretKey）的提供者抽象基类：从组件配置加载密钥并封装为 {@link KeyWrapper}。
 * <p>密钥材料以 Base64Url 存储；首次加载后缓存在 model note 中避免重复解码。子类指定 {@link KeyUse}、{@link KeyType} 与算法名称。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class AbstractGeneratedSecretKeyProvider implements KeyProvider {

    /** 密钥启用/活跃状态。 */
    private final KeyStatus status;
    /** 密钥组件配置模型。 */
    private final ComponentModel model;
    /** 密钥标识 kid。 */
    private final String kid;
    /** 已加载的对称密钥。 */
    private final SecretKey secretKey;
    /** 密钥用途（签名 SIG 或加密 ENC）。 */
    private final KeyUse use;
    /** JWK 密钥类型（如 OCT）。 */
    private String type;
    /** 密钥算法名称（如 AES、HS256）。 */
    private final String algorithm;

    /** @param model 组件配置 @param use 密钥用途 @param type JWK 类型 @param algorithm 算法名称 */
    public AbstractGeneratedSecretKeyProvider(ComponentModel model, KeyUse use, String type, String algorithm) {
        this.status = KeyStatus.from(model.get(Attributes.ACTIVE_KEY, true), model.get(Attributes.ENABLED_KEY, true));
        this.kid = model.get(Attributes.KID_KEY);
        this.model = model;
        this.use = use;
        this.type = type;
        this.algorithm = algorithm;

        if (model.hasNote(SecretKey.class.getName())) {
            secretKey = model.getNote(SecretKey.class.getName());
        } else {
            secretKey = KeyUtils.loadSecretKey(Base64Url.decode(model.get(Attributes.SECRET_KEY)), JavaAlgorithm.getJavaAlgorithm(algorithm));
            model.setNote(SecretKey.class.getName(), secretKey);
        }
    }

    @Override
    /** @return 包含单个对称密钥的流 */
    public Stream<KeyWrapper> getKeysStream() {
        KeyWrapper key = new KeyWrapper();

        key.setProviderId(model.getId());
        key.setProviderPriority(model.get("priority", 0l));

        key.setKid(kid);
        key.setUse(use);
        key.setType(type);
        key.setAlgorithm(algorithm);
        key.setStatus(status);
        key.setSecretKey(secretKey);

        return Stream.of(key);
    }

    @Override
    /** 无资源需释放。 */
    public void close() {
    }

}
