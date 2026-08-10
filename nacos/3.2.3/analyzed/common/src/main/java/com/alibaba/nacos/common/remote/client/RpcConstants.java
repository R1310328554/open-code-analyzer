/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * RPC 模块配置常量：定义客户端/服务端/集群 TLS 属性键前缀、后缀及
 * 带 {@link RpcConfigLabel} 注解的可扫描配置项集合。
 * RpcConstants.
 *
 * @author githubcheng2978.
 */
public class RpcConstants {
    
    /** SDK 客户端 RPC 配置前缀 */
    public static final String NACOS_CLIENT_RPC = "nacos.remote.client.rpc";
    
    /** 服务端 RPC TLS 配置前缀 */
    public static final String NACOS_SERVER_RPC = "nacos.remote.server.rpc.tls";
    
    /** 集群节点间 RPC TLS 配置前缀 */
    public static final String NACOS_PEER_RPC = "nacos.remote.peer.rpc.tls";
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_ENABLE = NACOS_CLIENT_RPC + ClientSuffix.TLS_ENABLE;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_PROVIDER =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_PROVIDER;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_MUTUAL_AUTH = NACOS_CLIENT_RPC + ClientSuffix.MUTUAL_AUTH;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_PROTOCOLS =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_PROTOCOLS;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CIPHERS = NACOS_CLIENT_RPC + ClientSuffix.TLS_CIPHERS;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CERT_CHAIN_PATH =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_CERT_CHAIN_PATH;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_CERT_KEY =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_CERT_KEY;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_PWD =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_TRUST_PWD;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_TRUST_COLLECTION_CHAIN_PATH;
    
    @RpcConfigLabel
    public static final String RPC_CLIENT_TLS_TRUST_ALL =
        NACOS_CLIENT_RPC + ClientSuffix.TLS_TRUST_ALL;
    
    /** 启动时反射收集的带 RpcConfigLabel 的配置键名集合 */
    private static final Set<String> CONFIG_NAMES = new HashSet<>();
    
    static {
        Class clazz = RpcConstants.class;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (declaredField.getType().equals(String.class) && null != declaredField.getAnnotation(
                RpcConfigLabel.class)) {
                try {
                    CONFIG_NAMES.add((String) declaredField.get(null));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }
    
    /**
     * 客户端 RPC TLS 配置属性后缀集合，与 {@link #NACOS_CLIENT_RPC} 前缀拼接成完整键。
     * Enumeration of common suffixes for RPC configuration properties. Each enum constant represents a specific
     * configuration attribute suffix. This allows for the construction of complete configuration property keys.
     */
    public class ClientSuffix {
        
        /**
         * Suffix for 'tls.enable' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_ENABLE = ".tls.enable";
        
        /**
         * Suffix for 'tls.provider' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_PROVIDER = ".tls.provider";
        
        /**
         * Suffix for 'tls.mutualAuth' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String MUTUAL_AUTH = ".tls.mutualAuth";
        
        /**
         * Suffix for 'tls.protocols' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_PROTOCOLS = ".tls.protocols";
        
        /**
         * Suffix for 'tls.ciphers' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CIPHERS = ".tls.ciphers";
        
        /**
         * Suffix for 'tls.certChainFile' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CERT_CHAIN_PATH = ".tls.certChainFile";
        
        /**
         * Suffix for 'tls.certPrivateKey' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CERT_KEY = ".tls.certPrivateKey";
        
        /**
         * Suffix for 'tls.certPrivateKeyPassword' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_PWD = ".tls.certPrivateKeyPassword";
        
        /**
         * Suffix for 'tls.trustCollectionChainPath' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_COLLECTION_CHAIN_PATH =
            ".tls.trustCollectionChainPath";
        
        /**
         * Suffix for 'tls.trustAll' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_ALL = ".tls.trustAll";
    }
    
    /**
     * 服务端/集群 RPC TLS 配置属性后缀，与 {@link #NACOS_PEER_RPC} 等前缀拼接。
     * Enumeration of common suffixes for RPC configuration properties. Each enum constant represents a specific
     * configuration attribute suffix. This allows for the construction of complete configuration property keys.
     */
    public class ServerSuffix {
        
        /**
         * Suffix for 'tls.enable' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_ENABLE = ".enableTls";
        
        /**
         * Suffix for 'tls.provider' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_PROVIDER = ".sslProvider";
        
        /**
         * Suffix for 'tls.mutualAuth' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String MUTUAL_AUTH = ".mutualAuthEnable";
        
        /**
         * Suffix for 'tls.protocols' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_PROTOCOLS = ".protocols";
        
        /**
         * Suffix for 'tls.ciphers' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CIPHERS = ".ciphers";
        
        /**
         * Suffix for 'tls.certChainFile' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CERT_CHAIN_PATH = ".certChainFile";
        
        /**
         * Suffix for 'tls.certPrivateKey' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_CERT_KEY = ".certPrivateKey";
        
        /**
         * Suffix for 'tls.certPrivateKeyPassword' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_PWD = ".certPrivateKeyPassword";
        
        /**
         * Suffix for 'tls.trustCollectionChainPath' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_COLLECTION_CHAIN_PATH = ".trustCollectionCertFile";
        
        /**
         * Suffix for 'tls.trustAll' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String TLS_TRUST_ALL = ".trustAll";
        
        /**
         * Suffix for '.sslContextRefresher' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String SSL_CONTEXT_REFRESHER = ".sslContextRefresher";
        
        /**
         * Suffix for '.compatibility' configuration property.
          * <p>RPC 配置常量；详见类级说明。</p>
         */
        public static final String COMPATIBILITY = ".compatibility";
    }
    
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface RpcConfigLabel {
        
    }
    
    /** 返回所有已注册的 RPC 配置键名（不可变集合） */
    public static Set<String> getRpcParams() {
        return Collections.unmodifiableSet(CONFIG_NAMES);
    }
    
}
