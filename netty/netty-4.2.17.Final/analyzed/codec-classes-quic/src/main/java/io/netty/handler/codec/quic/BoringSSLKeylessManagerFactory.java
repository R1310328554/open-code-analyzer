/*
 * Copyright 2022 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

import org.jetbrains.annotations.Nullable;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.util.Objects.requireNonNull;

/**
 * 支持通过 {@link BoringSSLAsyncPrivateKeyMethod} 实现远程/无本地私钥签名的 {@link KeyManagerFactory}。
 * <p>
 * 私钥不在本地存储，TLS 握手所需的签名与解密由外部回调完成，证书链仍由本工厂提供。
 */
public final class BoringSSLKeylessManagerFactory extends KeyManagerFactory {

    /** 异步私钥操作方法，握手时由 BoringSSL 回调触发签名或解密。 */
    final BoringSSLAsyncPrivateKeyMethod privateKeyMethod;

    private BoringSSLKeylessManagerFactory(KeyManagerFactory keyManagerFactory,
                                           BoringSSLAsyncPrivateKeyMethod privateKeyMethod) {
        super(new KeylessManagerFactorySpi(keyManagerFactory),
                keyManagerFactory.getProvider(), keyManagerFactory.getAlgorithm());
        this.privateKeyMethod = requireNonNull(privateKeyMethod, "privateKeyMethod");
    }

    /**
     * 从证书链文件创建无密钥 KeyManager 工厂。
     *
     * @param privateKeyMethod              用于密钥签名的 {@link BoringSSLAsyncPrivateKeyMethod}。
     * @param chain                         包含 {@link X509Certificate} 证书链的 {@link File}。
     * @return                              新的工厂实例。
     * @throws CertificateException         证书解析失败时抛出。
     * @throws IOException                  读取文件失败时抛出。
     * @throws KeyStoreException            KeyStore 操作失败时抛出。
     * @throws NoSuchAlgorithmException     算法不可用时抛出。
     * @throws UnrecoverableKeyException    密钥不可恢复时抛出。
     */
    public static BoringSSLKeylessManagerFactory newKeyless(BoringSSLAsyncPrivateKeyMethod privateKeyMethod, File chain)
            throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try (InputStream chainInputStream = Files.newInputStream(chain.toPath())) {
            return newKeyless(privateKeyMethod, chainInputStream);
        }
    }

    /**
     * 从证书链输入流创建无密钥 KeyManager 工厂。
     *
     * @param privateKeyMethod              用于密钥签名的 {@link BoringSSLAsyncPrivateKeyMethod}。
     * @param chain                         包含 {@link X509Certificate} 证书链的 {@link InputStream}。
     * @return                              新的工厂实例。
     * @throws CertificateException         证书解析失败时抛出。
     * @throws IOException                  读取流失败时抛出。
     * @throws KeyStoreException            KeyStore 操作失败时抛出。
     * @throws NoSuchAlgorithmException     算法不可用时抛出。
     * @throws UnrecoverableKeyException    密钥不可恢复时抛出。
     */
    public static BoringSSLKeylessManagerFactory newKeyless(BoringSSLAsyncPrivateKeyMethod privateKeyMethod,
                                                            InputStream chain)
            throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return newKeyless(privateKeyMethod, QuicSslContext.toX509Certificates0(chain));
    }

    /**
     * 从已解析的 X509 证书链创建无密钥 KeyManager 工厂。
     *
     * @param privateKeyMethod              用于密钥签名的 {@link BoringSSLAsyncPrivateKeyMethod}。
     * @param certificateChain              {@link X509Certificate} 证书链。
     * @return                              新的工厂实例。
     * @throws CertificateException         证书处理失败时抛出。
     * @throws IOException                  I/O 异常时抛出。
     * @throws KeyStoreException            KeyStore 操作失败时抛出。
     * @throws NoSuchAlgorithmException     算法不可用时抛出。
     * @throws UnrecoverableKeyException    密钥不可恢复时抛出。
     */
    public static BoringSSLKeylessManagerFactory newKeyless(BoringSSLAsyncPrivateKeyMethod privateKeyMethod,
                                                            X509Certificate... certificateChain)
            throws CertificateException, IOException,
            KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        checkNotNull(certificateChain, "certificateChain");
        KeyStore store = new KeylessKeyStore(certificateChain.clone());
        store.load(null, null);
        BoringSSLKeylessManagerFactory factory = new BoringSSLKeylessManagerFactory(
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()), privateKeyMethod);
        factory.init(store, null);
        return factory;
    }

    /** 委托给底层 KeyManagerFactory 的 SPI 实现，仅转发 init 与 getKeyManagers。 */
    private static final class KeylessManagerFactorySpi extends KeyManagerFactorySpi {

        private final KeyManagerFactory keyManagerFactory;

        KeylessManagerFactorySpi(KeyManagerFactory keyManagerFactory) {
            this.keyManagerFactory = requireNonNull(keyManagerFactory, "keyManagerFactory");
        }

        @Override
        protected void engineInit(KeyStore ks, char[] password)
                throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
            keyManagerFactory.init(ks, password);
        }

        @Override
        protected void engineInit(ManagerFactoryParameters spec) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected KeyManager[] engineGetKeyManagers() {
            return keyManagerFactory.getKeyManagers();
        }
    }

    /**
     * 只读虚拟 KeyStore：仅暴露证书链，私钥返回 {@link BoringSSLKeylessPrivateKey} 占位符。
     */
    private static final class KeylessKeyStore extends KeyStore {
        private static final String ALIAS = "key";
        private KeylessKeyStore(final X509Certificate[] certificateChain) {
            super(new KeyStoreSpi() {

                private final Date creationDate = new Date();

                @Override
                @Nullable
                public Key engineGetKey(String alias, char[] password) {
                    if (engineContainsAlias(alias)) {
                        // 返回占位私钥，实际签名由 BoringSSLAsyncPrivateKeyMethod 完成
                        return BoringSSLKeylessPrivateKey.INSTANCE;
                    }
                    return null;
                }

                @Override
                public Certificate @Nullable [] engineGetCertificateChain(String alias) {
                    return engineContainsAlias(alias)? certificateChain.clone() : null;
                }

                @Override
                @Nullable
                public Certificate engineGetCertificate(String alias) {
                    return engineContainsAlias(alias)? certificateChain[0] : null;
                }

                @Override
                @Nullable
                public Date engineGetCreationDate(String alias) {
                    return engineContainsAlias(alias)? creationDate : null;
                }

                @Override
                public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
                        throws KeyStoreException {
                    throw new KeyStoreException("Not supported");
                }

                @Override
                public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
                    throw new KeyStoreException("Not supported");
                }

                @Override
                public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
                    throw new KeyStoreException("Not supported");
                }

                @Override
                public void engineDeleteEntry(String alias) throws KeyStoreException {
                    throw new KeyStoreException("Not supported");
                }

                @Override
                public Enumeration<String> engineAliases() {
                    return Collections.enumeration(Collections.singleton(ALIAS));
                }

                @Override
                public boolean engineContainsAlias(String alias) {
                    return ALIAS.equals(alias);
                }

                @Override
                public int engineSize() {
                    return 1;
                }

                @Override
                public boolean engineIsKeyEntry(String alias) {
                    return engineContainsAlias(alias);
                }

                @Override
                public boolean engineIsCertificateEntry(String alias) {
                    return engineContainsAlias(alias);
                }

                @Override
                @Nullable
                public String engineGetCertificateAlias(Certificate cert) {
                    if (cert instanceof X509Certificate) {
                        for (X509Certificate x509Certificate : certificateChain) {
                            if (x509Certificate.equals(cert)) {
                                return ALIAS;
                            }
                        }
                    }
                    return null;
                }

                @Override
                public void engineStore(OutputStream stream, char[] password) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void engineLoad(@Nullable InputStream stream, char @Nullable [] password) {
                    if (stream != null && password != null) {
                        throw new UnsupportedOperationException();
                    }
                }
            }, null, "keyless");
        }
    }
}
