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

package com.alibaba.nacos.api.naming;

import com.alibaba.nacos.api.exception.NacosException;

import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * {@link NamingService} 工厂类。
 *
 * <p>通过反射加载客户端实现 {@code com.alibaba.nacos.client.naming.NacosNamingService}，避免 API 模块直接依赖 client 包。</p>
 *
 * @author nkorange
 */
public class NamingFactory {
    
    /**
     * 根据服务器地址列表创建命名服务客户端。
     *
     * @param serverList 逗号分隔的 Nacos 服务器地址
     * @return 新的 {@link NamingService} 实例
     * @throws NacosException 反射实例化失败时抛出
     */
    public static NamingService createNamingService(String serverList) throws NacosException {
        try {
            Class<?> driverImplClass =
                Class.forName("com.alibaba.nacos.client.naming.NacosNamingService");
            Constructor constructor = driverImplClass.getConstructor(String.class);
            return (NamingService) constructor.newInstance(serverList);
        } catch (Throwable e) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
        }
    }
    
    /**
     * 根据 {@link Properties} 配置创建命名服务客户端。
     *
     * @param properties 命名服务配置（namespace、serverAddr 等）
     * @return 新的 {@link NamingService} 实例
     * @throws NacosException 反射实例化失败时抛出
     */
    public static NamingService createNamingService(Properties properties) throws NacosException {
        try {
            Class<?> driverImplClass =
                Class.forName("com.alibaba.nacos.client.naming.NacosNamingService");
            Constructor constructor = driverImplClass.getConstructor(Properties.class);
            return (NamingService) constructor.newInstance(properties);
        } catch (Throwable e) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
        }
    }
}
