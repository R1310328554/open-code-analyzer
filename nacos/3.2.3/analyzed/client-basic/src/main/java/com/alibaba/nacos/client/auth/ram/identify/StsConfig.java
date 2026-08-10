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

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * Sts config.
 * <p>STS（Security Token Service）临时凭证配置单例：支持通过 ECS 元数据 URL、静态 JSON 或客户端属性注入 RAM 角色临时 AK/SK/Token，并控制刷新与缓存策略。</p>
 *
 * @author Nacos
 */
public class StsConfig {
    
    /** 阿里云 ECS 元数据默认 STS 凭证 URL 前缀（需拼接 RAM 角色名） */
    private static final String RAM_SECURITY_CREDENTIALS_URL =
        "http://100.100.100.200/latest/meta-data/ram/security-credentials/";
    
    /** 绑定的 RAM 角色名称，用于构造元数据拉取地址 */
    private String ramRoleName;
    
    /**
     * The STS temporary certificate will be refreshed when the validity period of
     * the temporary certificate is left (allow the local time to be at most slower than the STS service time).
     * <p>临时凭证剩余有效期低于该阈值（毫秒）时触发刷新，默认 3 分钟；允许本地时钟略慢于 STS 服务。</p>
     */
    private int timeToRefreshInMillisecond = 3 * 60 * 1000;
    
    /**
     * Metadata interface for obtaining STS temporary credentials (including role name).
     * <p>拉取 STS 临时凭证的 HTTP 元数据地址；未显式设置且已配置角色名时自动拼接默认 ECS URL。</p>
     */
    private String securityCredentialsUrl;
    
    /**
     * Set the STS temporary certificate and no longer obtain it through the metadata interface.
     * <p>静态 STS 响应 JSON 字符串；设置后不再访问元数据接口，适用于离线或测试场景。</p>
     */
    private String securityCredentials;
    
    /**
     * Whether to cache.
     * <p>是否在 {@link StsCredentialHolder} 中缓存已获取的临时凭证。</p>
     */
    private boolean cacheSecurityCredentials = true;
    
    /** 延迟加载单例持有者 */
    private static class Singleton {
        
        private static final StsConfig INSTANCE = new StsConfig();
    }
    
    /** 私有构造：从 {@link com.alibaba.nacos.client.env.NacosClientProperties} 原型读取 STS 相关属性 */
    private StsConfig() {
        String ramRoleName = NacosClientProperties.PROTOTYPE
            .getProperty(IdentifyConstants.RAM_ROLE_NAME_PROPERTY);
        if (!StringUtils.isBlank(ramRoleName)) {
            setRamRoleName(ramRoleName);
        }
        
        String timeToRefreshInMillisecond = NacosClientProperties.PROTOTYPE
            .getProperty(IdentifyConstants.REFRESH_TIME_PROPERTY);
        if (!StringUtils.isBlank(timeToRefreshInMillisecond)) {
            setTimeToRefreshInMillisecond(Integer.parseInt(timeToRefreshInMillisecond));
        }
        
        String securityCredentials =
            NacosClientProperties.PROTOTYPE.getProperty(IdentifyConstants.SECURITY_PROPERTY);
        if (!StringUtils.isBlank(securityCredentials)) {
            setSecurityCredentials(securityCredentials);
        }
        
        String securityCredentialsUrl = NacosClientProperties.PROTOTYPE
            .getProperty(IdentifyConstants.SECURITY_URL_PROPERTY);
        if (!StringUtils.isBlank(securityCredentialsUrl)) {
            setSecurityCredentialsUrl(securityCredentialsUrl);
        }
        
        String cacheSecurityCredentials = NacosClientProperties.PROTOTYPE
            .getProperty(IdentifyConstants.SECURITY_CACHE_PROPERTY);
        if (!StringUtils.isBlank(cacheSecurityCredentials)) {
            setCacheSecurityCredentials(Boolean.parseBoolean(cacheSecurityCredentials));
        }
    }
    
    /** 返回全局唯一 {@link StsConfig} 实例 */
    public static StsConfig getInstance() {
        return Singleton.INSTANCE;
    }
    
    /** 获取 RAM 角色名 */
    public String getRamRoleName() {
        return ramRoleName;
    }
    
    /** 设置 RAM 角色名 */
    public void setRamRoleName(String ramRoleName) {
        this.ramRoleName = ramRoleName;
    }
    
    /** 获取凭证刷新阈值（毫秒） */
    public int getTimeToRefreshInMillisecond() {
        return timeToRefreshInMillisecond;
    }
    
    /** 设置凭证刷新阈值（毫秒） */
    public void setTimeToRefreshInMillisecond(int timeToRefreshInMillisecond) {
        this.timeToRefreshInMillisecond = timeToRefreshInMillisecond;
    }
    
    /** 获取 STS 元数据 URL；若未设置且存在角色名则拼接默认 ECS 地址 */
    public String getSecurityCredentialsUrl() {
        if (securityCredentialsUrl == null && ramRoleName != null) {
            return RAM_SECURITY_CREDENTIALS_URL + ramRoleName;
        }
        return securityCredentialsUrl;
    }
    
    /** 设置 STS 元数据 URL */
    public void setSecurityCredentialsUrl(String securityCredentialsUrl) {
        this.securityCredentialsUrl = securityCredentialsUrl;
    }
    
    /** 获取静态 STS JSON 凭证内容 */
    public String getSecurityCredentials() {
        return securityCredentials;
    }
    
    /** 设置静态 STS JSON 凭证内容 */
    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }
    
    /** 判断是否启用 STS：静态凭证或元数据 URL 任一非空即为开启 */
    public boolean isStsOn() {
        return StringUtils.isNotEmpty(getSecurityCredentials())
            || StringUtils.isNotEmpty(getSecurityCredentialsUrl());
    }
    
    /** 是否缓存 STS 凭证 */
    public boolean isCacheSecurityCredentials() {
        return cacheSecurityCredentials;
    }
    
    /** 设置是否缓存 STS 凭证 */
    public void setCacheSecurityCredentials(boolean cacheSecurityCredentials) {
        this.cacheSecurityCredentials = cacheSecurityCredentials;
    }
}
