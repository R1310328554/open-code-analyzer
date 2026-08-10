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

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.common.util.PemException;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * 从 HTTP 请求头读取 X.509 客户端证书的抽象查找器。
 * <p>适用于 Keycloak 部署在反向代理（如 Apache）后、由代理转发客户端证书的场景。
 * 仅当 {@link HttpRequest#isProxyTrusted()} 为 true 时才信任请求头中的证书。</p>
 *
 * @author <a href="mailto:brat000012001@gmail.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 * @since 3/29/2017
 */

public abstract class AbstractClientCertificateFromHttpHeadersLookup implements X509ClientCertificateLookup {

    protected static final Logger logger = Logger.getLogger(AbstractClientCertificateFromHttpHeadersLookup.class);

    /** 存放客户端叶子证书的 HTTP 头名称 */
    protected final String sslClientCertHttpHeader;
    /** 证书链各节 HTTP 头名称前缀（后缀 _0、_1…） */
    protected final String sslCertChainHttpHeaderPrefix;
    /** 除叶子证书外额外读取的证书链节数 */
    protected final int certificateChainLength;

    /**
     * @param sslCientCertHttpHeader 客户端证书 HTTP 头名（不可为 null）
     * @param sslCertChainHttpHeaderPrefix 证书链头前缀，可为 null
     * @param certificateChainLength 额外链节数量，须 ≥ 0
     */
    public AbstractClientCertificateFromHttpHeadersLookup(String sslCientCertHttpHeader,
                                                          String sslCertChainHttpHeaderPrefix,
                                                          int certificateChainLength) {
        if (sslCientCertHttpHeader == null) {
            throw new IllegalArgumentException("sslClientCertHttpHeader");
        }

        if (certificateChainLength < 0) {
            throw new IllegalArgumentException("certificateChainLength must be greater or equal to zero");
        }

        this.sslClientCertHttpHeader = sslCientCertHttpHeader;
        this.sslCertChainHttpHeaderPrefix = sslCertChainHttpHeaderPrefix;
        this.certificateChainLength = certificateChainLength;
    }

    @Override
    public void close() {

    }

    /** 读取指定 HTTP 头的首个值。 */
    static String getHeaderValue(HttpRequest httpRequest, String headerName) {
        return httpRequest.getHttpHeaders().getRequestHeaders().getFirst(headerName);
    }

    /** 去除 PEM 字符串首尾的双引号包裹。 */
    private static String trimDoubleQuotes(String quotedString) {

        if (quotedString == null) {
            return null;
        }

        int len = quotedString.length();
        if (len > 1 && quotedString.charAt(0) == '"' &&
                quotedString.charAt(len - 1) == '"') {
            logger.trace("Detected a certificate enclosed in double quotes");
            return quotedString.substring(1, len - 1);
        }
        return quotedString;
    }

    /** 子类实现：将 PEM 字符串解码为 X509Certificate。 */
    protected abstract X509Certificate decodeCertificateFromPem(String pem) throws PemException;

    /** 从指定 HTTP 头读取并解码单个 X.509 证书。 */
    protected X509Certificate getCertificateFromHttpHeader(HttpRequest request, String httpHeader) throws GeneralSecurityException {
        String encodedCertificate = getHeaderValue(request, httpHeader);

        // 去除双引号包裹
        encodedCertificate = trimDoubleQuotes(encodedCertificate);

        if (encodedCertificate == null ||
                encodedCertificate.trim().length() == 0) {
            logger.warnf("HTTP header \"%s\" is empty", httpHeader);
            return null;
        }

        try {
            X509Certificate cert = decodeCertificateFromPem(encodedCertificate);
            if (cert == null) {
                logger.warnf("HTTP header \"%s\" does not contain a valid x.509 certificate\n%s",
                        httpHeader, encodedCertificate);
            } else {
                logger.debugf("Found a valid x.509 certificate in \"%s\" HTTP header",
                        httpHeader);
            }
            return cert;
        }
        catch(PemException e) {
            logger.error(e.getMessage(), e);
            throw new GeneralSecurityException(e);
        }
    }


    /**
     * 从 HTTP 头构建客户端证书链。
     * <p>代理未受信任时直接返回 null。</p>
     */
    @Override
    public final X509Certificate[] getCertificateChain(HttpRequest httpRequest) throws GeneralSecurityException {
        if (!httpRequest.isProxyTrusted()) {
            logger.warnf("HTTP header \"%s\" is not trusted", sslClientCertHttpHeader);
            return null;
        }
        List<X509Certificate> chain = new ArrayList<>();

        // 读取客户端叶子证书
        X509Certificate cert = getCertificateFromHttpHeader(httpRequest, sslClientCertHttpHeader);
        if (cert != null) {
            buildChain(httpRequest, chain, cert);
        }
        return chain.toArray(new X509Certificate[0]);
    }

    /** 将叶子证书加入链并从索引头读取后续链节。 */
    protected void buildChain(HttpRequest httpRequest, List<X509Certificate> chain, X509Certificate cert) {
        chain.add(cert);
        addCertificateChainFromIndexedHeaders(httpRequest, chain);
    }

    /** 从 {@code prefix_0}、{@code prefix_1}… 索引头读取证书链中间/根证书。 */
    protected void addCertificateChainFromIndexedHeaders(HttpRequest httpRequest, List<X509Certificate> chain) {
        for (int i = 0; i < certificateChainLength; i++) {
            try {
                String headerName = String.format("%s_%s", sslCertChainHttpHeaderPrefix, i);
                X509Certificate cert = getCertificateFromHttpHeader(httpRequest, headerName);
                if (cert != null) {
                    chain.add(cert);
                }
            } catch (GeneralSecurityException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }
}
