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

package com.alibaba.nacos.api;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.NacosLockFactory;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Properties;

/**
 * Nacos 客户端工厂类，统一创建配置、命名与锁等服务实例。
 *
 * @author Nacos
 */
public class NacosFactory {
    
    /**
     * 根据 Properties 创建配置服务 {@link ConfigService}。
     *
     * @param properties init param
     * @return config
     * @throws NacosException Exception
     */
    public static ConfigService createConfigService(Properties properties) throws NacosException {
        return ConfigFactory.createConfigService(properties);
    }
    
    /**
     * Create config service.
     *
     * @param serverAddr server list
     * @return config
     * @throws NacosException Exception
      * <p>Nacos AI Registry/API 模块；详见上方英文说明。</p>
     */
    public static ConfigService createConfigService(String serverAddr) throws NacosException {
        return ConfigFactory.createConfigService(serverAddr);
    }
    
    /**
     * 根据服务器地址创建命名服务 {@link NamingService}。
     *
     * @param serverAddr server list
     * @return Naming
     * @throws NacosException Exception
     */
    public static NamingService createNamingService(String serverAddr) throws NacosException {
        return NamingFactory.createNamingService(serverAddr);
    }
    
    /**
     * Create naming service.
     *
     * @param properties init param
     * @return Naming
     * @throws NacosException Exception
      * <p>Nacos AI Registry/API 模块；详见上方英文说明。</p>
     */
    public static NamingService createNamingService(Properties properties) throws NacosException {
        return NamingFactory.createNamingService(properties);
    }
    
    /**
     * 创建命名维护服务（已废弃，请改用 nacos-maintainer-client）。
     *
     * @param serverAddr server address
     * @return NamingMaintainService
     * @throws NacosException Exception
     * @deprecated use {@link com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory} in nacos-maintainer-client artifact tp replaced.
     */
    @Deprecated
    public static NamingMaintainService createMaintainService(String serverAddr)
        throws NacosException {
        return NamingMaintainFactory.createMaintainService(serverAddr);
    }
    
    /**
     * Create maintain service.
     *
     * @param properties server address
     * @return NamingMaintainService
     * @throws NacosException Exception
     * @deprecated use {@link com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory} in nacos-maintainer-client artifact tp replaced.
      * <p>Nacos AI Registry/API 模块；详见上方英文说明。</p>
     */
    @Deprecated
    public static NamingMaintainService createMaintainService(Properties properties)
        throws NacosException {
        return NamingMaintainFactory.createMaintainService(properties);
    }
    
    /**
     * 根据 Properties 创建分布式锁服务 {@link LockService}。
     *
     * @param properties init param
     * @return lock service
     * @throws NacosException Exception
     */
    public static LockService createLockService(Properties properties) throws NacosException {
        return NacosLockFactory.createLockService(properties);
    }
}
