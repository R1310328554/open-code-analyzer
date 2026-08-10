/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.tls;

import com.alibaba.nacos.common.utils.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * TLS 信任管理器工具：根据是否开启客户端认证及信任证书路径，
 * 返回基于 JKS 的安全 {@link javax.net.ssl.TrustManager} 或信任所有证书的
 * {@link #trustAll} 降级方案（构建失败时 WARN 并降级）。
 * A TrustManager tool returns the specified TrustManager.
 *
 * @author wangwei
 */
public final class SelfTrustManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTrustManager.class);
    
    @SuppressWarnings("checkstyle:WhitespaceAround")
    /** 信任所有服务端/客户端证书的 TrustManager 数组（无双向校验） */
    static TrustManager[] trustAll = new TrustManager[] {new X509TrustManager() {
        
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};
    
    /**
     * 按配置选择 TrustManager：needAuth 且证书路径有效时加载 JKS 信任库；
     * 否则或构建失败时返回 {@link #trustAll}。
     *
     * @param needAuth      是否启用客户端/server 证书校验
     * @param trustCertPath 信任根/链证书文件路径
     * @return TrustManager 数组
     */
    public static TrustManager[] trustManager(boolean needAuth, String trustCertPath) {
        if (needAuth) {
            try {
                return trustCertPath == null ? null : buildSecureTrustManager(trustCertPath);
            } catch (SSLException e) {
                LOGGER.warn("degrade trust manager as build failed, " + "will trust all certs.");
                return trustAll;
            }
        } else {
            return trustAll;
        }
    }
    
    /** 从 X.509 证书文件构建 JKS TrustManagerFactory */
    private static TrustManager[] buildSecureTrustManager(String trustCertPath)
        throws SSLException {
        TrustManagerFactory selfTmf;
        InputStream in = null;
        
        try {
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            selfTmf = TrustManagerFactory.getInstance(algorithm);
            
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, null);
            
            in = new FileInputStream(trustCertPath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            
            Collection<X509Certificate> certs =
                (Collection<X509Certificate>) cf.generateCertificates(in);
            int count = 0;
            for (Certificate cert : certs) {
                trustKeyStore.setCertificateEntry("cert-" + (count++), cert);
            }
            
            selfTmf.init(trustKeyStore);
            return selfTmf.getTrustManagers();
        } catch (Exception e) {
            LOGGER.error("build client trustManagerFactory failed", e);
            throw new SSLException(e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }
}
