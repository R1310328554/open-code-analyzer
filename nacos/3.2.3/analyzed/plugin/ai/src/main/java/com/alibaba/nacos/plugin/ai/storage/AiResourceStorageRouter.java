/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.storage;

import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.storage.model.StorageKey;
import com.alibaba.nacos.plugin.ai.storage.spi.AiResourceStorage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link AiResourceStorage} 的路由门面（Facade）。
 *
 * <p>上层模块（Skill、Prompt 等）应仅依赖本路由器：构造含 provider 与不透明 key 的
 * {@link StorageKey}，再将读写委托给路由器即可。</p>
 *
 * <p>存储实现由外部初始化器（例如 ai 模块中的 {@code AiResourceStorageInitializer}）
 * 在根应用上下文刷新完成后，通过 {@link #join(AiResourceStorage)} 注册。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class AiResourceStorageRouter {
    
    private static final AiResourceStorageRouter INSTANCE = new AiResourceStorageRouter();
    
    /** 按 provider 类型索引的存储实现注册表。 */
    private static final Map<String, AiResourceStorage> STORAGES_BY_TYPE =
        new ConcurrentHashMap<>(8);
    
    private AiResourceStorageRouter() {
        // 存储实现由外部初始化器在上下文就绪后通过 join() 注册
    }
    
    /**
     * 获取全局单例路由器。
     *
     * @return 路由器实例
     */
    public static AiResourceStorageRouter getInstance() {
        return INSTANCE;
    }
    
    /**
     * 根据 {@link StorageKey#getProvider()} 路由到对应存储实现。
     *
     * @param storageKey 存储键
     * @return 匹配的存储实现
     */
    public AiResourceStorage route(StorageKey storageKey) {
        if (storageKey == null || StringUtils.isBlank(storageKey.getProvider())) {
            throw new IllegalArgumentException("StorageKey.provider is blank");
        }
        AiResourceStorage storage = STORAGES_BY_TYPE.get(storageKey.getProvider());
        if (storage == null) {
            throw new IllegalStateException(
                "No AiResourceStorage for provider: " + storageKey.getProvider());
        }
        return storage;
    }
    
    /** @return 所有已注册存储实现的只读视图 */
    public Map<String, AiResourceStorage> allStorages() {
        return Collections.unmodifiableMap(STORAGES_BY_TYPE);
    }
    
    /**
     * 运行时注册或覆盖存储实现。
     *
     * <p>主要用于测试或嵌入式场景；若同类型已存在则会被覆盖。</p>
     *
     * @param storage 存储实现
     * @return 注册成功返回 {@code true}
     */
    public static synchronized boolean join(AiResourceStorage storage) {
        if (storage == null || StringUtils.isBlank(storage.type())) {
            return false;
        }
        STORAGES_BY_TYPE.put(storage.type(), storage);
        return true;
    }
    
    /** 清空注册表，仅供单元测试使用。 */
    @JustForTest
    public static synchronized void reset() {
        STORAGES_BY_TYPE.clear();
    }
}
