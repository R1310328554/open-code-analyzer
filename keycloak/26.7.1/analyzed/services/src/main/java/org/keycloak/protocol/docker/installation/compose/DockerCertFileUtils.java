package org.keycloak.protocol.docker.installation.compose;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;

import org.keycloak.common.util.PemUtils;

/**
 * Docker Compose 安装包证书/密钥 PEM 格式化工具。
 * <p>将 DER 编码证书或 PKCS#8 私钥转为带 64 字符换行的 PEM 文本。</p>
 */
public final class DockerCertFileUtils {
    public static final String BEGIN_CERT = PemUtils.BEGIN_CERT;
    public static final String END_CERT = PemUtils.END_CERT;
    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";
    public static final String LINE_SEPERATOR = System.getProperty("line.separator");

    private DockerCertFileUtils() {
    }

    /** 将 X.509 证书格式化为 PEM 字符串。 */
    public static String formatCrtFileContents(final Certificate certificate) throws CertificateEncodingException {
        return encodeAndPrettify(BEGIN_CERT, certificate.getEncoded(), END_CERT);
    }

    /** 将 PKCS#8 私钥格式化为 PEM 字符串。 */
    public static String formatPrivateKeyContents(final PrivateKey privateKey) {
        return encodeAndPrettify(BEGIN_PRIVATE_KEY, privateKey.getEncoded(), END_PRIVATE_KEY);
    }

    /** 将公钥 DER 编码包装为 PEM 证书块格式。 */
    public static String formatPublicKeyContents(final PublicKey publicKey) {
        return encodeAndPrettify(BEGIN_CERT, publicKey.getEncoded(), END_CERT);
    }

    /** Base64 MIME 编码（64 字符换行）并添加 PEM 头尾。 */
    private static String encodeAndPrettify(final String header, final byte[] rawCrtText, final String footer) {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPERATOR.getBytes());
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        final String prettified_cert = header + LINE_SEPERATOR + encodedCertText + LINE_SEPERATOR + footer;
        return prettified_cert;
    }
}
