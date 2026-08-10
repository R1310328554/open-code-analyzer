/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.x509;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;

import org.keycloak.common.util.PemUtils;
import org.keycloak.http.HttpRequest;

import org.jboss.logging.Logger;

/**
 * Envoy 反向代理 SSL 客户端证书查找实现。
 * <p>从 Envoy 转发的 {@code x-forwarded-client-cert} HTTP 头中解析 URL 编码的 PEM 格式客户端证书及证书链。</p>
 *
 * @see <a href="https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_conn_man/headers#x-forwarded-client-cert">Envoy XFCC 文档</a>
 */
public class EnvoyProxySslClientCertificateLookup implements X509ClientCertificateLookup {

    private static final Logger logger = Logger.getLogger(EnvoyProxySslClientCertificateLookup.class);

    /** Envoy 转发客户端证书的标准 HTTP 头名称。 */
    protected final static String XFCC_HEADER = "x-forwarded-client-cert";
    /** XFCC 头中仅含叶子证书的键名。 */
    protected final static String XFCC_HEADER_CERT_KEY = "Cert";
    /** XFCC 头中含完整证书链（含叶子证书）的键名。 */
    protected final static String XFCC_HEADER_CHAIN_KEY = "Chain";

    /** {@inheritDoc} 无状态实现，无需释放资源。 */
    @Override
    public void close() {
    }


    /**
     * 从 Envoy 转发的 HTTP 请求中提取客户端证书链。
     *
     * <p>Envoy 在 {@code x-forwarded-client-cert} 头中以如下格式编码证书：</p>
     * <pre>
     *   x-forwarded-client-cert: key1="url encoded value 1";key2="url encoded value 2";...
     * </pre>
     *
     * <p>本实现支持以下键：</p>
     * <ul>
     *   <li>{@code Cert} — URL 编码 PEM 格式的叶子客户端证书</li>
     *   <li>{@code Chain} — URL 编码 PEM 格式的完整证书链（含叶子证书）</li>
     * </ul>
     *
     * @param httpRequest Envoy 转发的 HTTP 请求
     * @return 解析出的客户端证书链；不可信或无头时返回 {@code null}
     */
    @Override
    public X509Certificate[] getCertificateChain(HttpRequest httpRequest) throws GeneralSecurityException {
        if (!httpRequest.isProxyTrusted()) {
            logger.warnv("HTTP header \"{0}\" is not trusted", XFCC_HEADER);
            return null;
        }

        String xfcc = httpRequest.getHttpHeaders().getRequestHeaders().getFirst(XFCC_HEADER);
        if (xfcc == null) {
            return null;
        }

        X509Certificate[] certs = null;

        try {
            StringTokenizer st = new StringTokenizer(xfcc, ";");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int index = token.indexOf("=");
                if (index != -1) {
                    String key = token.substring(0, index).trim();
                    String value = token.substring(index + 1).trim();

                    if (key.equals(XFCC_HEADER_CHAIN_KEY)) {
                        // Chain 含完整链（含叶子证书），找到即可停止解析
                        certs = PemUtils.decodeCertificates(decodeValue(value));
                        break;
                    } else if (key.equals(XFCC_HEADER_CERT_KEY)) {
                        // Cert 仅含叶子证书，继续查找是否还有 Chain 键
                        certs = PemUtils.decodeCertificates(decodeValue(value));
                    }
                }
            }
        } catch (Exception e) {
            logger.warnv("Failed to extract client certificate from x-forwarded-client-cert header: {0}",
                    e.getMessage());
            throw new SecurityException("Failed to extract client certificate from x-forwarded-client-cert header", e);
        }

        return certs;
    }

    /**
     * 解码 XFCC 头中的 URL 编码值：去除引号并 URL 解码。
     *
     * @param value 原始头字段值
     * @return 解码后的 PEM 字符串
     */
    private String decodeValue(String value) {
        // 去除首尾引号（如有）
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
