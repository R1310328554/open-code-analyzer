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

import com.alibaba.nacos.common.http.client.NacosRestTemplate;

import java.util.List;

/**
 * 客户端认证服务 SPI 抽象基类，提供公共依赖的默认实现。
 *
 * <p>子类通过 {@link java.util.ServiceLoader} 机制注册，由
 * {@link ClientAuthPluginManager} 统一加载与管理。</p>
 *
 * @author Nacos
 */
public abstract class AbstractClientAuthService implements ClientAuthService {
    
    /**
     * Nacos 服务端地址列表，登录与令牌刷新时使用。
     */
    protected volatile List<String> serverList;
    
    /**
     * HTTP 请求模板，用于与 Nacos 服务端通信。
     */
    protected NacosRestTemplate nacosRestTemplate;
    
    @Override
    public void setServerList(List<String> serverList) {
        this.serverList = serverList;
    }
    
    @Override
    public void setNacosRestTemplate(NacosRestTemplate nacosRestTemplate) {
        this.nacosRestTemplate = nacosRestTemplate;
    }
}
