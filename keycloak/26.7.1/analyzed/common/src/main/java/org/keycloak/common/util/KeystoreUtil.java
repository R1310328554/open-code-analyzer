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

package org.keycloak.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.keycloak.common.constants.GenericConstants;
import org.keycloak.common.crypto.CryptoIntegration;

/**
 * 密钥库/信任库加载与格式探测工具。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeystoreUtil {

    /** 密钥库/信任库格式的公共描述。 */
    public interface StoreFormat {
        List<String> getFileExtensions();
        String getPrimaryExtension();
    };

    /** 信任库常见文件格式。 */
    public enum TruststoreFormat implements StoreFormat {
        JKS("jks", "truststore"),
        PKCS12("p12", "pfx", "pkcs12"),
        PEM("pem", "ca", "crt"),
        BCFKS("bcfks");

        // 该格式对应的典型文件扩展名
        private final List<String> fileExtensions;

        TruststoreFormat(String... extensions) {
            this.fileExtensions = Arrays.asList(extensions);
        }

        @Override
        public List<String> getFileExtensions() {
            return fileExtensions;
        }

        @Override
        public String getPrimaryExtension() {
            return fileExtensions.get(0);
        }
    }

    /** 密钥库常见文件格式。 */
    public enum KeystoreFormat implements StoreFormat {
        JKS("jks"),
        PKCS12("p12", "pfx", "pkcs12"),
        BCFKS("bcfks");

        // 该格式对应的典型文件扩展名
        private final List<String> fileExtensions;
        KeystoreFormat(String... extensions) {
            this.fileExtensions = Arrays.asList(extensions);
        }

        @Override
        public List<String> getFileExtensions() {
            return fileExtensions;
        }

        @Override
        public String getPrimaryExtension() {
            return fileExtensions.get(0);
        }
    }

    /** 从文件路径加载 {@link KeyStore}，类型由扩展名或 {@code preferedType} 推断。 */
    public static KeyStore loadKeyStore(String filename, String password) throws Exception {
        return loadKeyStore(filename, password, null);
    }

    public static KeyStore loadKeyStore(String filename, String password, String preferedType) throws Exception {
        String keystoreType = getKeystoreType(preferedType, filename, KeyStore.getDefaultType());
        KeyStore trustStore = KeyStore.getInstance(keystoreType);
        InputStream trustStream = null;
        if (filename.startsWith(GenericConstants.PROTOCOL_CLASSPATH)) {
            String resourcePath = filename.replace(GenericConstants.PROTOCOL_CLASSPATH, "");
            if (Thread.currentThread().getContextClassLoader() != null) {
                trustStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }
            if (trustStream == null) {
                trustStream = KeystoreUtil.class.getResourceAsStream(resourcePath);
            }
            if (trustStream == null) {
                throw new RuntimeException("Unable to find key store in classpath");
            }
        } else {
            trustStream = new FileInputStream(new File(filename));
        }
        try (InputStream is = trustStream) {
            trustStore.load(is, password == null ? null : password.toCharArray());
        }
        return trustStore;
    }

    /** 从密钥库按别名加载 RSA/通用密钥对。 */
    public static KeyPair loadKeyPairFromKeystore(String keystoreFile, String storePassword, String keyPassword, String keyAlias, KeystoreFormat format) {
        try (InputStream stream = FindFile.findFile(keystoreFile)) {
            KeyStore keyStore = CryptoIntegration.getProvider().getKeyStore(format);

            keyStore.load(stream, storePassword.toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("Couldn't load key with alias '" + keyAlias + "' from keystore");
            }
            PublicKey publicKey = keyStore.getCertificate(keyAlias).getPublicKey();
            if (publicKey == null) {
                throw new RuntimeException("Couldn't load public key with alias '" + keyAlias + "' from keystore");
            }
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key: " + e.getMessage(), e);
        }
    }

    private static <T extends StoreFormat> Optional<T> getStoreFormat(String path, T[] values) {
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex > -1) {
            String ext = path.substring(lastDotIndex + 1).toLowerCase();
            return Arrays.stream(values)
                    .filter(ksFormat -> ksFormat.getFileExtensions().contains(ext))
                    .findFirst();
        }
        return Optional.empty();
    }

    public static Optional<TruststoreFormat> getTruststoreFormat(String path) {
        return getStoreFormat(path, TruststoreFormat.values());
    }

    public static Optional<KeystoreFormat> getKeystoreFormat(String path) {
        return getStoreFormat(path, KeystoreFormat.values());
    }

    private static <T extends StoreFormat> String getStoreType(String preferredType, String path, String defaultType, T[] values) {
        // Configured type has precedence
        if (preferredType != null) {
            return preferredType;
        }

        // Fallback to path
        Optional<StoreFormat> format = getStoreFormat(path, values);
        if (format.isPresent()) {
            return format.get().toString();
        }

        // Fallback to default
        return defaultType;
    }

    public static String getTruststoreType(String preferredType, String path, String defaultType) {
        return getStoreType(preferredType, path, defaultType, TruststoreFormat.values());
    }

    /**
     * 推断支持的密钥库类型。
     *
     * @param preferredType 首选格式，通常来自配置；非空时优先于路径推断
     * @param path 文件路径，可根据扩展名推断（如 {@code .pkcs12} → {@code pkcs12}）
     * @param defaultType 上述均无法推断时的兜底类型，不可为 null
     * @return 解析出的密钥库类型名
     */
    public static String getKeystoreType(String preferredType, String path, String defaultType) {
        return getStoreType(preferredType, path, defaultType, KeystoreFormat.values());
    }
}
