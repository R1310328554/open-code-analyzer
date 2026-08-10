package org.keycloak.services.x509;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * NGINX 受信代理 X.509 客户端证书查找实现。
 *
 * <p>从 NGINX 在 TLS 双向认证后转发的 HTTP 头中提取终端用户证书，并依赖 NGINX 的
 * {@code ssl-client-verify: SUCCESS} 头确认证书已由 NGINX 验证通过，无需 Keycloak 信任库重建链。</p>
 *
 * <p>NGINX 配置须包含：</p>
 * <code>
 * server {
 *    ...
 *    ssl_client_certificate                  path-to-trusted-ca.crt;
 *    ssl_verify_client                       on|optional;
 *    ssl_verify_depth                        2;
 *    ...
 *    location / {
 *    ...
 *      proxy_set_header ssl-client-cert        $ssl_client_escaped_cert;
 *    ...
 *  }
 * </code>
 *
 * <p>注意：{@code $ssl_client_cert} 已弃用，请仅使用 {@code $ssl_client_escaped_cert}。</p>
 *
 * @author <a href="mailto:youssef.elhouti@tailosoft.com">Youssef El Houti</a>
 * @version $Revision: 1 $
 * @since 01/09/2022
 */

public class NginxProxyTrustedClientCertificateLookup extends AbstractClientCertificateFromHttpHeadersLookup {

    private static final Logger log = Logger.getLogger(NginxProxyTrustedClientCertificateLookup.class);

    /** 证书 PEM 是否经 URL 编码。 */
    private final boolean certIsUrlEncoded;

    /**
     * 构造受信 NGINX 证书查找器。
     *
     * @param sslCientCertHttpHeader 客户端证书 HTTP 头名
     * @param sslCertChainHttpHeaderPrefix 链头前缀
     * @param certificateChainLength 链最大深度
     * @param certIsUrlEncoded 证书是否 URL 编码
     */
    public NginxProxyTrustedClientCertificateLookup(String sslCientCertHttpHeader,
                                                String sslCertChainHttpHeaderPrefix,
                                                int certificateChainLength,
                                                boolean certIsUrlEncoded) {
        super(sslCientCertHttpHeader, sslCertChainHttpHeaderPrefix, certificateChainLength);

        this.certIsUrlEncoded = certIsUrlEncoded;
    }

    /**
     * 从头中提取证书，并校验 NGINX {@code ssl-client-verify} 是否为 {@code SUCCESS}。
     */
    @Override
    protected X509Certificate getCertificateFromHttpHeader(HttpRequest request, String httpHeader) throws GeneralSecurityException {
        X509Certificate certificate = super.getCertificateFromHttpHeader(request, httpHeader);
        if (certificate == null) {
            return null;
        }
        String validCertificateResult = getHeaderValue(request, "ssl-client-verify");
        if ("SUCCESS".equals(validCertificateResult)) {
            return certificate;
        } else {
            log.warn("nginx could not verify the certificate: ssl-client-verify: " + validCertificateResult);
            return null;
        }
    }

    /** {@inheritDoc} 解码 PEM，必要时先 URL 解码。 */
    @Override
    protected X509Certificate decodeCertificateFromPem(String pem) throws PemException {

        if (pem == null) {
            log.warn("End user TLS Certificate is NULL! ");
            return null;
        }
        if (certIsUrlEncoded) {
            pem = java.net.URLDecoder.decode(pem, StandardCharsets.UTF_8);
        }


        return PemUtils.decodeCertificate(pem);
    }

}
