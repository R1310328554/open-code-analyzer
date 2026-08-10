package org.keycloak.services.x509;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.keycloak.common.util.DerUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * RFC 9440 合规的客户端证书查找实现。
 *
 * <p>从符合 RFC 9440 的反向代理转发的 HTTP 头中提取客户端证书及证书链，
 * 供部署在 TLS 终结代理后的 Keycloak 使用。</p>
 *
 * @author <a href="mailto:seiferma.dev+kc@gmail.com">Stephan Seifermann</a>
 * @version $Revision: 1 $
 * @since 12/30/2024
 */
public class Rfc9440ClientCertificateLookup implements X509ClientCertificateLookup {

    /** RFC 违规通用异常基类。 */
    public static class RfcViolationException extends Exception {
        /** @param rfc RFC 编号 @param section 章节 @param details 详情 @param cause 根因 */
        public RfcViolationException(String rfc, String section, String details, Throwable cause) {
            super("Violation of RFC " + rfc + " (see section " + section + "): " + details, cause);
        }
    }

    /** RFC 9440 格式违规异常。 */
    public static class Rfc9440ViolationException extends RfcViolationException {
        public Rfc9440ViolationException(String section, String details) {
            this(section, details, null);
        }
        public Rfc9440ViolationException(String section, String details, Throwable cause) {
            super("9440", section, details, cause);
        }
    }

    /** RFC 8941 Structured Fields 格式违规异常。 */
    public static class Rfc8941ViolationException extends RfcViolationException {
        public Rfc8941ViolationException(String section, String details) {
            this(section, details, null);
        }
        public Rfc8941ViolationException(String section, String details, Throwable cause) {
            super("8941", section, details, cause);
        }
    }

    private static final Logger log = Logger.getLogger(Rfc9440ClientCertificateLookup.class);
    /** 客户端叶子证书 HTTP 头名称。 */
    protected final String sslClientCertHttpHeader;
    /** 证书链 HTTP 头名称。 */
    protected final String sslCertChainHttpHeader;
    /** 最多加载的链证书数量。 */
    protected final int certificateChainLength;

    /**
     * 构造 RFC 9440 证书查找器。
     *
     * @param sslClientCertHttpHeader 客户端证书头名（不可为空）
     * @param sslCertChainHttpHeader 证书链头名（不可为空）
     * @param certificateChainLength 链长度上限
     */
    public Rfc9440ClientCertificateLookup(String sslClientCertHttpHeader,
                                          String sslCertChainHttpHeader,
                                          int certificateChainLength) {
        this.sslClientCertHttpHeader = Optional.ofNullable(sslClientCertHttpHeader)
                .filter(s -> !s.isBlank())
                .orElseThrow(() ->  new IllegalArgumentException("sslClientCertHttpHeader"));

        this.sslCertChainHttpHeader = Optional.ofNullable(sslCertChainHttpHeader)
                .filter(s -> !s.isBlank())
                .orElseThrow(() ->  new IllegalArgumentException("sslCertChainHttpHeader"));

        this.certificateChainLength = certificateChainLength;
    }

    /** {@inheritDoc} 从 RFC 9440 合规头中组装完整证书链。 */
    @Override
    public X509Certificate[] getCertificateChain(HttpRequest httpRequest) throws GeneralSecurityException {
        if (!httpRequest.isProxyTrusted()) {
            log.warnf("HTTP header \"%s\" is not trusted", sslClientCertHttpHeader);
            return null;
        }
        try {
            List<X509Certificate> chain = new ArrayList<>();
            X509Certificate clientCertificate = getClientCertificateFromHeader(httpRequest);
            if (clientCertificate != null) {
                chain.add(clientCertificate);
                chain.addAll(getClientCertificateChainFromHeader(httpRequest));
            }
            return chain.toArray(new X509Certificate[0]);
        } catch (RfcViolationException e) {
            throw new GeneralSecurityException(e);
        }
    }

    /** {@inheritDoc} 无状态实现，无需释放资源。 */
    @Override
    public void close() {
        // intentionally left blank
    }

    /**
     * 从 {@link #sslClientCertHttpHeader} 头提取客户端叶子证书。
     *
     * @param httpRequest 含相关 HTTP 头的请求
     * @return 解析出的证书；未呈现时返回 {@code null}
     * @throws RfcViolationException 头缺失或格式不符合 RFC
     */
    protected X509Certificate getClientCertificateFromHeader(HttpRequest httpRequest) throws RfcViolationException {
        List<String> headerValues = httpRequest.getHttpHeaders().getRequestHeader(sslClientCertHttpHeader);
        if (headerValues.isEmpty()) {
            return null;
        }
        if (headerValues.size() > 1) {
            throw new Rfc9440ViolationException("2.2", "client cert header must occur at most once");
        }

        return parseCertificateFromHttpByteSequence(headerValues.get(0));
    }

    /**
     * 从 {@link #sslCertChainHttpHeader} 头提取证书链（不含叶子证书）。
     *
     * @param httpRequest 含相关 HTTP 头的请求
     * @return 按头中出现顺序排列的证书列表，最多 {@link #certificateChainLength} 张
     * @throws RfcViolationException 头值格式不符合 RFC
     * @throws GeneralSecurityException 证书无法解析
     */
    protected List<X509Certificate> getClientCertificateChainFromHeader(HttpRequest httpRequest) throws RfcViolationException, GeneralSecurityException {
        List<String> chainHeaderValues = httpRequest.getHttpHeaders().getRequestHeader(sslCertChainHttpHeader);
        if (chainHeaderValues == null || chainHeaderValues.isEmpty()) {
            // RFC 9440 第 2.3 节：链头为可选
            return Collections.emptyList();
        }

        // 按 RFC 8941 第 3.1 节，头值可被拆分
        List<String> encodedCerts = new ArrayList<>();
        for (String chainHeaderValue : chainHeaderValues) {
            // RFC 8941 第 3.1 节：列表项以逗号加可选空白分隔
            String[] listEntries = chainHeaderValue.split(",\\s*");
            encodedCerts.addAll(Arrays.asList(listEntries));
        }

        // 链长度可能超过配置上限，截断至限制
        if (encodedCerts.size() > certificateChainLength) {
            log.debugf("The amount of certificates in the chain header %d is bigger than the configured limit of %d. Truncating.", encodedCerts.size(), certificateChainLength);
            encodedCerts = encodedCerts.subList(0, certificateChainLength);
        }

        // 列表项为 RFC 9440 第 2.1 节定义的字节序列编码
        List<X509Certificate> parsedCertificates = new ArrayList<>();
        for (String encodedCert : encodedCerts) {
            parsedCertificates.add(parseCertificateFromHttpByteSequence(encodedCert));
        }
        return parsedCertificates;
    }

    /**
     * 按 RFC 9440 第 2.1 节从 HTTP 字节序列解析 X509 证书。
     *
     * @param byteSequence RFC 9440 编码的证书字节序列（形如 {@code :base64:}）
     * @return 解析出的 X509 证书
     * @throws RfcViolationException 输入不符合 RFC 格式
     */
    protected static X509Certificate parseCertificateFromHttpByteSequence(String byteSequence) throws RfcViolationException {
        if (byteSequence.length() < 2 || !byteSequence.startsWith(":") || !byteSequence.endsWith(":")) {
            throw new Rfc8941ViolationException("3.3.5", "value is not encoded as byte sequence");
        }
        String base64EncodedByteSequence = byteSequence.substring(1, byteSequence.length() - 1);

        byte[] certificateBytes;
        try {
            certificateBytes = Base64.getMimeDecoder().decode(base64EncodedByteSequence);
        } catch (IllegalArgumentException e) {
            throw new Rfc9440ViolationException("2.1", "value does not contain base64 encoded content", e);
        }

        X509Certificate certificate;
        try (InputStream is = new ByteArrayInputStream(certificateBytes)) {
            certificate = DerUtils.decodeCertificate(is);
        } catch (Exception e) {
            throw new Rfc9440ViolationException("2.1", "value does not contain DER encoded certificate", e);
        }

        if (certificate == null) {
            throw new Rfc9440ViolationException("2.1", "value does not contain DER encoded certificate");
        }

        log.debugf("Parsed certificate : Subject DN=[%s]  SerialNumber=[%s]", certificate.getSubjectX500Principal(), certificate.getSerialNumber());
        return certificate;
    }

}
