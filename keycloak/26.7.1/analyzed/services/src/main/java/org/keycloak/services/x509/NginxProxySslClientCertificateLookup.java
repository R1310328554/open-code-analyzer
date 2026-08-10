package org.keycloak.services.x509;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

/**
 * NGINX 反向代理 SSL 客户端证书查找实现。
 *
 * <p>从 NGINX 在 TLS 双向认证后通过 HTTP 头转发的终端用户 X.509 证书中提取客户端证书。
 * NGINX 无法直接传递完整 CA 链，本实现借助 Keycloak 信任库重建证书链。</p>
 *
 * <p>NGINX 配置须包含：</p>
 * <code>
 * server {
 *    ...
 *    ssl_client_certificate                  path-to-my-trustyed-cas-for-client-auth.pem;
 *    ssl_verify_client                       on|optional_no_ca;
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
 * @author <a href="mailto:arnault.michel@toad-consulting.com">Arnault MICHEL</a>
 * @version $Revision: 1 $
 * @since 10/09/2018
 */

public class NginxProxySslClientCertificateLookup extends AbstractClientCertificateFromHttpHeadersLookup {

    private static final Logger log = Logger.getLogger(NginxProxySslClientCertificateLookup.class);

    /** Keycloak 信任库是否已成功加载。 */
    private final boolean isTruststoreLoaded;
    /** 证书 PEM 是否经 URL 编码（对应 {@code ssl_client_escaped_cert}）。 */
    private final boolean certIsUrlEncoded;
    /** 信任库中的根 CA 证书集合。 */
    private final Set<X509Certificate> trustedRootCerts;
    /** 信任库中的中间 CA 证书集合。 */
    private final Set<X509Certificate> intermediateCerts;


    /**
     * 构造 NGINX 证书查找器。
     *
     * @param sslClientCertHttpHeader 客户端证书 HTTP 头名
     * @param sslCertChainHttpHeaderPrefix 链头前缀（NGINX 通常不使用）
     * @param certificateChainLength 链最大深度
     * @param intermediateCerts 中间 CA 证书集合
     * @param trustedRootCerts 根 CA 证书集合
     * @param isTruststoreLoaded 信任库是否已加载
     * @param certIsUrlEncoded 证书是否 URL 编码
     */
    public NginxProxySslClientCertificateLookup(String sslClientCertHttpHeader,
                                                String sslCertChainHttpHeaderPrefix,
                                                int certificateChainLength,
                                                Set<X509Certificate> intermediateCerts,
                                                Set<X509Certificate> trustedRootCerts,
                                                boolean isTruststoreLoaded,
                                                boolean certIsUrlEncoded
                                                ) {
        super(sslClientCertHttpHeader, sslCertChainHttpHeaderPrefix, certificateChainLength);

      Objects.requireNonNull(intermediateCerts,"requireNonNull intermediateCerts");
      Objects.requireNonNull(trustedRootCerts,"requireNonNull trustedRootCerts");
      this.intermediateCerts = intermediateCerts;
      this.trustedRootCerts = trustedRootCerts;
      this.isTruststoreLoaded = isTruststoreLoaded;
      this.certIsUrlEncoded = certIsUrlEncoded;

        if (!this.isTruststoreLoaded) {
            log.warn("Keycloak Truststore is null or empty, but it's required for NGINX x509cert-lookup provider");
            log.warn("   see Keycloak documentation here : https://www.keycloak.org/docs/latest/server_installation/index.html#_truststore");
        }
    }

    /**
     * 去除 PEM 头尾标记及换行符。
     *
     * @param pem 原始 PEM 字符串
     * @return 仅含 Base64 主体内容的字符串
     */
    private static String removeBeginEnd(String pem) {
        pem = pem.replace(PemUtils.BEGIN_CERT, "");
        pem = pem.replace(PemUtils.END_CERT, "");
        pem = pem.replace("\r\n", "");
        pem = pem.replace("\n", "");
        return pem.trim();
    }

    /**
     * 解码终端用户证书，含 NGINX {@code ssl_client_escaped_cert} 变量的 URL 解码。
     */
    @Override
    protected X509Certificate decodeCertificateFromPem(String pem) throws PemException {

        if (pem == null) {
            log.warn("End user TLS Certificate is NULL! ");
            return null;
        }
        if (certIsUrlEncoded) {
            pem = java.net.URLDecoder.decode(pem, StandardCharsets.UTF_8);
        }

        if (pem.startsWith(PemUtils.BEGIN_CERT)) {
            pem = removeBeginEnd(pem);
        }

        return PemUtils.decodeCertificate(pem);
    }

    /** {@inheritDoc} 借助 Keycloak 信任库重建 NGINX 无法转发的证书链。 */
    @Override
    protected void buildChain(HttpRequest httpRequest, List<X509Certificate> chain, X509Certificate clientCert) {
        log.debugf("End user certificate found : Subject DN=[%s]  SerialNumber=[%s]", clientCert.getSubjectX500Principal(), clientCert.getSerialNumber());

        // 使用 Keycloak 信任库重建终端用户证书链（NGINX 无法在头中传递 CA 链）
        X509Certificate[] certChain = buildChain(clientCert);
        if (certChain == null || certChain.length == 0) {
            log.info("Impossible to rebuild end user cert chain : client certificate authentication will fail." );
            chain.add(clientCert);
        } else {
            for (X509Certificate caCert : certChain) {
                chain.add(caCert);
                log.debugf("Rebuilded user cert chain DN : %s", caCert.getSubjectX500Principal());
            }
        }
    }

    /**
     * NGINX 无法在 HTTP 头中传递 CA 链，此处用 Keycloak 信任库重建终端用户证书链。
     * <p>信任库须同时包含根 CA 与中间 CA 证书。</p>
     *
     * @param endUserAuthCert 终端用户客户端证书
     * @return 重建的证书链；失败时返回空数组
     */
    private X509Certificate[] buildChain(X509Certificate endUserAuthCert) {

        X509Certificate[] userCertChain = new X509Certificate[0];

        try {

            // 无信任库则无法重建链
            if (!isTruststoreLoaded) {
                log.warn("Keycloak Truststore is null, but it is required !");
                log.warn("  see https://www.keycloak.org/docs/latest/server_installation/index.html#_truststore");
                return userCertChain;
            }

            // 指定起始证书的选择器
            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(endUserAuthCert);

            // 信任锚点（根 CA 证书集合）
            Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
            for (X509Certificate trustedRootCert : trustedRootCerts) {
                trustAnchors.add(new TrustAnchor(trustedRootCert, null));
            }
            // 配置 PKIX 证书路径构建参数
            PKIXBuilderParameters pkixParams = new PKIXBuilderParameters( trustAnchors, selector);

            // 禁用 CRL 检查（吊销状态可能由 Keycloak 其他设置另行处理）
            pkixParams.setRevocationEnabled(false);
            pkixParams.setExplicitPolicyRequired(false);
            pkixParams.setAnyPolicyInhibited(false);
            pkixParams.setPolicyQualifiersRejected(false);
            pkixParams.setMaxPathLength(certificateChainLength);

            // 将中间证书与终端用户证书加入 CertStore
            intermediateCerts.add(endUserAuthCert);
            CollectionCertStoreParameters intermediateCAUserCert = new CollectionCertStoreParameters(intermediateCerts);
            CertStore intermediateCertStore = CryptoIntegration.getProvider().getCertStore(intermediateCAUserCert);
            pkixParams.addCertStore(intermediateCertStore);

            // 构建并验证认证路径（不含吊销状态）
            CertPathBuilder certPathBuilder = CryptoIntegration.getProvider().getCertPathBuilder();
            CertPath certPath = certPathBuilder.build(pkixParams).getCertPath();
            log.debug("Certification path building OK, and contains " + certPath.getCertificates().size() + " X509 Certificates");

            userCertChain = convertCertPathToX509CertArray(certPath);

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            log.error(e.getLocalizedMessage(),e);
        } catch (CertPathBuilderException e) {
            if (log.isEnabled(Level.TRACE)) {
                log.debug(e.getLocalizedMessage(),e);
            } else {
                log.warn(e.getLocalizedMessage());
            }
        } finally {
            if (isTruststoreLoaded) {
                // 从中间证书集合中移除临时加入的终端用户证书
                intermediateCerts.remove(endUserAuthCert);
            }
        }

        return userCertChain;
    }


    /**
     * 将 {@link CertPath} 转换为 X509 证书数组。
     *
     * @param certPath PKIX 构建出的证书路径
     * @return X509 证书数组
     */
    private X509Certificate[] convertCertPathToX509CertArray(CertPath certPath ) {

        X509Certificate[] x509certChain = new X509Certificate[0];
        if (certPath == null){
          return x509certChain;
        }

        List<X509Certificate> trustedX509Chain = new ArrayList<X509Certificate>();
        for (Certificate certificate : certPath.getCertificates()) {
            if (certificate instanceof X509Certificate) {
                trustedX509Chain.add((X509Certificate) certificate);
            }
        }

        return trustedX509Chain.toArray(x509certChain);

    }
}
