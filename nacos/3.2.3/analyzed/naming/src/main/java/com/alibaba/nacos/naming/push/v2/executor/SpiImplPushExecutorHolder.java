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

package com.alibaba.nacos.naming.push.v2.executor;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.naming.pojo.Subscriber;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * SPI 推送执行器持有者（单例）。
 *
 * <p>启动时通过 {@link NacosServiceLoader} 加载全部 {@link SpiPushExecutor} 扩展，按 {@link #findPushExecutorSpiImpl} 匹配感兴趣的实现。</p>
 *
 * @author xiweng.yy
 */
public class SpiImplPushExecutorHolder {
    
    private static final SpiImplPushExecutorHolder INSTANCE = new SpiImplPushExecutorHolder();
    
    private final Set<SpiPushExecutor> pushExecutors;
    
    private SpiImplPushExecutorHolder() {
        pushExecutors = new HashSet<>(NacosServiceLoader.load(SpiPushExecutor.class));
    }
    
    /** 返回 SPI 持有者单例。 */
    public static SpiImplPushExecutorHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * 查找对该次推送感兴趣的 SPI 推送执行器。
     *
     * @param clientId   客户端 ID
     * @param subscriber 订阅者信息
     * @return 匹配的 {@link PushExecutor}，否则 {@code Optional.empty()}
     */
    public Optional<SpiPushExecutor> findPushExecutorSpiImpl(String clientId,
        Subscriber subscriber) {
        for (SpiPushExecutor each : pushExecutors) {
            if (each.isInterest(clientId, subscriber)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
