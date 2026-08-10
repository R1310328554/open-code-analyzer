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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.jboss.logging.Logger;

import static org.keycloak.keys.Attributes.KID_KEY;
import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

/**
 * Java 密钥库密钥 {@link KeyProviderFactory}，ID 为 {@code java-keystore}。
 * <p>从领域隔离的 keystores 目录加载 JKS/PKCS12 等文件，校验证书链并支持 Vault 密码引用。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JavaKeystoreKeyProviderFactory implements KeyProviderFactory {
    private static final Logger logger = Logger.getLogger(JavaKeystoreKeyProviderFactory.class);

    /** 工厂标识 {@code java-keystore}。 */
    public static final String ID = "java-keystore";

    /** SPI 初始化参数：密钥库文件父目录路径键。 */
    public static final String KEYSTORES_PATH_INIT_KEY = "keystores-path";

    public static final String KEYSTORE_KEY = "keystore";
    public static final ProviderConfigProperty KEYSTORE_PROPERTY = new ProviderConfigProperty(KEYSTORE_KEY, "Keystore",
            """
            Path to the keystore file. The keystore should be located inside a folder named like the realm name inside
            the main keystores directory (by default `data` directory under {project_name}'s installation folder). For a
            realm called `test` the keystore file should located inside `${kc.home.dir}/data/test`. This way the keystore
            file is isolated between different realms. If the path is relative, the file will be located from that folder.
            """,
            STRING_TYPE, null);

    public static final String KEYSTORE_PASSWORD_KEY = "keystorePassword";
    public static final ProviderConfigProperty KEYSTORE_PASSWORD_PROPERTY = new ProviderConfigProperty(KEYSTORE_PASSWORD_KEY, "Keystore Password", "Password for the keys", STRING_TYPE, null, true);

    public static final String KEYSTORE_TYPE_KEY = "keystoreType";

    // 密钥库类型配置项延迟至 init() 初始化（需 CryptoProvider 就绪）
    private ProviderConfigProperty keystoreTypeProperty;

    public static final String KEY_ALIAS_KEY = "keyAlias";
    public static final ProviderConfigProperty KEY_ALIAS_PROPERTY = new ProviderConfigProperty(KEY_ALIAS_KEY, "Key Alias", "Alias for the private key", STRING_TYPE, null);

    public static final String KEY_PASSWORD_KEY = "keyPassword";
    public static final ProviderConfigProperty KEY_PASSWORD_PROPERTY = new ProviderConfigProperty(KEY_PASSWORD_KEY, "Key Password", "Password for the private key", STRING_TYPE, null, true);

    private static final String HELP_TEXT = "Loads keys from a Java keys file";

    private List<ProviderConfigProperty> configProperties;
    private Path keystoresPath;

    /** 解析 keystores 根目录并构建支持的密钥库类型配置项。 */
    @Override
    public void init(Config.Scope config) {
        this.keystoresPath = Paths.get(config.get(KEYSTORES_PATH_INIT_KEY, System.getProperty("kc.home.dir") + "/data")).normalize();
        String[] supportedKeystoreTypes = CryptoIntegration.getProvider().getSupportedKeyStoreTypes()
                .map(KeystoreUtil.KeystoreFormat::toString)
                .toArray(String[]::new);
        this.keystoreTypeProperty = new ProviderConfigProperty(KEYSTORE_TYPE_KEY, "Keystore Type",
                "Keystore type. This parameter is not mandatory. If omitted, the type will be detected from keystore file or default keystore type will be used", LIST_TYPE,
                supportedKeystoreTypes.length > 0 ? supportedKeystoreTypes[0] : null, supportedKeystoreTypes);

        configProperties = ProviderConfigurationBuilder.create()
                .property(Attributes.PRIORITY_PROPERTY)
                .property(Attributes.ENABLED_PROPERTY)
                .property(Attributes.ACTIVE_PROPERTY)
                .property(mergedAlgorithmProperties())
                .property(KEYSTORE_PROPERTY)
                .property(KEYSTORE_PASSWORD_PROPERTY)
                .property(keystoreTypeProperty)
                .property(KEY_ALIAS_PROPERTY)
                .property(KEY_PASSWORD_PROPERTY)
                .property(Attributes.KEY_USE_PROPERTY)
                .build();
    }

    /** 创建 {@link JavaKeystoreKeyProvider}，传入 Vault 转录器解析密码。 */
    @Override
    public KeyProvider create(KeycloakSession session, ComponentModel model) {
        return new JavaKeystoreKeyProvider(keystoresPath, session.getContext().getRealm(), model, session.vault());
    }

    /** 校验配置、限制密钥库路径在领域目录内，并试加载密钥验证证书链。 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        String kid = model.get(KID_KEY);

        if (kid == null) {
            kid = KeycloakModelUtils.generateId();
            model.put(KID_KEY, kid);
        }

        ConfigurationValidationHelper.check(model)
                .checkLong(Attributes.PRIORITY_PROPERTY, false)
                .checkBoolean(Attributes.ENABLED_PROPERTY, false)
                .checkBoolean(Attributes.ACTIVE_PROPERTY, false)
                .checkSingle(KEYSTORE_PROPERTY, true)
                .checkSingle(KEYSTORE_PASSWORD_PROPERTY, true)
                .checkSingle(keystoreTypeProperty, false)
                .checkSingle(KEY_ALIAS_PROPERTY, true)
                .checkSingle(KEY_PASSWORD_PROPERTY, true);

        Path keystorePath = Paths.get(model.get(KEYSTORE_KEY)).normalize();
        if (!keystorePath.isAbsolute()) {
            keystorePath = this.keystoresPath.resolve(realm.getName()).resolve(keystorePath);
        }
        if (!keystorePath.startsWith(keystoresPath.resolve(realm.getName()))) {
            throw new ComponentValidationException(String.format(
                    "Keystore file '%s' is not under the realm directory '%s'", keystorePath, keystoresPath.resolve(realm.getName())));
        }

        try {
            KeyWrapper key = new JavaKeystoreKeyProvider(keystoresPath, realm, model, session.vault()).loadKey(keystoresPath, realm, model);
            validateCertificateChain(key.getCertificateChain());
        } catch(GeneralSecurityException e) {
            logger.error("Failed to load keys.", e);
            throw new ComponentValidationException("Certificate error on server. " + e.getMessage(), e);
        } catch (Throwable t) {
            logger.error("Failed to load keys.", t);
            throw new ComponentValidationException("Failed to load keys. " + t.getMessage(), t);
        }
    }

    /**
     * 校验密钥库条目中的 X509 证书链（若存在）。
     * <p>以链末证书为信任锚进行 PKIX 验证。</p>
     *
     * @param certificates 证书链
     * @throws GeneralSecurityException 验证失败
     */
    private static void validateCertificateChain(List<X509Certificate> certificates) throws GeneralSecurityException {
        if (certificates == null || certificates.isEmpty()) {
            return;
        }

        Set<TrustAnchor> anchors = new HashSet<>();

        // 将证书链最后一个证书视为信任锚
        anchors.add(new TrustAnchor(certificates.get(certificates.size() - 1), null));

        PKIXParameters params = new PKIXParameters(anchors);

        params.setRevocationEnabled(false);

        CertPath certPath = CertificateFactory.getInstance("X.509").generateCertPath(certificates);
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());

        validator.validate(certPath, params);
    }

    // 合并 RSA/EC/HMAC/AES/EdDSA/ECDH 支持的算法为单一配置项
    private static ProviderConfigProperty mergedAlgorithmProperties() {
        List<String> algorithms = Stream.of(
                        List.of(Algorithm.AES, Algorithm.EdDSA),
                        List.of(Algorithm.ES256, Algorithm.ES384, Algorithm.ES512),
                        Attributes.HS_ALGORITHM_PROPERTY.getOptions(),
                        Attributes.RS_ALGORITHM_PROPERTY.getOptions(),
                        Attributes.RS_ENC_ALGORITHM_PROPERTY.getOptions(),
                        GeneratedEcdhKeyProviderFactory.ECDH_ALGORITHM_PROPERTY.getOptions())
                .flatMap(Collection::stream)
                .toList();
        return new ProviderConfigProperty(Attributes.RS_ALGORITHM_PROPERTY.getName(), Attributes.RS_ALGORITHM_PROPERTY.getLabel(),
                Attributes.RS_ALGORITHM_PROPERTY.getHelpText(), Attributes.RS_ALGORITHM_PROPERTY.getType(),
                Attributes.RS_ALGORITHM_PROPERTY.getDefaultValue(), algorithms.toArray(String[]::new));

    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return this.configProperties;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(KEYSTORES_PATH_INIT_KEY)
                .type("string")
                .helpText(
                        """
                        The parent directory where the keystore files should be placed. The default value is the keycloak
                        data folder "${kc.home.dir}/data". In order to isolate keystores between realms, the final keystore
                        files should be placed in a folder with the realm name inside this directory. For example:
                        "${kc.home.dir}/data/{realm-name}/keystore.jks".
                        """
                )
                .add()
                .build();
    }
}
