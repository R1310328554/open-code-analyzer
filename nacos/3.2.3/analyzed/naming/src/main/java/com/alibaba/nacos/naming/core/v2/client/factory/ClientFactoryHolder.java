/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.client.factory;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.naming.misc.Loggers;

import java.util.Collection;
import java.util.HashMap;

/**
 * 客户端工厂持有者，通过 SPI 加载并缓存各类 {@link ClientFactory}。
 *
 * <p>按连接类型查找对应工厂，未匹配时回退到默认工厂。</p>
 *
 * @author xiweng.yy
 */
public class ClientFactoryHolder {
    
    /** 单例实例。 */
    private static final ClientFactoryHolder INSTANCE = new ClientFactoryHolder();
    
    /** 连接类型 → 工厂实现映射表。 */
    private final HashMap<String, ClientFactory> clientFactories;
    
    private ClientFactoryHolder() {
        clientFactories = new HashMap<>(4);
        Collection<ClientFactory> clientFactories = NacosServiceLoader.load(ClientFactory.class);
        for (ClientFactory each : clientFactories) {
            if (this.clientFactories.containsKey(each.getType())) {
                Loggers.SRV_LOG.warn("Client type {} found multiple factory, use {} default",
                    each.getType(),
                    each.getClass().getCanonicalName());
            }
            this.clientFactories.put(each.getType(), each);
        }
    }
    
    public static ClientFactoryHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * 按类型查找 {@link ClientFactory}；未找到或类型为空时返回默认工厂。
     *
     * @param type target type
     * @return target type {@link ClientFactory}, if not fount, return 'default' client factory.
     */
    public ClientFactory findClientFactory(String type) {
        if (StringUtils.isEmpty(type) || !clientFactories.containsKey(type)) {
            return clientFactories.get(ClientConstants.DEFAULT_FACTORY);
        }
        return clientFactories.get(type);
    }
}
