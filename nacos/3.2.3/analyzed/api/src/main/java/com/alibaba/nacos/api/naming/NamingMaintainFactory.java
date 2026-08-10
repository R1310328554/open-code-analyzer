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
 * {@link NamingMaintainService} 工厂类（已废弃）。
 *
 * <p>通过反射加载 {@code NacosNamingMaintainService} 实现；3.3.0 起请改用 nacos-maintainer-client 中的 {@link com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory}。</p>
 *
 * @author liaochuntao
 * @since 1.0.1
 * @deprecated use {@link com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory} in nacos-maintainer-client artifact tp replaced.
 */
@Deprecated
public class NamingMaintainFactory {
    
    /**
     * 根据服务器地址列表创建运维维护服务。
     *
     * @param serverList Nacos 服务器地址列表
     * @return 新的 {@link NamingMaintainService}
     * @throws NacosException 实例化失败时抛出
     */
    public static NamingMaintainService createMaintainService(String serverList)
        throws NacosException {
        try {
            Class<?> driverImplClass =
                Class.forName("com.alibaba.nacos.client.naming.NacosNamingMaintainService");
            Constructor constructor = driverImplClass.getConstructor(String.class);
            return (NamingMaintainService) constructor.newInstance(serverList);
        } catch (Throwable e) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
        }
    }
    
    /**
     * 根据配置属性创建运维维护服务。
     *
     * @param properties 客户端配置
     * @return 新的 {@link NamingMaintainService}
     * @throws NacosException 实例化失败时抛出
     */
    public static NamingMaintainService createMaintainService(Properties properties)
        throws NacosException {
        try {
            Class<?> driverImplClass =
                Class.forName("com.alibaba.nacos.client.naming.NacosNamingMaintainService");
            Constructor constructor = driverImplClass.getConstructor(Properties.class);
            return (NamingMaintainService) constructor.newInstance(properties);
        } catch (Throwable e) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
        }
    }
    
}
