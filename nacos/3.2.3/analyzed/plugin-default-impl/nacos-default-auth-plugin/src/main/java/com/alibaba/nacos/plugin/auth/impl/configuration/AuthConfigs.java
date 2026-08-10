/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.configuration;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.auth.config.AuthErrorCode;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Nacos 默认鉴权插件运行时配置中心。
 *
 * <p>绑定 {@code nacos.core.auth.*} 与插件前缀 {@code nacos.core.auth.plugin.*}， 订阅 {@link ServerConfigChangeEvent} 热更新；启动时 {@link #validate()} 校验鉴权类型与集群身份凭证。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
@Configuration
public class AuthConfigs extends Subscriber<ServerConfigChangeEvent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthConfigs.class);
    
    /** 鉴权插件扩展属性配置前缀。 */
    private static final String PREFIX = "nacos.core.auth.plugin";
    
    @JustForTest
    private static Boolean cachingEnabled = null;
    
    /** 是否开启 Open API / 服务端鉴权。 */
    @Value("${" + Constants.Auth.NACOS_CORE_AUTH_ENABLED + ":false}")
    private boolean authEnabled;
    
    /** 是否开启控制台登录与权限校验。 */
    @Value("${" + Constants.Auth.NACOS_CORE_AUTH_CONSOLE_ENABLED + ":true}")
    private boolean consoleAuthEnabled;
    
    /** 当前启用的鉴权体系类型（nacos、ldap、oidc 等）。 */
    @Value("${" + Constants.Auth.NACOS_CORE_AUTH_SYSTEM_TYPE + ":}")
    private String nacosAuthSystemType;
    
    @Value("${" + Constants.Auth.NACOS_CORE_AUTH_SERVER_IDENTITY_KEY + ":}")
    private String serverIdentityKey;
    
    @Value("${" + Constants.Auth.NACOS_CORE_AUTH_SERVER_IDENTITY_VALUE + ":}")
    private String serverIdentityValue;
    
    /** 是否允许 AI 公共资源匿名访问。 */
    @Value("${" + AuthConstants.NACOS_CORE_AUTH_NACOS_ANONYMOUS_AI_ENABLED + ":false}")
    private boolean aiAnonymousEnabled;
    
    private boolean hasGlobalAdminRole;
    
    /** 按鉴权类型分组的插件扩展属性缓存。 */
    private volatile Map<String, Properties> authPluginProperties = new HashMap<>();
    
    /** 注册配置变更订阅并加载插件属性。 */
    public AuthConfigs() {
        NotifyCenter.registerSubscriber(this);
        refreshPluginProperties();
    }
    
    /**
     * 启动时校验鉴权配置合法性。
     *
     * @throws NacosException If the config is not valid.
     */
    @PostConstruct
    public void validate() throws NacosException {
        if (!authEnabled && !consoleAuthEnabled) {
            return;
        }
        if (StringUtils.isEmpty(nacosAuthSystemType)) {
            throw new NacosException(AuthErrorCode.INVALID_TYPE.getCode(),
                AuthErrorCode.INVALID_TYPE.getMsg());
        }
        if (EnvUtil.getStandaloneMode()) {
            return;
        }
        if (StringUtils.isEmpty(serverIdentityKey) || StringUtils.isEmpty(serverIdentityValue)) {
            throw new NacosException(AuthErrorCode.EMPTY_IDENTITY.getCode(),
                AuthErrorCode.EMPTY_IDENTITY.getMsg());
        }
    }
    
    /** 从环境变量刷新各鉴权类型的插件属性映射。 */
    private void refreshPluginProperties() {
        try {
            Map<String, Properties> newProperties = new HashMap<>(1);
            Properties properties =
                PropertiesUtil.getPropertiesWithPrefix(EnvUtil.getEnvironment(), PREFIX);
            if (properties != null) {
                for (String each : properties.stringPropertyNames()) {
                    int typeIndex = each.indexOf('.');
                    String type = each.substring(0, typeIndex);
                    String subKey = each.substring(typeIndex + 1);
                    newProperties.computeIfAbsent(type, key -> new Properties())
                        .setProperty(subKey, properties.getProperty(each));
                }
            }
            authPluginProperties = newProperties;
        } catch (Exception e) {
            LOGGER.warn("Refresh plugin properties failed ", e);
        }
    }
    
    public boolean isHasGlobalAdminRole() {
        return hasGlobalAdminRole;
    }
    
    public void setHasGlobalAdminRole(boolean hasGlobalAdminRole) {
        this.hasGlobalAdminRole = hasGlobalAdminRole;
    }
    
    public String getNacosAuthSystemType() {
        return nacosAuthSystemType;
    }
    
    public String getServerIdentityKey() {
        return serverIdentityKey;
    }
    
    public String getServerIdentityValue() {
        return serverIdentityValue;
    }
    
    /**
     * 控制台鉴权是否已开启。
     *
     * @return console auth function is open
     */
    public boolean isConsoleAuthEnabled() {
        return consoleAuthEnabled;
    }
    
    /**
     * 服务端 Open API 鉴权是否已开启。
     *
     * @return server auth function is open
     */
    public boolean isAuthEnabled() {
        return authEnabled;
    }
    
    /**
     * AI 资源匿名访问是否已开启。
     *
     * @return AI anonymous access is open
     */
    public boolean isAiAnonymousEnabled() {
        return aiAnonymousEnabled;
    }
    
    /**
     * 权限信息是否允许缓存。
     *
     * @return bool
     */
    public boolean isCachingEnabled() {
        if (Objects.nonNull(AuthConfigs.cachingEnabled)) {
            return cachingEnabled;
        }
        return ConvertUtils
            .toBoolean(EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_CACHING_ENABLED, "true"));
    }
    
    /** 按鉴权类型获取插件扩展属性，缺失时返回空 Properties。 */
    public Properties getAuthPluginProperties(String authType) {
        Properties properties = authPluginProperties.get(authType);
        if (properties == null) {
            LOGGER.warn("Can't find properties for type {}, will use empty properties", authType);
            return new Properties();
        }
        return properties;
    }
    
    @JustForTest
    public static void setCachingEnabled(boolean cachingEnabled) {
        AuthConfigs.cachingEnabled = cachingEnabled;
    }
    
    /** 响应服务端配置变更，热更新鉴权开关与插件属性。 */
    @Override
    public void onEvent(ServerConfigChangeEvent event) {
        try {
            authEnabled =
                EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_ENABLED, Boolean.class, false);
            consoleAuthEnabled = EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_CONSOLE_ENABLED,
                Boolean.class, true);
            cachingEnabled = EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_CACHING_ENABLED,
                Boolean.class, true);
            serverIdentityKey =
                EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_SERVER_IDENTITY_KEY, "");
            serverIdentityValue =
                EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_SERVER_IDENTITY_VALUE, "");
            nacosAuthSystemType =
                EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_SYSTEM_TYPE, "");
            aiAnonymousEnabled =
                EnvUtil.getProperty(AuthConstants.NACOS_CORE_AUTH_NACOS_ANONYMOUS_AI_ENABLED,
                    Boolean.class, false);
            refreshPluginProperties();
        } catch (Exception e) {
            LOGGER.warn("Upgrade auth config from env failed, use old value", e);
        }
    }
    
    /** 订阅 {@link ServerConfigChangeEvent} 类型。 */
    @Override
    public Class<? extends Event> subscribeType() {
        return ServerConfigChangeEvent.class;
    }
}
