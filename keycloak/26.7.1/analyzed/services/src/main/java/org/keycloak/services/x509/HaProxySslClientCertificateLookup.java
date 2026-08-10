/*
 * Copyright 2017 Analytical Graphics, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * HAProxy 反向代理转发的 X.509 客户端证书查找实现。
 *
 * <p>HAProxy 目前不符合 RFC 9440：仅支持将整个证书链 Base64 编码，而 RFC 9440 要求每张证书单独 Base64 编码并以 {@code :} 包裹、多值以 CSV 分隔。
 * 若 HAProxy 后续支持该格式，可弃用本提供者而改用 {@link Rfc9440ClientCertificateLookup}
 * （参见 <a href="https://github.com/haproxy/haproxy/issues/2235">haproxy/#2235</a>）。</p>
 *
 * <p>头字段值须为 Base64 编码的 DER 证书，与 HAProxy 的
 * {@code ssl_c_der,base64} 和 {@code ssl_c_chain_der,base64} 采样获取一致。</p>
 *
 * <p>证书链读取支持两种模式：</p>
 *
 * <ul>
 *   <li><b>单头模式</b>（通过 {@code sslCertChain}）：整条链在一个头中，为拼接 DER 后 Base64 编码。
 *       仅加载前 {@code certificateChainLength} 张证书。HAProxy 配置示例：
 *       <pre>
 * http-request set-header Client-Cert %[ssl_c_der,base64]
 * http-request set-header Client-Cert-Chain %[ssl_c_chain_der,base64]
 *       </pre>
 *   </li>
 *   <li><b>索引头模式</b>（已弃用，基于 {@code sslCertChainPrefix}）：链中每张证书在独立头
 *       {@code {prefix}_{index}} 中，如 {@code Client-Cert-Chain_0}。
 *       当链中存在多张中间证书时 HAProxy 无法为每张单独设头，故仅适用于单中间证书场景。</li>
 * </ul>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/27/2017
 */
public class HaProxySslClientCertificateLookup extends AbstractClientCertificateFromHttpHeadersLookup {

    private static final Logger logger = Logger.getLogger(HaProxySslClientCertificateLookup.class);

    /** 单头模式下存放完整证书链的 HTTP 头名称。 */
    private final String sslCertChainHttpHeader;

    /**
     * 构造 HAProxy 证书查找器。
     *
     * @param sslClientCertHttpHeader 客户端叶子证书 HTTP 头名
     * @param sslCertChainHttpHeaderPrefix 索引头模式下的链头前缀（可为 null）
     * @param sslCertChainHttpHeader 单头模式下的链 HTTP 头名（可为 null）
     * @param certificateChainLength 最多加载的链证书数量
     */
    public HaProxySslClientCertificateLookup(String sslClientCertHttpHeader,
                                             String sslCertChainHttpHeaderPrefix,
                                             String sslCertChainHttpHeader,
                                             int certificateChainLength) {
        super(sslClientCertHttpHeader, sslCertChainHttpHeaderPrefix, certificateChainLength);
        this.sslCertChainHttpHeader = sslCertChainHttpHeader;
    }

    /** {@inheritDoc} 将 PEM 字符串解码为 X509 证书。 */
    @Override
    protected X509Certificate decodeCertificateFromPem(String pem) throws PemException {
        if (pem == null) {
            return null;
        }
        return PemUtils.decodeCertificate(pem);
    }

    /** {@inheritDoc} 将叶子证书加入链，并按配置从单头或索引头加载其余链证书。 */
    @Override
    protected void buildChain(HttpRequest httpRequest, List<X509Certificate> chain, X509Certificate cert) {
        chain.add(cert);
        if (sslCertChainHttpHeader != null) {
            try {
                addCertificateChainFromSingleHeader(httpRequest, chain);
            } catch (GeneralSecurityException e) {
                logger.warn(e.getMessage(), e);
            }
        } else {
            addCertificateChainFromIndexedHeaders(httpRequest, chain);
        }
    }

    /**
     * 从单个 HTTP 头解析 Base64 编码的 DER 证书链并追加到链列表。
     *
     * @param httpRequest 当前请求
     * @param chain 待追加的证书链列表
     */
    private void addCertificateChainFromSingleHeader(HttpRequest httpRequest, List<X509Certificate> chain) throws GeneralSecurityException {
        if (certificateChainLength == 0) {
            return;
        }

        String headerValue = getHeaderValue(httpRequest, sslCertChainHttpHeader);
        if (headerValue == null || headerValue.isEmpty()) {
            return;
        }

        byte[] derBytes;
        try {
            derBytes = Base64.getMimeDecoder().decode(headerValue);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Failed to decode base64 content from header " + sslCertChainHttpHeader, e);
        }

        try (InputStream is = new ByteArrayInputStream(derBytes)) {
            CryptoIntegration.getProvider().getX509CertFactory()
                  .generateCertificates(is)
                  .stream()
                  .limit(certificateChainLength)
                  .map(X509Certificate.class::cast)
                  .peek(cert -> logger.debugf("Parsed chain certificate: Subject DN=[%s]", cert.getSubjectX500Principal()))
                  .forEach(chain::add);
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to parse certificate chain from header " + sslCertChainHttpHeader, e);
        }
    }
}
