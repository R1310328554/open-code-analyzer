/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.keycloak.Config;
import org.keycloak.common.util.CertificateUtils;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.PemUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.jboss.logging.Logger;

/**
 * 自动生成 RSA 密钥对的工厂抽象基类：校验密钥长度、生成 PEM 私钥与自签名证书。
 * <p>支持按 {@link KeyUse} 与 RSA 算法创建回退组件；密钥长度变更时自动重新生成密钥与 X509 证书。</p>
 */
public abstract class AbstractGeneratedRsaKeyProviderFactory extends AbstractRsaKeyProviderFactory {

    /** 默认 RSA 模数位长（2048 位）。 */
    private int defaultKeySize = 2048;

    /** 子类日志记录器。 */
    abstract protected Logger getLogger();

    /** 构建含 priority/enabled/active 的 RSA 组件配置模板。 */
    public final static ProviderConfigurationBuilder rsaKeyConfigurationBuilder() {
        return ProviderConfigurationBuilder.create()
                .property(Attributes.PRIORITY_PROPERTY)
                .property(Attributes.ENABLED_PROPERTY)
                .property(Attributes.ACTIVE_PROPERTY);
    }

    /** 在标准 RSA 配置上追加密钥长度（keySize）属性。 */
    protected ProviderConfigurationBuilder generatedRsaKeyConfigurationBuilder() {
        ProviderConfigProperty prop = Attributes.KEY_SIZE_PROPERTY.get();
        prop.setDefaultValue(defaultKeySize);
        return rsaKeyConfigurationBuilder().property(prop);
    }

    @Override
    /** 从 SPI 配置读取默认 RSA 密钥长度。 */
    public void init(Config.Scope config) {
        this.defaultKeySize = config.getInt(Attributes.KEY_SIZE_KEY, 2048);
    }

    @Override
    /** 无匹配密钥时自动创建低优先级回退 RSA 组件。 */
    public boolean createFallbackKeys(KeycloakSession session, KeyUse keyUse, String algorithm) {
        if (isValidKeyUse(keyUse) && isSupportedRsaAlgorithm(algorithm)) {
            RealmModel realm = session.getContext().getRealm();

            ComponentModel generated = new ComponentModel();
            generated.setName("fallback-" + algorithm);
            generated.setParentId(realm.getId());
            generated.setProviderId(getId());
            generated.setProviderType(KeyProvider.class.getName());

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle(Attributes.PRIORITY_KEY, "-100");
            config.putSingle(Attributes.ALGORITHM_KEY, algorithm);
            generated.setConfig(config);

            realm.addComponentModel(generated);

            return true;
        } else {
            return false;
        }
    }

    /** 判断密钥用途是否适用于本子类 RSA 工厂。 */
    abstract protected boolean isValidKeyUse(KeyUse keyUse);

    /** 判断 RSA 签名/加密算法是否由本子类支持。 */
    abstract protected boolean isSupportedRsaAlgorithm(String algorithm);

    @Override
    /** 校验 keySize；缺少密钥或长度变更时重新生成 RSA 密钥对与证书。 */
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        super.validateConfiguration(session, realm, model);

        ConfigurationValidationHelper.check(model).checkList(Attributes.KEY_SIZE_PROPERTY.get(), false);

        int size = model.get(Attributes.KEY_SIZE_KEY, this.defaultKeySize);

        if (!(model.contains(Attributes.PRIVATE_KEY_KEY) && model.contains(Attributes.CERTIFICATE_KEY))) {
            generateKeys(realm, model, size);

            getLogger().debugv("Generated keys for {0}", realm.getName());
        } else {
            PrivateKey privateKey = PemUtils.decodePrivateKey(model.get(Attributes.PRIVATE_KEY_KEY));
            int currentSize = ((RSAPrivateKey) privateKey).getModulus().bitLength();
            if (currentSize != size) {
                generateKeys(realm, model, size);

                getLogger().debugv("Key size changed, generating new keys for {0}", realm.getName());
            }
        }
    }

    /** 生成指定位长的 RSA 密钥对并写入 PEM 私钥。 */
    private void generateKeys(RealmModel realm, ComponentModel model, int size) {
        KeyPair keyPair;
        try {
            keyPair = KeyUtils.generateRsaKeyPair(size);
            model.put(Attributes.PRIVATE_KEY_KEY, PemUtils.encodeKey(keyPair.getPrivate()));
        } catch (Throwable t) {
            getLogger().warnf("Failed to generate keys for key provider '%s' in realm '%s'. Details: %s", model.getName(), realm.getName(), t.getMessage());
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(t.getMessage(), t);
            }
            throw new ComponentValidationException("Failed to generate keys", t);
        }

        generateCertificate(realm, model, keyPair);
    }

    /** 为 RSA 密钥对生成 V1 自签名 X509 证书并写入配置。 */
    private void generateCertificate(RealmModel realm, ComponentModel model, KeyPair keyPair) {
        try {
            Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keyPair, realm.getName());
            model.put(Attributes.CERTIFICATE_KEY, PemUtils.encodeCertificate(certificate));
        } catch (Throwable t) {
            getLogger().warnf("Failed to generate certificate for key provider '%s' in realm '%s'. Details: %s", model.getName(), realm.getName(), t.getMessage());
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(t.getMessage(), t);
            }
            throw new ComponentValidationException("Failed to generate certificate", t);
        }
    }
}
