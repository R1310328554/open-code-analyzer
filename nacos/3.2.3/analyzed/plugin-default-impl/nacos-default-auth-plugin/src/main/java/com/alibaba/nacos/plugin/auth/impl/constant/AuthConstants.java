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

package com.alibaba.nacos.plugin.auth.impl.constant;

import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.impl.utils.PasswordEncoderUtil;

/**
 * 默认鉴权插件常量定义。
 *
 * <p>涵盖插件类型标识、HTTP 头、Token 配置键、LDAP 参数、 V3 API 路径及 AI 匿名访问相关系统预留用户名/角色。</p>
 *
 * @author onew
 */
public class AuthConstants {
    
    /** 内置 Nacos 本地鉴权插件类型标识。 */
    public static final String AUTH_PLUGIN_TYPE = "nacos";
    
    /** LDAP 鉴权插件类型标识。 */
    public static final String LDAP_AUTH_PLUGIN_TYPE = "ldap";
    
    /** 全局管理员 Spring Security 角色名。 */
    public static final String GLOBAL_ADMIN_ROLE = "ROLE_ADMIN";
    
    /** HTTP Authorization 请求头名称。 */
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    /** JWT Token 前缀（Bearer 方案）。 */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /** 默认内置用户名。 */
    public static final String DEFAULT_USER = "nacos";
    
    /** 登录请求参数字段：用户名。 */
    public static final String PARAM_USERNAME = "username";
    
    /** 登录请求参数字段：密码。 */
    public static final String PARAM_PASSWORD = "password";
    
    /**
     * 控制台资源名前缀（已废弃）。
     *
     * @deprecated Use {@link Constants.Resource#CONSOLE_RESOURCE_NAME_PREFIX} instead.
     */
    @Deprecated
    public static final String CONSOLE_RESOURCE_NAME_PREFIX =
        Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX;
    
    /** 修改密码操作的资源入口标识。 */
    public static final String UPDATE_PASSWORD_ENTRY_POINT =
        CONSOLE_RESOURCE_NAME_PREFIX + "user/password";
    
    /** 分布式锁 gRPC 操作资源入口。 */
    public static final String LOCK_OPERATOR_POINT = "grpc/lock";
    
    /** Session/上下文中的 Nacos 用户属性键。 */
    public static final String NACOS_USER_KEY = "nacosuser";
    
    /** JWT 签名密钥配置项。 */
    public static final String TOKEN_SECRET_KEY = "nacos.core.auth.plugin.nacos.token.secret.key";
    
    public static final String DEFAULT_TOKEN_SECRET_KEY = "";
    
    /** JWT 过期时间（秒）配置项。 */
    public static final String TOKEN_EXPIRE_SECONDS =
        "nacos.core.auth.plugin.nacos.token.expire.seconds";
    
    /** 默认 Token 有效期：18000 秒（5 小时）。 */
    public static final Long DEFAULT_TOKEN_EXPIRE_SECONDS = 18_000L;
    
    /** LDAP 服务器 URL 配置项。 */
    public static final String NACOS_CORE_AUTH_LDAP_URL = "nacos.core.auth.ldap.url";
    
    public static final String NACOS_CORE_AUTH_LDAP_BASEDC = "nacos.core.auth.ldap.basedc";
    
    public static final String NACOS_CORE_AUTH_LDAP_TIMEOUT = "nacos.core.auth.ldap.timeout";
    
    public static final String NACOS_CORE_AUTH_LDAP_USERDN = "nacos.core.auth.ldap.userDn";
    
    public static final String NACOS_CORE_AUTH_LDAP_PASSWORD = "nacos.core.auth.ldap.password";
    
    public static final String NACOS_CORE_AUTH_LDAP_FILTER_PREFIX =
        "nacos.core.auth.ldap.filter.prefix";
    
    public static final String NACOS_CORE_AUTH_CASE_SENSITIVE =
        "nacos.core.auth.ldap.case.sensitive";
    
    /** 是否忽略 LDAP 部分结果异常的配置项。 */
    public static final String NACOS_CORE_AUTH_IGNORE_PARTIAL_RESULT_EXCEPTION =
        "nacos.core.auth.ldap.ignore.partial.result.exception";
    
    public static final String LDAP_DEFAULT_ENCODED_PASSWORD =
        PasswordEncoderUtil.encode(System.getProperty("ldap.default.password", "nacos"));
    
    /** LDAP 同步用户在本地存储中的用户名前缀。 */
    public static final String LDAP_PREFIX = "LDAP_";
    
    /** BCrypt 允许的最大密码长度（字节）。 */
    public static final int MAX_PASSWORD_LENGTH = 72;
    
    /** V3 鉴权 Controller 基础路径前缀说明。 */
    /** V3 用户管理 API 路径。 */
    public static final String USER_PATH = "/v3/auth/user";
    
    /** V3 角色管理 API 路径。 */
    public static final String ROLE_PATH = "/v3/auth/role";
    
    /** V3 权限管理 API 路径。 */
    public static final String PERMISSION_PATH = "/v3/auth/permission";
    
    /** 系统预留匿名用户名，用于 PUBLIC AI 资源未认证访问。 */
    public static final String ANONYMOUS_USER = "__nacos_anonymous__";
    
    /** 系统预留匿名角色名，绑定 {@link #ANONYMOUS_USER}。 */
    public static final String ANONYMOUS_ROLE = "__nacos_anonymous_role__";
    
    /**
     * 标记 {@link com.alibaba.nacos.auth.annotation.Secured} API 允许匿名访问的标签值。
     *
     * @see Constants.Tag#ALLOW_ANONYMOUS
     */
    public static final String TAG_ALLOW_ANONYMOUS = Constants.Tag.ALLOW_ANONYMOUS;
    
    /** 控制 AI 资源匿名访问是否启用的配置项键名。 */
    public static final String NACOS_CORE_AUTH_NACOS_ANONYMOUS_AI_ENABLED =
        "nacos.core.auth.nacos.anonymous.ai.enabled";
}
