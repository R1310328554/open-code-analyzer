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

package com.alibaba.nacos.maintainer.client.naming;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;

/**
 * 命名维护服务工厂：创建 {@link NamingMaintainerService} 实例。
 *
 * <p>支持服务端地址字符串或完整 {@link Properties} 两种入口。</p>
 *
 * @author Nacos
 */
public class NamingMaintainerFactory {
    
    /**
     * create Naming maintainer service.
     *
     * @param serverList server list
     * @return naming maintainer service
     * @throws NacosException nacos exception
      * <p>Nacos 模块组件；详见上方说明。</p>
     */
    /** 根据 serverAddr 列表字符串创建命名维护服务。 */
    public static NamingMaintainerService createNamingMaintainerService(String serverList)
        throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverList);
        return new NacosNamingMaintainerServiceImpl(properties);
    }
    
    /**
     * create Naming maintainer service.
     *
     * @param properties properties
     * @return naming maintainer service
     * @throws NacosException nacos exception
      * <p>Nacos 模块组件；详见上方说明。</p>
     */
    /** 根据客户端 {@link Properties} 创建命名维护服务。 */
    public static NamingMaintainerService createNamingMaintainerService(Properties properties)
        throws NacosException {
        if (properties == null) {
            throw new NacosException(NacosException.INVALID_PARAM, "properties is null");
        }
        return new NacosNamingMaintainerServiceImpl(properties);
    }
}
