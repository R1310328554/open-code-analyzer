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

package com.alibaba.nacos.plugin.auth.constant;

/**
 * Nacos 认证插件通用常量定义。
 *
 * <p>按功能划分为 Auth（认证开关与配置键）、Resource（资源标识）、
 * Identity（身份上下文键）和 Tag（注解标签）四组。</p>
 *
 * @author onew
 */
public class Constants {
    
    /**
     * 认证核心配置项键名。
     */
    public static class Auth {
        
        /** 是否启用认证总开关。 */
        public static final String NACOS_CORE_AUTH_ENABLED = "nacos.core.auth.enabled";
        
        /** 是否启用控制台认证。 */
        public static final String NACOS_CORE_AUTH_CONSOLE_ENABLED =
            "nacos.core.auth.console.enabled";
        
        /** 是否启用管理员认证。 */
        public static final String NACOS_CORE_AUTH_ADMIN_ENABLED = "nacos.core.auth.admin.enabled";
        
        /** 认证系统类型（如 nacos、ldap、oidc）。 */
        public static final String NACOS_CORE_AUTH_SYSTEM_TYPE = "nacos.core.auth.system.type";
        
        /** 是否启用认证结果缓存。 */
        public static final String NACOS_CORE_AUTH_CACHING_ENABLED =
            "nacos.core.auth.caching.enabled";
        
        /** 服务端身份校验密钥名。 */
        public static final String NACOS_CORE_AUTH_SERVER_IDENTITY_KEY =
            "nacos.core.auth.server.identity.key";
        
        /** 服务端身份校验密钥值。 */
        public static final String NACOS_CORE_AUTH_SERVER_IDENTITY_VALUE =
            "nacos.core.auth.server.identity.value";
        
    }
    
    /**
     * 资源相关常量。
     */
    public static class Resource {
        
        /** 资源标识各段之间的分隔符。 */
        public static final String SPLITTER = ":";
        
        /** 通配符，表示匹配任意资源。 */
        public static final String ANY = "*";
        
        /** 资源属性中的 action 键名。 */
        public static final String ACTION = "action";
        
        /** 资源属性中的请求类名键。 */
        public static final String REQUEST_CLASS = "requestClass";
        
        /** 控制台资源名称前缀。 */
        public static final String CONSOLE_RESOURCE_NAME_PREFIX = "console/";
        
        /** AI 资源子类型键名。 */
        public static final String AI_TYPE = "aiType";
        
        /** AI 资源类型：MCP。 */
        public static final String AI_TYPE_MCP = "mcp";
        
        /** AI 资源类型：Agent。 */
        public static final String AI_TYPE_AGENT = "agent";
        
        /** AI 资源类型：Skill。 */
        public static final String AI_TYPE_SKILL = "skill";
        
        /** AI 资源类型：Prompt。 */
        public static final String AI_TYPE_PROMPT = "prompt";
        
        /** AI 资源类型：AgentSpec。 */
        public static final String AI_TYPE_AGENT_SPEC = "agentSpec";
    }
    
    /**
     * 身份上下文相关键名。
     */
    public static class Identity {
        
        /** 身份 ID 在上下文中的键名。 */
        public static final String IDENTITY_ID = "identity_id";
        
        /** HTTP 请求头 X-Real-IP 键名。 */
        public static final String X_REAL_IP = "X-Real-IP";
        
        /** 远程 IP 在上下文中的键名。 */
        public static final String REMOTE_IP = "remote_ip";
        
        /** 身份上下文对象在上下文中的键名。 */
        public static final String IDENTITY_CONTEXT = "identity_context";
    }
    
    /**
     * {@link com.alibaba.nacos.auth.annotation.Secured} 注解标签常量。
     */
    public static class Tag {
        
        /** 标记接口仅需身份校验、无需权限校验。 */
        public static final String ONLY_IDENTITY = "only_identity";
        
        /**
         * 标记 {@link com.alibaba.nacos.auth.annotation.Secured} 接口允许匿名访问。
         */
        public static final String ALLOW_ANONYMOUS = "allowAnonymous";
        
        /** 跨命名空间操作的特殊标签组合键。 */
        public static final String SECURED_SPECIAL_TAGS =
            com.alibaba.nacos.api.common.Constants.NAMESPACE_ID
                + Resource.SPLITTER
                + com.alibaba.nacos.api.common.Constants.TARGET_NAMESPACE_ID;
    }
}
