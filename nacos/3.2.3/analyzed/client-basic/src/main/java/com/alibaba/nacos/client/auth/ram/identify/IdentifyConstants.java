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

package com.alibaba.nacos.client.auth.ram.identify;

/**
 * Identify Constants.
 * <p>SPAS/RAM 凭证发现与 HTTP 头相关常量：Properties 键名、默认文件路径、Docker/环境变量键等。</p>
 *
 * @author Nacos
 */
public class IdentifyConstants {
    
    /** 凭证 Properties 中 AccessKey 字段名 */
    public static final String ACCESS_KEY = "accessKey";
    
    /** 凭证 Properties 中 SecretKey 字段名 */
    public static final String SECRET_KEY = "secretKey";
    
    /** STS 临时凭证 SecurityToken 请求头名 */
    public static final String SECURITY_TOKEN_HEADER = "Spas-SecurityToken";
    
    /** 凭证 Properties 中租户 ID 字段名 */
    public static final String TENANT_ID = "tenantId";
    
    /** classpath 默认凭证文件名 */
    public static final String PROPERTIES_FILENAME = "spas.properties";
    
    /** 宿主机默认凭证目录前缀 */
    public static final String CREDENTIAL_PATH = "/home/admin/.spas_key/";
    
    /** 无 appName 时的默认凭证子目录/文件名 */
    public static final String CREDENTIAL_DEFAULT = "default";
    
    /** 容器内实例信息凭证文件路径 */
    public static final String DOCKER_CREDENTIAL_PATH = "/etc/instanceInfo";
    
    /** Docker 凭证文件内 AccessKey 键名 */
    public static final String DOCKER_ACCESS_KEY = "env_spas_accessKey";
    
    /** Docker 凭证文件内 SecretKey 键名 */
    public static final String DOCKER_SECRET_KEY = "env_spas_secretKey";
    
    /** Docker 凭证文件内 tenantId 键名 */
    public static final String DOCKER_TENANT_ID = "ebv_spas_tenantId";
    
    /** 环境变量 AccessKey 键名 */
    public static final String ENV_ACCESS_KEY = "spas_accessKey";
    
    /** 环境变量 SecretKey 键名 */
    public static final String ENV_SECRET_KEY = "spas_secretKey";
    
    /** 环境变量 tenantId 键名 */
    public static final String ENV_TENANT_ID = "tenant.id";
    
    /** CredentialService 单例 Map 中默认应用的键 */
    public static final String NO_APP_NAME = "";
    
    /** 未传 appName 时从系统属性读取项目名 */
    public static final String PROJECT_NAME_PROPERTY = "project.name";
    
    /** RAM 角色名系统属性键 */
    public static final String RAM_ROLE_NAME_PROPERTY = "ram.role.name";
    
    /** STS 凭证刷新间隔配置键（毫秒） */
    public static final String REFRESH_TIME_PROPERTY = "time.to.refresh.in.millisecond";
    
    /** 安全凭证相关配置属性名 */
    public static final String SECURITY_PROPERTY = "security.credentials";
    
    /** 远程拉取凭证 URL 配置键 */
    public static final String SECURITY_URL_PROPERTY = "security.credentials.url";
    
    /** 是否缓存安全凭证的配置键 */
    public static final String SECURITY_CACHE_PROPERTY = "cache.security.credentials";
}
