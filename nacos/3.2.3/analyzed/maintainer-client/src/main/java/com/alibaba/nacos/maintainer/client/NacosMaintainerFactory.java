/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.maintainer.client;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerFactory;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerService;

import java.util.Properties;

/**
 * Nacos 维护客户端工厂：提供配置维护服务的便捷创建入口。
 *
 * <p>内部委托 {@link ConfigMaintainerFactory} 完成实例化。</p>
 *
 * @author Nacos
 */
public class NacosMaintainerFactory {
    
    /** 根据服务端地址列表创建配置维护服务。 */
    public static ConfigMaintainerService createConfigMaintainerService(String serverList)
        throws NacosException {
        return ConfigMaintainerFactory.createConfigMaintainerService(serverList);
    }
    
    /** 根据客户端 {@link Properties} 创建配置维护服务。 */
    public static ConfigMaintainerService createConfigMaintainerService(Properties properties)
        throws NacosException {
        return ConfigMaintainerFactory.createConfigMaintainerService(properties);
    }
}
