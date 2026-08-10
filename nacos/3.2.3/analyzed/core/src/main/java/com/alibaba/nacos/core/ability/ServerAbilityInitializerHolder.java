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

package com.alibaba.nacos.core.ability;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.core.utils.Loggers;

import java.util.Collection;
import java.util.HashSet;

/**
 * 服务端能力初始化器持有者：通过 SPI 加载全部 {@link ServerAbilityInitializer} 实现。
 * Nacos server ability initializer holder.
 *
 * @author xiweng.yy
 */
public class ServerAbilityInitializerHolder {
    
    /** 单例实例。 */
    private static final ServerAbilityInitializerHolder INSTANCE =
        new ServerAbilityInitializerHolder();
    
    /** 已加载的初始化器集合。 */
    private final Collection<ServerAbilityInitializer> initializers;
    
    /** 私有构造：通过 {@link NacosServiceLoader} 发现并注册初始化器。 */
    private ServerAbilityInitializerHolder() {
        initializers = new HashSet<>();
        for (ServerAbilityInitializer each : NacosServiceLoader
            .load(ServerAbilityInitializer.class)) {
            Loggers.CORE.info("Load {} for ServerAbilityInitializer",
                each.getClass().getCanonicalName());
            initializers.add(each);
        }
    }
    
    /** 获取持有者单例。 */
    public static ServerAbilityInitializerHolder getInstance() {
        return INSTANCE;
    }
    
    /** 返回全部已加载的服务端能力初始化器。 */
    public Collection<ServerAbilityInitializer> getInitializers() {
        return initializers;
    }
}
