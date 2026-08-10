/*
 * Copyright 2023 The Netty Project
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

package io.netty.handler.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

/**
 * Wraps an existing {@link X509ExtendedTrustManager} and enhances the {@link CertificateException} that is thrown
 * because of hostname validation.
 *
 * <p>包装 {@link X509ExtendedTrustManager}，在主机名校验失败时附加 SNI、peerHost 与 SAN/CN 详情，便于排查。</p>
 */
final class EnhancingX509ExtendedTrustManager extends X509ExtendedTrustManager {

    // X509 SAN 类型常量：DNS(2)、URI(6)、IP(7)，见 getSubjectAlternativeNames()
    static final int ALTNAME_DNS = 2;
    static final int ALTNAME_URI = 6;
    static final int ALTNAME_IP = 7;
    /** SAN 列表格式化分隔符。 */
    private static final String SEPARATOR = ", ";

    /** 被装饰的扩展信任管理器。 */
    private final X509ExtendedTrustManager wrapped;

    /** 包内构造：要求 wrapped 实际为 X509ExtendedTrustManager。 */
    EnhancingX509ExtendedTrustManager(X509TrustManager wrapped) {
        this.wrapped = (X509ExtendedTrustManager) wrapped;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        wrapped.checkClientTrusted(chain, authType, socket);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        try {
            wrapped.checkServerTrusted(chain, authType, socket);
        } catch (CertificateException e) {
            throwEnhancedCertificateException(e, chain,
                    socket instanceof SSLSocket ? ((SSLSocket) socket).getHandshakeSession() : null);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        wrapped.checkClientTrusted(chain, authType, engine);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        try {
            wrapped.checkServerTrusted(chain, authType, engine);
        } catch (CertificateException e) {
            throwEnhancedCertificateException(e, chain, engine != null ? engine.getHandshakeSession() : null);
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        wrapped.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            wrapped.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            throwEnhancedCertificateException(e, chain, null);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return wrapped.getAcceptedIssuers();
    }

    private static void throwEnhancedCertificateException(CertificateException e, X509Certificate[] chain,
                                                          SSLSession session) throws CertificateException {
        // 仅能根据 JDK HostnameChecker 固定英文消息前缀识别主机名失败
        String message = e.getMessage();
        if (message != null &&
                (message.startsWith("No subject alternative") || message.startsWith("No name matching"))) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(message);
            // 去掉消息末尾多余句点
            if (message.charAt(message.length() - 1) == '.') {
                sb.setLength(sb.length() - 1);
            }
            if (session != null) {
                sb.append(" for SNIHostName=").append(getSNIHostName(session))
                        .append(" and peerHost=").append(session.getPeerHost());
            }
            sb.append(" in the chain of ").append(chain.length).append(" certificate(s):");
            for (int i = 0; i < chain.length; i++) {
                X509Certificate cert = chain[i];
                Collection<List<?>> collection = cert.getSubjectAlternativeNames();
                sb.append(' ').append(i + 1).append(". subjectAlternativeNames=[");
                if (collection != null) {
                    boolean hasNames = false;
                    for (List<?> altNames : collection) {
                        if (altNames.size() < 2) {
                            // We expect at least a pair of 'nameType:value' in that list.
                            continue;
                        }
                        final int nameType = ((Integer) altNames.get(0)).intValue();
                        // 格式化输出 DNS/IP/URI 类型 SAN
                        if (nameType == ALTNAME_DNS) {
                            sb.append("DNS");
                        } else if (nameType == ALTNAME_IP) {
                            sb.append("IP");
                        } else if (nameType == ALTNAME_URI) {
                            // gRPC/SPIFFE 环境常见 URI SAN，虽不参与主机名匹配但有助于调试
                            // Though the hostname matcher won't be looking at them, having them there can help
                            // debugging cases where hostname verification was enabled when it shouldn't be.
                            sb.append("URI");
                        } else {
                            continue;
                        }
                        sb.append(':').append((String) altNames.get(1)).append(SEPARATOR);
                        hasNames = true;
                    }
                    if (hasNames) {
                        // 去掉 SAN 列表末尾分隔符
                        sb.setLength(sb.length() - SEPARATOR.length());
                    }
                }
                sb.append("], CN=").append(getCommonName(cert)).append('.');
            }
            throw new CertificateException(sb.toString(), e);
        }
        throw e;
    }

    private static String getSNIHostName(SSLSession session) {
        if (!(session instanceof ExtendedSSLSession)) {
            return null;
        }
        List<SNIServerName> names = ((ExtendedSSLSession) session).getRequestedServerNames();
        for (SNIServerName sni : names) {
            if (sni instanceof SNIHostName) {
                SNIHostName hostName = (SNIHostName) sni;
                return hostName.getAsciiName();
            }
        }
        return null;
    }

    private static String getCommonName(X509Certificate cert) {
        try {
            // 解析 X500Principal 提取 CN，避免依赖已弃用 getSubjectDN
            X500Principal principal = cert.getSubjectX500Principal();
            // 2. Parse the DN using LdapName
            LdapName ldapName = new LdapName(principal.getName());
            // 3. Iterate over the Relative Distinguished Names (RDNs) to find CN
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return rdn.getValue().toString();
                }
            }
        } catch (Exception ignore) {
            // ignore
        }
        return "null";
    }
}
