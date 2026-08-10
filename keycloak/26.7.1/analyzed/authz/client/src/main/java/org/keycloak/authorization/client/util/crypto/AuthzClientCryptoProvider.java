/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.authorization.client.util.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.SSLSocketFactory;

import org.keycloak.common.crypto.CertificateUtilsProvider;
import org.keycloak.common.crypto.CryptoProvider;
import org.keycloak.common.crypto.ECDSACryptoProvider;
import org.keycloak.common.crypto.PemUtilsProvider;
import org.keycloak.common.crypto.UserIdentityExtractorProvider;
import org.keycloak.common.util.KeystoreUtil;

/**
 * <p>授权客户端（authz-client）使用的精简 {@link CryptoProvider} 实现。</p>
 *
 * <p>主要提供 ECDSA 签名在 concatenated R||S 与 ASN.1 DER 格式间的互转，
 * 其余算法能力均抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author rmartinc
 */
public class AuthzClientCryptoProvider implements CryptoProvider {

    @Override
    /** 返回 JRE 默认 KeyStore 的 Provider（非 BouncyCastle）。 */
    public Provider getBouncyCastleProvider() {
        try {
            return KeyStore.getInstance(KeyStore.getDefaultType()).getProvider();
        } catch (KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    /** SPI 排序值，数值越大优先级越低。 */
    public int order() {
        return 100;
    }

    @Override
    /** 通用算法提供方查找（authz-client 未实现）。 */
    public <T> T getAlgorithmProvider(Class<T> clazz, String algorithm) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** 证书工具（authz-client 未实现）。 */
    public CertificateUtilsProvider getCertificateUtils() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** PEM 编解码工具（authz-client 未实现）。 */
    public PemUtilsProvider getPemUtils() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** OCSP 证明方（authz-client 未实现）。 */
    public <T> T getOCSPProver(Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** 用户身份提取器（authz-client 未实现）。 */
    public UserIdentityExtractorProvider getIdentityExtractorProvider() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    /** 返回仅实现 ECDSA 签名格式转换的 {@link ECDSACryptoProvider}。 */
    public ECDSACryptoProvider getEcdsaCryptoProvider() {
        return new ECDSACryptoProvider() {
            @Override
            /** 将 concatenated R||S 签名转为 ASN.1 DER SEQUENCE。 */
            public byte[] concatenatedRSToASN1DER(byte[] signature, int signLength) throws IOException {
                int len = signLength / 2;
                int arraySize = len + 1;

                byte[] r = new byte[arraySize];
                byte[] s = new byte[arraySize];
                System.arraycopy(signature, 0, r, 1, len);
                System.arraycopy(signature, len, s, 1, len);
                BigInteger rBigInteger = new BigInteger(r);
                BigInteger sBigInteger = new BigInteger(s);

                ASN1Encoder.create().write(rBigInteger);
                ASN1Encoder.create().write(sBigInteger);

                return ASN1Encoder.create()
                        .writeDerSeq(
                                ASN1Encoder.create().write(rBigInteger),
                                ASN1Encoder.create().write(sBigInteger))
                        .toByteArray();
            }

            @Override
            /** 将 ASN.1 DER 签名解码为 concatenated R||S 格式。 */
            public byte[] asn1derToConcatenatedRS(byte[] derEncodedSignatureValue, int signLength) throws IOException {
                int len = signLength / 2;

                List<byte[]> seq = ASN1Decoder.create(derEncodedSignatureValue).readSequence();
                if (seq.size() != 2) {
                    throw new IOException("Invalid sequence with size different to 2");
                }

                BigInteger rBigInteger = ASN1Decoder.create(seq.get(0)).readInteger();
                BigInteger sBigInteger = ASN1Decoder.create(seq.get(1)).readInteger();

                byte[] r = integerToBytes(rBigInteger, len);
                byte[] s = integerToBytes(sBigInteger, len);

                byte[] concatenatedSignatureValue = new byte[signLength];
                System.arraycopy(r, 0, concatenatedSignatureValue, 0, len);
                System.arraycopy(s, 0, concatenatedSignatureValue, len, len);

                return concatenatedSignatureValue;
            }

            @Override
            public ECPublicKey getPublicFromPrivate(ECPrivateKey ecPrivateKey) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            /** 将 BigInteger 截断或零填充至指定长度字节。 */
            private byte[] integerToBytes(BigInteger s, int qLength) {
                byte[] bytes = s.toByteArray();
                if (qLength < bytes.length) {
                    byte[] tmp = new byte[qLength];
                    System.arraycopy(bytes, bytes.length - tmp.length, tmp, 0, tmp.length);
                    return tmp;
                } else if (qLength > bytes.length) {
                    byte[] tmp = new byte[qLength];
                    System.arraycopy(bytes, 0, tmp, tmp.length - bytes.length, bytes.length);
                    return tmp;
                }
                return bytes;
            }
        };
    }

    @Override
    public ECParameterSpec createECParams(String curveName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyPairGenerator getKeyPairGen(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cipher getAesCbcCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cipher getAesGcmCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SecretKeyFactory getSecretKeyFact(String keyAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyStore getKeyStore(KeystoreUtil.KeystoreFormat format) throws KeyStoreException, NoSuchProviderException {
        return KeyStore.getInstance(format.name());
    }

    @Override
    public CertificateFactory getX509CertFactory() throws CertificateException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CertStore getCertStore(CollectionCertStoreParameters collectionCertStoreParameters) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CertPathBuilder getCertPathBuilder() throws NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Signature getSignature(String sigAlgName) throws NoSuchAlgorithmException, NoSuchProviderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLSocketFactory wrapFactoryForTruststore(SSLSocketFactory delegate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
