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

package com.alibaba.nacos.client.naming.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosLoadException;
import com.alibaba.nacos.client.address.AbstractServerListManager;
import com.alibaba.nacos.client.address.PropertiesListProvider;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名模块服务端地址列表管理器。
 *
 * <p>继承 {@link AbstractServerListManager}，负责命名服务端列表的加载、轮询选取下一节点，并识别单域名配置模式。</p>
 *
 * @author xiweng.yy
 */
public class NamingServerListManager extends AbstractServerListManager {
    
    private static final Logger LOGGER = LogUtils.logger(NamingServerListManager.class);
    
    /** 当前选中服务端的轮询索引。 */
    private final AtomicInteger currentIndex = new AtomicInteger();
    
    /** 单域名模式下的 Nacos 域名地址。 */
    private String nacosDomain;
    
    /** 是否配置为单域名模式（仅一个 server 地址）。 */
    private boolean isDomain;
    
    /** 测试用构造器，从 Properties 派生客户端配置。 */
    @JustForTest
    public NamingServerListManager(Properties properties) {
        this(NacosClientProperties.PROTOTYPE.derive(properties), "");
    }
    
    public NamingServerListManager(NacosClientProperties properties, String namespace) {
        super(properties, namespace);
    }
    
    /** 启动时校验列表非空并随机初始化轮询起点，识别域名模式。 */
    @Override
    public void start() throws NacosException {
        super.start();
        List<String> serverList = getServerList();
        if (serverList.isEmpty()) {
            throw new NacosLoadException("serverList is empty,please check configuration");
        } else {
            currentIndex.set(ThreadLocalRandom.current().nextInt(serverList.size()));
        }
        if (serverListProvider instanceof PropertiesListProvider) {
            if (serverList.size() == 1) {
                isDomain = true;
                nacosDomain = serverList.get(0);
            }
        }
    }
    
    /** 返回单域名模式下的域名地址。 */
    public String getNacosDomain() {
        return nacosDomain;
    }
    
    /** 是否为单域名配置模式。 */
    public boolean isDomain() {
        return isDomain;
    }
    
    /** 模块名称标识，用于地址解析 SPI。 */
    @Override
    protected String getModuleName() {
        return "Naming";
    }
    
    /** 获取命名模块专用 HTTP 客户端。 */
    @Override
    protected NacosRestTemplate getNacosRestTemplate() {
        return NamingHttpClientManager.getInstance().getNacosRestTemplate();
    }
    
    /** 轮询返回下一个服务端地址。 */
    @Override
    public String genNextServer() {
        int index = currentIndex.incrementAndGet() % getServerList().size();
        return getServerList().get(index);
    }
    
    /** 返回当前索引对应的服务端地址。 */
    @Override
    public String getCurrentServer() {
        return getServerList().get(currentIndex.get() % getServerList().size());
    }
}
