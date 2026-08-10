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
package org.keycloak.keys;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * 自动生成 EC 密钥的工厂抽象基类：校验/生成椭圆曲线密钥对并支持回退密钥创建。
 * <p>子类指定曲线配置键、算法映射与支持的 {@link KeyUse}；配置变更或缺少密钥时自动重新生成 Base64 编码的 EC 公私钥。</p>
 */
public abstract class AbstractGeneratedEcKeyProviderFactory<T extends KeyProvider>
        extends AbstractEcKeyProviderFactory<T> {

    /** 默认 NIST 椭圆曲线名（如 P-256）。 */
    abstract protected String getDefaultEcEllipticCurve();

    /** 组件配置中椭圆曲线属性键。 */
    abstract protected String getEcEllipticCurveKey();

    /** 按 JWS/JWE 算法解析对应 NIST 曲线名。 */
    abstract protected String getEcEllipticCurveKey(String algorithm);

    /** 椭圆曲线配置项的 {@link ProviderConfigProperty} 描述。 */
    abstract protected ProviderConfigProperty getEcEllipticCurveProperty();

    /** 组件配置中 EC 私钥属性键。 */
    abstract protected String getEcPrivateKeyKey();

    /** 组件配置中 EC 公钥属性键。 */
    abstract protected String getEcPublicKeyKey();

    /** 子类日志记录器。 */
    abstract protected Logger getLogger();

    /** 判断算法是否由本子类 EC 工厂支持。 */
    abstract protected boolean isSupportedEcAlgorithm(String algorithm);

    /** 判断密钥用途（签名/加密）是否适用于本子类。 */
    abstract protected boolean isValidKeyUse(KeyUse keyUse);

    @Override
    /** 无匹配密钥时自动创建低优先级回退 EC 组件。 */
    public boolean createFallbackKeys(KeycloakSession session, KeyUse keyUse, String algorithm) {
        if (isValidKeyUse(keyUse) && isSupportedEcAlgorithm(algorithm)) {
            RealmModel realm = session.getContext().getRealm();

            ComponentModel generated = new ComponentModel();
            generated.setName("fallback-" + algorithm);
            generated.setParentId(realm.getId());
            generated.setProviderId(getId());
            generated.setProviderType(KeyProvider.class.getName());

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle(Attributes.PRIORITY_KEY, "-100");
            config.putSingle(getEcEllipticCurveKey(), getEcEllipticCurveKey(algorithm));
            generated.setConfig(config);

            realm.addComponentModel(generated);

            return true;
        } else {
            return false;
        }
    }

    @Override
    /** 校验曲线配置；缺少密钥或曲线变更时重新生成 EC 密钥对。 */
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        super.validateConfiguration(session, realm, model);

        ConfigurationValidationHelper.check(model).checkList(getEcEllipticCurveProperty(), false);

        String ecInNistRep = model.get(getEcEllipticCurveKey());
        if (ecInNistRep == null) ecInNistRep = getDefaultEcEllipticCurve();

        if (!(model.contains(getEcPrivateKeyKey()) && model.contains(getEcPublicKeyKey()))) {
            generateKeys(model, ecInNistRep);
            getLogger().debugv("Generated keys for {0}", realm.getName());
        } else {
            String currentEc = getCurveFromPublicKey(model.getConfig().getFirst(getEcPublicKeyKey()));
            if (!ecInNistRep.equals(currentEc)) {
                generateKeys(model, ecInNistRep);
                getLogger().debugv("Elliptic Curve changed, generating new keys for {0}", realm.getName());
            }
        }
    }

    /** 按 NIST 曲线名生成 EC 密钥对并写入组件配置。 */
    protected void generateKeys(ComponentModel model, String ecInNistRep) {
        KeyPair keyPair;
        try {
            keyPair = KeyUtils.generateEcKeyPair(convertECDomainParmNistRepToSecRep(ecInNistRep));
            model.put(getEcPrivateKeyKey(), Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            model.put(getEcPublicKeyKey(), Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            model.put(getEcEllipticCurveKey(), ecInNistRep);
        } catch (Throwable t) {
            throw new ComponentValidationException("Failed to generate EC keys", t);
        }
    }

    /** 从 Base64 编码公钥解析 NIST 曲线名（如 P-256）。 */
    protected String getCurveFromPublicKey(String publicEcKeyBase64Encoded) {
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicEcKeyBase64Encoded));
            ECPublicKey ecKey = (ECPublicKey) kf.generatePublic(publicKeySpec);
            return "P-" + ecKey.getParams().getCurve().getField().getFieldSize();
        } catch (Throwable t) {
            throw new ComponentValidationException("Failed to get EC from its public key", t);
        }
    }
}
