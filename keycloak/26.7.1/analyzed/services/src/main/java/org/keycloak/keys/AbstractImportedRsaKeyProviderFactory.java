/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.keycloak.common.util.CertificateUtils;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 导入外部 RSA 私钥的工厂抽象基类：校验 PEM 私钥、可选证书匹配与有效期。
 * <p>未提供证书时自动为导入密钥生成 V1 自签名 X509 证书；子类定义支持的 {@link KeyUse} 与 RSA 算法范围。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author <a href="mailto:f.b.rissi@gmail.com">Filipe Bojikian Rissi</a>
 */
public abstract class AbstractImportedRsaKeyProviderFactory extends AbstractRsaKeyProviderFactory {

    /** 构建含 priority/enabled/active/privateKey/certificate 的导入 RSA 配置模板。 */
    public final static ProviderConfigurationBuilder rsaKeyConfigurationBuilder() {
        return ProviderConfigurationBuilder.create()
                .property(Attributes.PRIORITY_PROPERTY)
                .property(Attributes.ENABLED_PROPERTY)
                .property(Attributes.ACTIVE_PROPERTY)
                .property(Attributes.PRIVATE_KEY_PROPERTY)
                .property(Attributes.CERTIFICATE_PROPERTY);
    }

    @Override
    /** 解码并校验 PEM 私钥；验证证书与私钥匹配及有效期，必要时生成自签名证书。 */
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        ConfigurationValidationHelper.check(model)
                .checkLong(Attributes.PRIORITY_PROPERTY, false)
                .checkBoolean(Attributes.ENABLED_PROPERTY, false)
                .checkBoolean(Attributes.ACTIVE_PROPERTY, false)
                .checkSingle(Attributes.PRIVATE_KEY_PROPERTY, true)
                .checkSingle(Attributes.CERTIFICATE_PROPERTY, false);

        KeyPair keyPair;
        try {
            PrivateKey privateKey = PemUtils.decodePrivateKey(model.get(Attributes.PRIVATE_KEY_KEY));
            PublicKey publicKey = KeyUtils.extractPublicKey(privateKey);
            keyPair = new KeyPair(publicKey, privateKey);
        } catch (Throwable t) {
            throw new ComponentValidationException("Failed to decode private key", t);
        }

        if (model.contains(Attributes.CERTIFICATE_KEY)) {
            X509Certificate certificate = null;
            try {
                certificate = PemUtils.decodeCertificate(model.get(Attributes.CERTIFICATE_KEY));
            } catch (Throwable t) {
                throw new ComponentValidationException("Failed to decode certificate", t);
            }

            if (certificate == null) {
                throw new ComponentValidationException("Failed to decode certificate");
            }

            if (!certificate.getPublicKey().equals(keyPair.getPublic())) {
                throw new ComponentValidationException("Certificate does not match private key");
            }

            try {
                certificate.checkValidity();
            } catch (CertificateException e) {
                throw new ComponentValidationException("Certificate is not valid", e);
            }
        } else {
            try {
                X509Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keyPair, realm.getName());
                model.put(Attributes.CERTIFICATE_KEY, PemUtils.encodeCertificate(certificate));
            } catch (Throwable t) {
                throw new ComponentValidationException("Failed to generate self-signed certificate", t);
            }
        }
    }

    /** 判断密钥用途是否适用于本子类导入 RSA 工厂。 */
    abstract protected boolean isValidKeyUse(KeyUse keyUse);

    /** 判断 RSA 算法是否由本子类支持。 */
    abstract protected boolean isSupportedRsaAlgorithm(String algorithm);

}
