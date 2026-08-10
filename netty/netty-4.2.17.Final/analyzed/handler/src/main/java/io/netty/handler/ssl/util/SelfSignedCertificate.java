/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.ssl.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * 为测试生成临时自签名 X.509 证书与私钥。
 * <p>
 * <strong>注意：</strong>
 * 切勿在生产环境使用本类生成的证书与私钥；仅供测试，安全性极低，
 * 内部甚至使用非安全伪随机数以加速生成。
 * </p><p>
 * 证书（PEM）与 EC/RSA 私钥写入系统临时目录（{@link java.io.File#createTempFile(String, String)}），
 * JVM 退出时通过 {@link java.io.File#deleteOnExit()} 删除。
 * </p><p>
 * 生成顺序：优先 {@code netty-pkitesting} 的 CertificateBuilder，其次 Bouncy Castle、keytool、
 * OpenJDK {@code sun.security.x509}。
 * </p>
 * @deprecated Use the {@code CertificateBuilder} from {@code netty-pkitesting} instead.
 */
@Deprecated
public final class SelfSignedCertificate {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SelfSignedCertificate.class);

    /** 默认生效时间：当前时间减 1 年（应对时钟回拨）。 */
    private static final Date DEFAULT_NOT_BEFORE = new Date(SystemPropertyUtil.getLong(
            "io.netty.selfSignedCertificate.defaultNotBefore", System.currentTimeMillis() - 86400000L * 365));
    /** X.509 规范允许的最大失效时间：9999-12-31 23:59:59。 */
    private static final Date DEFAULT_NOT_AFTER = new Date(SystemPropertyUtil.getLong(
            "io.netty.selfSignedCertificate.defaultNotAfter", 253402300799000L));

    /**
     * FIPS 140-2 要求 RSA 密钥至少 2048 位；可通过系统属性覆盖默认值。
     */
    private static final int DEFAULT_KEY_LENGTH_BITS =
            SystemPropertyUtil.getInt("io.netty.handler.ssl.util.selfSignedKeyStrength", 2048);

    /** PEM 格式证书临时文件。 */
    private final File certificate;
    /** PEM 格式私钥临时文件。 */
    private final File privateKey;
    /** 内存中的 X.509 证书对象。 */
    private final X509Certificate cert;
    /** 内存中的私钥对象。 */
    private final PrivateKey key;

    /**
     * 创建新实例（默认 RSA、localhost、默认有效期）。
     * <p> Algorithm: RSA </p>
     */
    public SelfSignedCertificate() throws CertificateException {
        this(new Builder());
    }

    /**
     * Creates a new instance.
     * <p> Algorithm: RSA </p>
     *
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     */
    public SelfSignedCertificate(Date notBefore, Date notAfter)
            throws CertificateException {
        this(new Builder().notBefore(notBefore).notAfter(notAfter));
    }

    /**
     * Creates a new instance.
     *
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     * @param algorithm Key pair algorithm
     * @param bits      the number of bits of the generated private key
     */
    public SelfSignedCertificate(Date notBefore, Date notAfter, String algorithm, int bits)
            throws CertificateException {
        this(new Builder().notBefore(notBefore).notAfter(notAfter).algorithm(algorithm).bits(bits));
    }

    /**
     * Creates a new instance.
     * <p> Algorithm: RSA </p>
     *
     * @param fqdn a fully qualified domain name
     */
    public SelfSignedCertificate(String fqdn) throws CertificateException {
        this(new Builder().fqdn(fqdn));
    }

    /**
     * Creates a new instance.
     *
     * @param fqdn      a fully qualified domain name
     * @param algorithm Key pair algorithm
     * @param bits      the number of bits of the generated private key
     */
    public SelfSignedCertificate(String fqdn, String algorithm, int bits) throws CertificateException {
        this(new Builder().fqdn(fqdn).algorithm(algorithm).bits(bits));
    }

    /**
     * Creates a new instance.
     * <p> Algorithm: RSA </p>
     *
     * @param fqdn      a fully qualified domain name
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     */
    public SelfSignedCertificate(String fqdn, Date notBefore, Date notAfter) throws CertificateException {
        this(new Builder().fqdn(fqdn).notBefore(notBefore).notAfter(notAfter));
    }

    /**
     * Creates a new instance.
     *
     * @param fqdn      a fully qualified domain name
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     * @param algorithm Key pair algorithm
     * @param bits      the number of bits of the generated private key
     */
    public SelfSignedCertificate(String fqdn, Date notBefore, Date notAfter, String algorithm, int bits)
            throws CertificateException {
        this(new Builder().fqdn(fqdn).notBefore(notBefore).notAfter(notAfter).algorithm(algorithm).bits(bits));
    }

    /**
     * Creates a new instance.
     * <p> Algorithm: RSA </p>
     *
     * @param fqdn      a fully qualified domain name
     * @param random    the {@link SecureRandom} to use
     * @param bits      the number of bits of the generated private key
     */
    public SelfSignedCertificate(String fqdn, SecureRandom random, int bits)
            throws CertificateException {
        this(new Builder().fqdn(fqdn).random(random).bits(bits));
    }

    /**
     * Creates a new instance.
     *
     * @param fqdn      a fully qualified domain name
     * @param random    the {@link SecureRandom} to use
     * @param algorithm Key pair algorithm
     * @param bits      the number of bits of the generated private key
     */
    public SelfSignedCertificate(String fqdn, SecureRandom random, String algorithm, int bits)
            throws CertificateException {
        this(new Builder().fqdn(fqdn).random(random).algorithm(algorithm).bits(bits));
    }

    /**
     * Creates a new instance.
     * <p> Algorithm: RSA </p>
     *
     * @param fqdn      a fully qualified domain name
     * @param random    the {@link SecureRandom} to use
     * @param bits      the number of bits of the generated private key
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     */
    public SelfSignedCertificate(String fqdn, SecureRandom random, int bits, Date notBefore, Date notAfter)
            throws CertificateException {
        this(new Builder().fqdn(fqdn).notBefore(notBefore).notAfter(notAfter).random(random).bits(bits));
    }

    /**
     * Creates a new instance.
     *
     * @param fqdn      a fully qualified domain name
     * @param random    the {@link SecureRandom} to use
     * @param bits      the number of bits of the generated private key
     * @param notBefore Certificate is not valid before this time
     * @param notAfter  Certificate is not valid after this time
     * @param algorithm Key pair algorithm
     */
    public SelfSignedCertificate(String fqdn, SecureRandom random, int bits, Date notBefore, Date notAfter,
                                 String algorithm) throws CertificateException {
        this(new Builder().fqdn(fqdn).random(random).algorithm(algorithm).bits(bits)
                .notBefore(notBefore).notAfter(notAfter));
    }

    private SelfSignedCertificate(Builder builder) throws CertificateException {
        if (!builder.generateCertificateBuilder()) {
            if (!builder.generateBc()) {
                if (!builder.generateKeytool()) {
                    if (!builder.generateSunMiscSecurity()) {
                        // last exception is always from generateSunMiscSecurity, so we can cast here
                        throw (CertificateException) builder.failure;
                    }
                }
            }
        }

        certificate = new File(builder.paths[0]);
        privateKey = new File(builder.paths[1]);
        key = builder.privateKey;
        try (FileInputStream certificateInput = new FileInputStream(certificate)) {
            cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(certificateInput);
        } catch (Exception e) {
            throw new CertificateEncodingException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 返回 PEM 格式的 X.509 证书文件。
     */
    public File certificate() {
        return certificate;
    }

    /**
     * 返回 PEM 格式的 EC/RSA 私钥文件。
     */
    public File privateKey() {
        return privateKey;
    }

    /**
     * 返回内存中的 X.509 证书对象。
     */
    public X509Certificate cert() {
        return cert;
    }

    /**
     * 返回内存中的 EC/RSA 私钥。
     */
    public PrivateKey key() {
        return key;
    }

    /**
     * 删除生成的证书与私钥临时文件。
     */
    public void delete() {
        safeDelete(certificate);
        safeDelete(privateKey);
    }

    static String[] newSelfSignedCertificate(
            String fqdn, PrivateKey key, X509Certificate cert) throws IOException, CertificateEncodingException {
        // 将私钥编码为 PEM 文件
        ByteBuf wrappedBuf = Unpooled.wrappedBuffer(key.getEncoded());
        ByteBuf encodedBuf;
        final String keyText;
        try {
            encodedBuf = Base64.encode(wrappedBuf, true);
            try {
                keyText = "-----BEGIN PRIVATE KEY-----\n" +
                        encodedBuf.toString(CharsetUtil.US_ASCII) +
                        "\n-----END PRIVATE KEY-----\n";
            } finally {
                encodedBuf.release();
            }
        } finally {
            wrappedBuf.release();
        }

        // 文件名中将非法字符替换为 'x'
        fqdn = fqdn.replaceAll("[^\\w.-]", "x");

        File keyFile = PlatformDependent.createTempFile("keyutil_" + fqdn + '_', ".key", null);
        keyFile.deleteOnExit();

        OutputStream keyOut = new FileOutputStream(keyFile);
        try {
            keyOut.write(keyText.getBytes(CharsetUtil.US_ASCII));
            keyOut.close();
            keyOut = null;
        } finally {
            if (keyOut != null) {
                safeClose(keyFile, keyOut);
                safeDelete(keyFile);
            }
        }

        wrappedBuf = Unpooled.wrappedBuffer(cert.getEncoded());
        final String certText;
        try {
            encodedBuf = Base64.encode(wrappedBuf, true);
            try {
                // 将证书编码为 CRT 文件
                certText = "-----BEGIN CERTIFICATE-----\n" +
                        encodedBuf.toString(CharsetUtil.US_ASCII) +
                        "\n-----END CERTIFICATE-----\n";
            } finally {
                encodedBuf.release();
            }
        } finally {
            wrappedBuf.release();
        }

        File certFile = PlatformDependent.createTempFile("keyutil_" + fqdn + '_', ".crt", null);
        certFile.deleteOnExit();

        OutputStream certOut = new FileOutputStream(certFile);
        try {
            certOut.write(certText.getBytes(CharsetUtil.US_ASCII));
            certOut.close();
            certOut = null;
        } finally {
            if (certOut != null) {
                safeClose(certFile, certOut);
                safeDelete(certFile);
                safeDelete(keyFile);
            }
        }

        return new String[] { certFile.getPath(), keyFile.getPath() };
    }

    private static void safeDelete(File certFile) {
        if (!certFile.delete()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to delete a file: " + certFile);
            }
        }
    }

    private static void safeClose(File keyFile, OutputStream keyOut) {
        try {
            keyOut.close();
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close a file: " + keyFile, e);
            }
        }
    }

    private static boolean isBouncyCastleAvailable() {
        try {
            // this class is in bcpkix, both fips and non-fips
            Class.forName("org.bouncycastle.cert.X509v3CertificateBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static final class Builder {
        // 用户可配置字段
        String fqdn = "localhost";
        SecureRandom random;
        int bits = DEFAULT_KEY_LENGTH_BITS;
        Date notBefore = DEFAULT_NOT_BEFORE;
        Date notAfter = DEFAULT_NOT_AFTER;
        String algorithm = "RSA";

        // 按需填充的生成结果
        Throwable failure;
        KeyPair keypair;
        PrivateKey privateKey;
        String[] paths;

        private Builder() {
        }

        /**
         * 设置证书 CN（完全限定域名）。
         *
         * @param fqdn The FQDN
         * @return This builder
         */
        public Builder fqdn(String fqdn) {
            this.fqdn = ObjectUtil.checkNotNullWithIAE(fqdn, "fqdn");
            return this;
        }

        /**
         * 设置密钥生成用 RNG（keytool 生成器不支持此选项）。
         *
         * @param random The CSPRNG
         * @return This builder
         */
        public Builder random(SecureRandom random) {
            this.random = random;
            return this;
        }

        /**
         * 设置密钥长度（位）。
         *
         * @param bits The key size
         * @return This builder
         */
        public Builder bits(int bits) {
            this.bits = bits;
            return this;
        }

        /**
         * 设置证书有效期起始时间。
         *
         * @param notBefore The start date
         * @return This builder
         */
        public Builder notBefore(Date notBefore) {
            this.notBefore = ObjectUtil.checkNotNullWithIAE(notBefore, "notBefore");
            return this;
        }

        /**
         * 设置证书有效期结束时间。
         *
         * @param notAfter The start date
         * @return This builder
         */
        public Builder notAfter(Date notAfter) {
            this.notAfter = ObjectUtil.checkNotNullWithIAE(notAfter, "notAfter");
            return this;
        }

        /**
         * 设置密钥算法（仅支持 RSA 与 EC）。
         *
         * @param algorithm The key algorithm
         * @return This builder
         */
        public Builder algorithm(String algorithm) {
            if ("EC".equalsIgnoreCase(algorithm)) {
                this.algorithm = "EC";
            } else if ("RSA".equalsIgnoreCase(algorithm)) {
                this.algorithm = "RSA";
            } else {
                throw new IllegalArgumentException("Algorithm not valid: " + algorithm);
            }
            return this;
        }

        private SecureRandom randomOrDefault() {
            // 测试场景跳过熵收集，使用非安全随机源加速
            // We just want to generate it without any delay because it's for testing purposes only.
            return random == null ? ThreadLocalInsecureRandom.current() : random;
        }

        private void generateKeyPairLocally() {
            if (keypair != null) {
                return;
            }

            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
                keyGen.initialize(bits, randomOrDefault());
                keypair = keyGen.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                // Should not reach here because every Java implementation must have RSA and EC key pair generator.
                throw new IllegalStateException(e);
            }
            privateKey = keypair.getPrivate();
        }

        private void addFailure(Throwable t) {
            if (failure != null) {
                t.addSuppressed(failure);
            }
            failure = t;
        }

        boolean generateBc() {
            if (!isBouncyCastleAvailable()) {
                // no need to even try. We can avoid generating the key pair with this check.
                logger.debug("Failed to generate a self-signed X.509 certificate because " +
                        "BouncyCastle PKIX is not available in classpath");
                return false;
            }
            generateKeyPairLocally();
            try {
                // Try Bouncy Castle first as otherwise we will see an IllegalAccessError on more recent JDKs.
                paths = BouncyCastleSelfSignedCertGenerator.generate(
                        fqdn, keypair, randomOrDefault(), notBefore, notAfter, algorithm);
                return true;
            } catch (Throwable t) {
                logger.debug("Failed to generate a self-signed X.509 certificate using Bouncy Castle:", t);
                addFailure(t);
                return false;
            }
        }

        boolean generateKeytool() {
            if (!KeytoolSelfSignedCertGenerator.isAvailable()) {
                logger.debug("Not attempting to generate certificate with keytool because keytool is missing");
                return false;
            }
            if (random != null) {
                logger.debug("Not attempting to generate certificate with keytool because of explicitly set " +
                        "SecureRandom");
                return false;
            }
            try {
                KeytoolSelfSignedCertGenerator.generate(this);
                return true;
            } catch (Throwable t) {
                logger.debug("Failed to generate a self-signed X.509 certificate using keytool:", t);
                addFailure(t);
                return false;
            }
        }

        boolean generateCertificateBuilder() {
            if (!CertificateBuilderCertGenerator.isAvailable()) {
                logger.debug("Not attempting to generate a certificate with CertificateBuilder " +
                        "because it's not available on the classpath");
                return false;
            }
            try {
                CertificateBuilderCertGenerator.generate(this);
                return true;
            } catch (CertificateException ce) {
                logger.debug(ce);
                addFailure(ce);
            } catch (Exception e) {
                String msg = "Failed to generate a self-signed X.509 certificate using CertificateBuilder:";
                logger.debug(msg, e);
                addFailure(new CertificateException(msg, e));
            }
            return false;
        }

        boolean generateSunMiscSecurity() {
            generateKeyPairLocally();
            try {
                // Try the OpenJDK's proprietary implementation.
                paths = OpenJdkSelfSignedCertGenerator.generate(
                        fqdn, keypair, randomOrDefault(), notBefore, notAfter, algorithm);
                return true;
            } catch (Throwable t2) {
                logger.debug("Failed to generate a self-signed X.509 certificate using sun.security.x509:", t2);
                final CertificateException certificateException = new CertificateException(
                        "No provider succeeded to generate a self-signed certificate. " +
                                "See debug log for the root cause.", t2);
                addFailure(certificateException);
                return false;
            }
        }

        /**
         * 构建证书；调用后不可再使用本 Builder。
         *
         * @return The certificate
         * @throws CertificateException If generation fails
         */
        public SelfSignedCertificate build() throws CertificateException {
            return new SelfSignedCertificate(this);
        }
    }
}
