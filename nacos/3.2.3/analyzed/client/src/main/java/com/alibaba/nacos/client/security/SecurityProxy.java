/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.security;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.address.AbstractServerListManager;
import com.alibaba.nacos.client.address.ServerListChangeEvent;
import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.plugin.auth.spi.client.ClientAuthPluginManager;
import com.alibaba.nacos.plugin.auth.spi.client.ClientAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 客户端安全代理。
 *
 * <p>管理 {@link ClientAuthPluginManager} 与各 {@link ClientAuthService} SPI 实现，负责登录、身份上下文注入及服务端列表变更时的插件刷新。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class SecurityProxy implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityProxy.class);
    
    /** 客户端鉴权插件管理器。 */
    private ClientAuthPluginManager clientAuthPluginManager;
    
    /**
     * 初始化鉴权插件并订阅服务端列表变更事件。
     *
     * @param serverListManager 客户端请求的服务端列表管理器
     * @Param nacosRestTemplate HTTP 请求模板
     */
    public SecurityProxy(AbstractServerListManager serverListManager,
        NacosRestTemplate nacosRestTemplate) {
        clientAuthPluginManager = new ClientAuthPluginManager();
        clientAuthPluginManager.init(serverListManager.getServerList(), nacosRestTemplate);
        NotifyCenter.registerSubscriber(new Subscriber<ServerListChangeEvent>() {
            
            @Override
            public void onEvent(ServerListChangeEvent event) {
                clientAuthPluginManager.refreshServerList(serverListManager.getServerList());
            }
            
            @Override
            public Class<? extends Event> subscribeType() {
                return ServerListChangeEvent.class;
            }
        });
    }
    
    /**
     * 对所有已加载的 {@link ClientAuthService} 执行登录。
     *
     * @param properties 登录身份信息（用户名、密码等）
     */
    public void login(Properties properties) {
        if (clientAuthPluginManager.getAuthServiceSpiImplSet().isEmpty()) {
            return;
        }
        for (ClientAuthService clientAuthService : clientAuthPluginManager
            .getAuthServiceSpiImplSet()) {
            clientAuthService.login(properties);
        }
    }
    
    /**
     * 合并各鉴权插件的登录身份上下文，供 HTTP/gRPC 请求头注入。
     *
     * @param resource 请求资源描述
     * @return 合并后的身份头键值对
     */
    public Map<String, String> getIdentityContext(RequestResource resource) {
        Map<String, String> header = new HashMap<>(1);
        for (ClientAuthService clientAuthService : clientAuthPluginManager
            .getAuthServiceSpiImplSet()) {
            LoginIdentityContext loginIdentityContext =
                clientAuthService.getLoginIdentityContext(resource);
            for (String key : loginIdentityContext.getAllKey()) {
                header.put(key, loginIdentityContext.getParameter(key));
            }
        }
        return header;
    }
    
    /** 关闭所有鉴权插件。 */
    @Override
    public void shutdown() throws NacosException {
        clientAuthPluginManager.shutdown();
    }
    
    /** 设置 reLogin 标志，触发各插件在下次请求前刷新 accessToken。 */
    public void reLogin() {
        if (clientAuthPluginManager.getAuthServiceSpiImplSet().isEmpty()) {
            return;
        }
        for (ClientAuthService clientAuthService : clientAuthPluginManager
            .getAuthServiceSpiImplSet()) {
            try {
                LoginIdentityContext loginIdentityContext =
                    clientAuthService.getLoginIdentityContext(new RequestResource());
                if (loginIdentityContext != null) {
                    loginIdentityContext.setParameter(NacosAuthLoginConstant.RELOGINFLAG, "true");
                }
            } catch (Exception e) {
                LOGGER.error("[SecurityProxy] set reLoginFlag failed.", e);
            }
        }
    }
}
