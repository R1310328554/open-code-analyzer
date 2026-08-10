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

package com.alibaba.nacos.plugin.auth.spi.client;

import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.plugin.auth.api.RequestResource;

import java.util.List;
import java.util.Properties;

/**
 * 客户端认证服务 SPI 接口，定义登录与身份上下文获取能力。
 *
 * <p>各认证插件（如 Nacos 内置、OIDC 等）实现此接口，由
 * {@link ClientAuthPluginManager} 加载并在 SDK 请求发出前注入凭证。</p>
 *
 * @author wuyfee
 */
public interface ClientAuthService extends Closeable {
    
    /**
     * 向 Nacos 服务端发起登录请求并获取凭证。
     *
     * @param properties 登录所需的认证信息（用户名、密码、clientId 等）
     * @return 登录是否成功
     */
    Boolean login(Properties properties);
    
    /**
     * 设置 Nacos 服务端地址列表。
     *
     * @param serverList 服务端地址列表
     */
    void setServerList(List<String> serverList);
    
    /**
     * 注入 HTTP 请求模板，供登录与令牌刷新使用。
     *
     * @param nacosRestTemplate Nacos HTTP 请求模板
     */
    void setNacosRestTemplate(NacosRestTemplate nacosRestTemplate);
    
    /**
     * 获取当前登录身份上下文，用于在后续请求中携带认证信息。
     *
     * @param resource 本次请求涉及的资源；部分插件实现会据此生成差异化凭证，无需时可忽略
     * @return 本插件维护的登录身份上下文
     */
    LoginIdentityContext getLoginIdentityContext(RequestResource resource);
    
}
