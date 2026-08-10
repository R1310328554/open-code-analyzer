/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.ldap;

import com.alibaba.nacos.common.tls.TlsHelper;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.Context;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 定制 LDAP 上下文源。
 *
 * <p>封装 LDAP/LDAPS 连接参数、连接池与 SSL 套接字工厂；LDAPS 场景下禁用端点校验并使用 Nacos TLS 上下文。</p>
 *
 * @author karsonto
 */
public class NacosLdapContextSource extends LdapContextSource {
    
    /** JNDI 环境属性集合。 */
    private final Map<String, Object> config = new HashMap<>(16);
    
    /** 是否使用 LDAPS（SSL）协议。 */
    private boolean useTsl = false;
    
    private static final String LDAPS = "ldaps";
    
    /**
     * 根据连接参数构造上下文源并初始化环境。
     *
     * @param ldapUrl      LDAP 服务 URL
     * @param ldapBaseDc   搜索基准 DN
     * @param userDn       绑定用户 DN
     * @param password     绑定密码
     * @param ldapTimeOut  连接超时（毫秒字符串）
     */
    public NacosLdapContextSource(String ldapUrl, String ldapBaseDc, String userDn, String password,
        String ldapTimeOut) {
        this.setUrl(ldapUrl);
        this.setBase(ldapBaseDc);
        this.setUserDn(userDn);
        this.setPassword(password);
        if (ldapUrl.toLowerCase().startsWith(LDAPS)) {
            useTsl = true;
        }
        this.setPooled(true);
        init(ldapTimeOut);
    }
    
    /**
     * 初始化 LdapContextSource 的 JNDI 环境属性。
     *
     * @param ldapTimeOut LDAP 连接超时（毫秒）
     */
    public void init(String ldapTimeOut) {
        if (useTsl) {
            // LDAPS：禁用端点主机名校验，使用自定义 SSL 套接字工厂
            System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");
            config.put("java.naming.security.protocol", "ssl");
            config.put("java.naming.ldap.factory.socket", LdapSslSocketFactory.class.getName());
            config.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        }
        config.put("java.naming.ldap.attributes.binary", "objectGUID");
        config.put("com.sun.jndi.ldap.connect.timeout", ldapTimeOut);
        this.setBaseEnvironmentProperties(config);
        this.afterPropertiesSet();
    }
    
    /**
     * LDAPS 专用 SSL 套接字工厂，基于 Nacos {@link TlsHelper} 构建信任上下文。
     */
    @SuppressWarnings("checkstyle:EmptyLineSeparator")
    public static class LdapSslSocketFactory extends SSLSocketFactory {
        
        private SSLSocketFactory socketFactory;
        
        /** 使用 Nacos TLS 助手创建 SSL 上下文并获取套接字工厂。 */
        public LdapSslSocketFactory() {
            try {
                this.socketFactory = TlsHelper.buildSslContext(true).getSocketFactory();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                Loggers.AUTH.error("Failed to create SSLContext", e);
            }
        }
        
        @Override
        public String[] getDefaultCipherSuites() {
            return socketFactory.getDefaultCipherSuites();
        }
        
        @Override
        public String[] getSupportedCipherSuites() {
            return socketFactory.getSupportedCipherSuites();
        }
        
        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
            return socketFactory.createSocket(socket, s, i, b);
        }
        
        @Override
        public Socket createSocket(String s, int i) throws IOException {
            return socketFactory.createSocket(s, i);
        }
        
        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1)
            throws IOException {
            return socketFactory.createSocket(s, i, inetAddress, i1);
        }
        
        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            return socketFactory.createSocket(inetAddress, i);
        }
        
        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1)
            throws IOException {
            return socketFactory.createSocket(inetAddress, i, inetAddress1, i1);
        }
    }
    
}
