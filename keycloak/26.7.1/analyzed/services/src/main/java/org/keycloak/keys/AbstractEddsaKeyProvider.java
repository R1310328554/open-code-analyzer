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
package org.keycloak.keys;

import java.security.KeyPair;
import java.util.stream.Stream;

import org.keycloak.common.util.KeyUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyStatus;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.RealmModel;

/**
 * EdDSA（Ed25519/Ed448）密钥提供者抽象基类：加载 OKP 类型密钥对并封装为 {@link KeyWrapper}。
 * <p>子类实现 {@link #loadKey} 以支持生成或导入 EdDSA 密钥；算法固定为 {@link Algorithm#EdDSA}，用途为签名（{@link KeyUse#SIG}）。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractEddsaKeyProvider implements KeyProvider {

    /** 密钥启用/活跃状态。 */
    private final KeyStatus status;

    /** 密钥组件配置模型。 */
    private final ComponentModel model;

    /** 已加载的 EdDSA 密钥包装对象。 */
    private final KeyWrapper key;

    /** 从组件配置加载 EdDSA 密钥；结果缓存在 model note 中避免重复加载。 */
    public AbstractEddsaKeyProvider(RealmModel realm, ComponentModel model) {
        this.model = model;
        this.status = KeyStatus.from(model.get(Attributes.ACTIVE_KEY, true), model.get(Attributes.ENABLED_KEY, true));

        if (model.hasNote(KeyWrapper.class.getName())) {
            key = model.getNote(KeyWrapper.class.getName());
        } else {
            key = loadKey(realm, model);
            model.setNote(KeyWrapper.class.getName(), key);
        }
    }

    /** 子类实现：从配置或密钥库加载 EdDSA 密钥对。 */
    protected abstract KeyWrapper loadKey(RealmModel realm, ComponentModel model);

    @Override
    /** @return 包含单个 EdDSA 密钥的流 */
    public Stream<KeyWrapper> getKeysStream() {
        return Stream.of(key);
    }

    /** 由 EdDSA 密钥对构建 {@link KeyWrapper}，设置 kid、曲线名与 OKP 类型。 */
    protected KeyWrapper createKeyWrapper(KeyPair keyPair, String curveName) {
        KeyWrapper key = new KeyWrapper();

        key.setProviderId(model.getId());
        key.setProviderPriority(model.get("priority", 0l));

        key.setKid(KeyUtils.createKeyId(keyPair.getPublic()));
        key.setUse(KeyUse.SIG);
        key.setType(KeyType.OKP);
        key.setAlgorithm(Algorithm.EdDSA);
        key.setCurve(curveName);
        key.setStatus(status);
        key.setPrivateKey(keyPair.getPrivate());
        key.setPublicKey(keyPair.getPublic());

        return key;
    }
}
