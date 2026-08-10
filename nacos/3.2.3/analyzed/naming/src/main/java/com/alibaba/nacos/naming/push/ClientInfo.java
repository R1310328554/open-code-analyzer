/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.push;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

/**
 * 客户端信息解析器，从 User-Agent 识别客户端类型与版本号。
 *
 * <p>推送链路根据 {@link ClientType} 与 Jackson {@link Version} 做协议适配与兼容性判断。</p>
 *
 * @author nacos
 */
public class ClientInfo {
    
    /** 客户端版本号（解析自 User-Agent）。 */
    public Version version;
    
    /** 客户端类型枚举。 */
    public ClientType type;
    
    /** 根据 HTTP User-Agent 字符串解析客户端类型与版本。 */
    public ClientInfo(String userAgent) {
        String versionStr = StringUtils.isEmpty(userAgent) ? StringUtils.EMPTY : userAgent;
        this.type = ClientType.getType(versionStr);
        if (versionStr.startsWith(ClientTypeDescription.CPP_CLIENT)) {
            this.type = ClientType.C;
        }
        this.version = parseVersion(versionStr);
    }
    
    private Version parseVersion(String versionStr) {
        if (StringUtils.isBlank(versionStr) || ClientType.UNKNOWN.equals(this.type)) {
            return Version.unknownVersion();
        }
        int versionStartIndex = versionStr.indexOf(":v");
        if (versionStartIndex < 0) {
            return Version.unknownVersion();
        }
        return VersionUtil.parseVersion(versionStr.substring(versionStartIndex + 2), null, null);
    }
    
    public enum ClientType {
        
        /** Go 语言客户端。 */
        GO(ClientTypeDescription.GO_CLIENT),
        /** Java 语言客户端。 */
        JAVA(ClientTypeDescription.JAVA_CLIENT),
        /** C 语言客户端。 */
        C(ClientTypeDescription.C_CLIENT),
        /** C# 语言客户端。 */
        CSHARP(ClientTypeDescription.CSHARP_CLIENT),
        /** PHP 语言客户端。 */
        PHP(ClientTypeDescription.PHP_CLIENT),
        /** DNS-F 客户端。 */
        DNS(ClientTypeDescription.DNSF_CLIENT),
        /** Nginx/Tengine 客户端。 */
        TENGINE(ClientTypeDescription.NGINX_CLIENT),
        /** Java SDK 客户端。 */
        JAVA_SDK(ClientTypeDescription.SDK_CLIENT),
        /** Nacos 集群节点间互推。 */
        NACOS_SERVER(UtilsAndCommons.NACOS_SERVER_HEADER),
        /** 未知客户端类型。 */
        UNKNOWN(UtilsAndCommons.UNKNOWN_SITE);
        
        private final String clientTypeDescription;
        
        ClientType(String clientTypeDescription) {
            this.clientTypeDescription = clientTypeDescription;
        }
        
        public String getClientTypeDescription() {
            return clientTypeDescription;
        }
        
        /** 按 User-Agent 前缀匹配客户端类型，未匹配则返回 {@link #UNKNOWN}。 */
        public static ClientType getType(String userAgent) {
            for (ClientType each : ClientType.values()) {
                if (userAgent.startsWith(each.getClientTypeDescription())) {
                    return each;
                }
            }
            return UNKNOWN;
        }
    }
    
    /** 各客户端 User-Agent 前缀常量定义。 */
    public static class ClientTypeDescription {
        
        public static final String JAVA_CLIENT = "Nacos-Java-Client";
        
        public static final String DNSF_CLIENT = "Nacos-DNS";
        
        public static final String C_CLIENT = "Nacos-C-Client";
        
        public static final String SDK_CLIENT = "Nacos-SDK-Java";
        
        public static final String NGINX_CLIENT = "unit-nginx";
        
        public static final String CPP_CLIENT = "vip-client4cpp";
        
        public static final String GO_CLIENT = "Nacos-Go-Client";
        
        public static final String PHP_CLIENT = "Nacos-Php-Client";
        
        public static final String CSHARP_CLIENT = "Nacos-CSharp-Client";
    }
    
}
