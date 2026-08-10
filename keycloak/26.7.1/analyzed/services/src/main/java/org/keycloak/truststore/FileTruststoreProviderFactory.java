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

package org.keycloak.truststore;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.x500.X500Principal;

import org.keycloak.Config;
import org.keycloak.common.enums.HostnameVerificationPolicy;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.config.HttpOptions;
import org.keycloak.config.ProxyOptions;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.jboss.logging.Logger;

/**
 * 文件型 {@link TruststoreProviderFactory}，从配置或系统属性加载信任库并注册单例。
 * <p>已弃用的 {@code spi-truststore-file-*} 选项仍受支持，建议使用 {@code truststore-paths}。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class FileTruststoreProviderFactory implements TruststoreProviderFactory {

    /** 主机名校验策略配置键。 */
    static final String HOSTNAME_VERIFICATION_POLICY = "hostname-verification-policy";

    private static final Logger log = Logger.getLogger(FileTruststoreProviderFactory.class);

    private TruststoreProvider provider;

    @Override
    public TruststoreProvider create(KeycloakSession session) {
        return provider;
    }

    // 仅供测试注入 mock 提供者
    public void setProvider(TruststoreProvider provider) {
        this.provider = provider;
    }

    /** 从配置、系统属性或 JRE 默认 cacerts 加载信任库并初始化提供者。 */
    @Override
    public void init(Config.Scope config) {

        String storepath = config.get("file");
        String pass = config.get("password");
        String policy = config.get(HOSTNAME_VERIFICATION_POLICY);
        String configuredType = config.get("type");

        if (storepath != null || pass != null || configuredType != null) {
            log.warn("Using deprecated 'spi-truststore-file-*' options. Consider using 'truststore-paths' option.");
        }

        HostnameVerificationPolicy verificationPolicy = null;
        KeyStore truststore = null;
        boolean system = false;
        if (storepath == null) {
            storepath = System.getProperty(TruststoreBuilder.SYSTEM_TRUSTSTORE_KEY);
            if (storepath == null) {
                File defaultTrustStore = TruststoreBuilder.getJRETruststore();
                if (!defaultTrustStore.exists()) {
                    throw new RuntimeException("Attribute 'file' missing in 'truststore':'file' configuration, and could not find the system truststore");
                }
                storepath = defaultTrustStore.getAbsolutePath();
                system = true;
            }
            // should there be an exception if pass / type are configured for the spi-truststore
            pass = System.getProperty(TruststoreBuilder.SYSTEM_TRUSTSTORE_PASSWORD_KEY, system ? "changeit" : null);
            configuredType = System.getProperty(TruststoreBuilder.SYSTEM_TRUSTSTORE_TYPE_KEY);
        }
        String type = KeystoreUtil.getKeystoreType(configuredType, storepath, KeyStore.getDefaultType());
        try {
            truststore = KeystoreUtil.loadKeyStore(storepath, pass, type);
        } catch (Exception e) {
            // FIPS 模式下默认类型可能为 PKCS12，但 cacerts 仍为 JKS，需回退尝试
            if (system && !"jks".equalsIgnoreCase(type)) {
                try {
                    truststore = KeystoreUtil.loadKeyStore(storepath, pass, "jks");
                } catch (Exception e1) {
                }
            }
            if (truststore == null) {
                throw new RuntimeException("Failed to initialize TruststoreProviderFactory: " + new File(storepath).getAbsolutePath() + ", truststore type: " + type, e);
            }
        }
        if (policy == null) {
            verificationPolicy = HostnameVerificationPolicy.DEFAULT;
        } else {
            try {
                verificationPolicy = HostnameVerificationPolicy.valueOf(policy);
            } catch (Exception e) {
                throw new RuntimeException("Invalid value for 'hostname-verification-policy': " + policy
                        + " (must be one of: " + Stream.of(HostnameVerificationPolicy.values())
                                .map(HostnameVerificationPolicy::name).collect(Collectors.joining(", "))
                        + ")");
            }
        }

        // 若启动时配置了 HTTPS 信任库则单独加载
        String httpsTrustStoreFile = config.root().get(HttpOptions.HTTPS_TRUST_STORE_FILE.getKey());
        KeyStore httpsTruststore = null;
        TruststoreCertificatesLoader httpsCertsLoader = null;
        if (httpsTrustStoreFile != null && config.root().get(ProxyOptions.PROXY_HEADERS.getKey()) == null) {
            try {
                String httpsTrustStorePassword = config.root().get(HttpOptions.HTTPS_TRUST_STORE_PASSWORD.getKey());
                String httpsTrustStoreType = config.root().get(HttpOptions.HTTPS_TRUST_STORE_TYPE.getKey());
                final String truststoreType = KeystoreUtil.getTruststoreType(httpsTrustStoreType, httpsTrustStoreFile, KeyStore.getDefaultType());
                if (KeystoreUtil.TruststoreFormat.PEM.name().equalsIgnoreCase(truststoreType)) {
                    httpsTruststore = TruststoreBuilder.createPkcs12KeyStore();
                    TruststoreBuilder.mergePemFile(httpsTruststore, httpsTrustStoreFile, true);
                } else {
                    httpsTruststore = KeystoreUtil.loadKeyStore(httpsTrustStoreFile, httpsTrustStorePassword, httpsTrustStoreType);
                }
                httpsCertsLoader = new TruststoreCertificatesLoader(httpsTruststore);
            } catch (Exception e) {
                log.debugf(e, "Error loading HTTPS trust-store file '%s'", httpsTrustStoreFile);
            }
        }

        TruststoreCertificatesLoader certsLoader = new TruststoreCertificatesLoader(truststore);
        provider = new FileTruststoreProvider(truststore, verificationPolicy, Collections.unmodifiableMap(certsLoader.trustedRootCerts),
                Collections.unmodifiableMap(certsLoader.intermediateCerts),
                httpsTruststore,
                httpsCertsLoader != null ? Collections.unmodifiableMap(httpsCertsLoader.trustedRootCerts) : null,
                httpsCertsLoader != null ? Collections.unmodifiableMap(httpsCertsLoader.intermediateCerts) : null
        );
        TruststoreProviderSingleton.set(provider);
        log.debugf("File truststore provider initialized: %s, Truststore type: %s",  new File(storepath).getAbsolutePath(), type);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "file";
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("file")
                .type("string")
                .helpText("DEPRECATED: The file path of the trust store from where the certificates are going to be read from to validate TLS connections.")
                .add()
                .property()
                .name("password")
                .type("string")
                .helpText("DEPRECATED: The trust store password.")
                .add()
                .property()
                .name(HOSTNAME_VERIFICATION_POLICY)
                .type("string")
                .helpText("DEPRECATED: The hostname verification policy.")
                .options(Arrays.stream(HostnameVerificationPolicy.values()).map(HostnameVerificationPolicy::name).toArray(String[]::new))
                .defaultValue(HostnameVerificationPolicy.DEFAULT.name())
                .add()
                .property()
                .name("type")
                .type("string")
                .helpText("DEPRECATED: Type of the truststore. If not provided, the type would be detected based on the truststore file extension or platform default type.")
                .add()
                .build();
    }

    /** 从 Keycloak 信任库读取证书，按根 CA 与中间 CA 分类。 */
    private static class TruststoreCertificatesLoader {

        /** 按主题 DN 索引的自签名根 CA 证书。 */
        private Map<X500Principal, List<X509Certificate>> trustedRootCerts = new HashMap<>();
        /** 按主题 DN 索引的中间 CA 证书。 */
        private Map<X500Principal, List<X509Certificate>> intermediateCerts = new HashMap<>();


        public TruststoreCertificatesLoader(KeyStore truststore) {
            readTruststore(truststore);
        }

        /**
         * 遍历信任库别名，将 X.509 证书分类为根 CA 与中间 CA 两类列表。
         */
        private void readTruststore(KeyStore truststore) {

            // 读取信任库别名与证书条目
            Enumeration<String> enumeration;

            try {

                enumeration = truststore.aliases();
                log.trace("Checking " + truststore.size() + " entries from the truststore.");
                while(enumeration.hasMoreElements()) {
                    String alias = enumeration.nextElement();
                    readTruststoreEntry(truststore, alias);
                }
            } catch (KeyStoreException e) {
                log.error("Error while reading Keycloak truststore "+e.getMessage(),e);
            }
        }

        private void readTruststoreEntry(KeyStore truststore, String alias) {
            try {
                Certificate certificate = truststore.getCertificate(alias);

                if (certificate instanceof X509Certificate) {
                    X509Certificate cax509cert = (X509Certificate) certificate;
                    if (isSelfSigned(cax509cert)) {
                        X500Principal principal = cax509cert.getSubjectX500Principal();
                        List<X509Certificate> certs = trustedRootCerts.get(principal);
                        if (certs == null) {
                            certs = new ArrayList<>();
                            trustedRootCerts.put(principal, certs);
                        }
                        certs.add(cax509cert);
                        log.debug("Trusted root CA found in truststore : alias : " + alias + " | Subject DN : " + principal);
                    } else {
                        X500Principal principal = cax509cert.getSubjectX500Principal();
                        List<X509Certificate> certs = intermediateCerts.get(principal);
                        if (certs == null) {
                            certs = new ArrayList<>();
                            intermediateCerts.put(principal, certs);
                        }
                        certs.add(cax509cert);
                        log.debug("Intermediate CA found in truststore : alias : " + alias + " | Subject DN : " + principal);
                    }
                } else
                    log.info("Skipping certificate with alias [" + alias + "] from truststore, because it's not an X509Certificate");
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException e) {
                log.warnf("Error while reading Keycloak truststore entry [%s]. Exception message: %s", alias, e.getMessage(), e);
            }
        }

        /**
         * 判断给定 X.509 证书是否为自签名（根 CA）。
         */
        private boolean isSelfSigned(X509Certificate cert)
                throws CertificateException, NoSuchAlgorithmException,
                NoSuchProviderException {
            try {
                // 尝试用证书自身公钥验证签名
                PublicKey key = cert.getPublicKey();
                cert.verify(key);
                log.trace("certificate " + cert.getSubjectDN() + " detected as root CA");
                return true;
            } catch (SignatureException sigEx) {
                // 签名无效，视为中间 CA
                log.trace("certificate " + cert.getSubjectDN() + " detected as intermediate CA");
            } catch (InvalidKeyException keyEx) {
                // 公钥无效，视为中间 CA
                log.trace("certificate " + cert.getSubjectDN() + " detected as intermediate CA");
            }
            return false;
        }
    }
}
