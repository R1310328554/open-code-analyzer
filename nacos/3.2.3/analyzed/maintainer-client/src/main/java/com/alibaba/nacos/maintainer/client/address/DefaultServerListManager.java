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

package com.alibaba.nacos.maintainer.client.address;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosLoadException;
import com.alibaba.nacos.client.address.AbstractServerListManager;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.maintainer.client.remote.HttpClientManager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 维护客户端默认服务端列表管理器：轮询选取 Nacos 节点。
 *
 * <p>继承 {@link AbstractServerListManager}，启动时随机初始化游标；HTTP 调用通过 {@link HttpClientManager} 获取 {@link NacosRestTemplate}。</p>
 *
 * @author Nacos
 */
public class DefaultServerListManager extends AbstractServerListManager {
    
    /** 当前服务端列表轮询下标。 */
    private final AtomicInteger currentIndex = new AtomicInteger();
    
    /** 使用客户端属性初始化服务端列表管理器。 */
    public DefaultServerListManager(NacosClientProperties properties) {
        super(properties);
    }
    
    /** 启动并校验服务端列表非空，随机设置初始下标。 */
    @Override
    public void start() throws NacosException {
        super.start();
        List<String> serverList = getServerList();
        if (serverList.isEmpty()) {
            throw new NacosLoadException("serverList is empty,please check configuration");
        } else {
            currentIndex.set(ThreadLocalRandom.current().nextInt(serverList.size()));
        }
    }
    
    /** 模块标识，供地址解析使用。 */
    @Override
    protected String getModuleName() {
        return "Naming";
    }
    
    /** 获取维护客户端共享 HTTP 模板。 */
    @Override
    protected NacosRestTemplate getNacosRestTemplate() {
        return HttpClientManager.getInstance().getNacosRestTemplate();
    }
    
    /** 递增下标并返回下一个服务端地址（轮询）。 */
    @Override
    public String genNextServer() {
        int index = currentIndex.incrementAndGet() % getServerList().size();
        return getServerList().get(index);
    }
    
    /** 返回当前下标对应的服务端地址。 */
    @Override
    public String getCurrentServer() {
        return getServerList().get(currentIndex.get() % getServerList().size());
    }
}
