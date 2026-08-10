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

package com.alibaba.nacos.plugin.ai.storage.spi;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.plugin.ai.storage.model.StorageKey;

/**
 * AI 资源存储 SPI 接口。
 *
 * <p>类似 Nacos 多数据源/多存储后端的设计，每个存储 provider 实现本接口，
 * 仅关注如何按 key 读写内容，面向 Skill、Prompt 等通用 AI 资源。</p>
 *
 * <p>实现类应通过 {@link AiResourceStorageBuilder} 创建实例。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public interface AiResourceStorage {
    
    /**
     * 类型标识，与 {@link StorageKey#getProvider()} 对应。
     *
     * @return 存储 provider 类型，例如 {@code "nacos_config"}、{@code "oss"}
     */
    String type();
    
    /**
     * 将内容写入存储。
     *
     * @param storageKey 标识资源位置的存储键
     * @param content    待保存的字节内容
     * @throws NacosException 保存失败时抛出
     */
    void save(StorageKey storageKey, byte[] content) throws NacosException;
    
    /**
     * 从存储读取内容。
     *
     * @param storageKey 标识资源位置的存储键
     * @return 字节内容；不存在时返回 {@code null}
     * @throws NacosException 读取失败时抛出
     */
    byte[] get(StorageKey storageKey) throws NacosException;
    
    /**
     * 从存储删除内容。
     *
     * @param storageKey 标识资源位置的存储键
     * @throws NacosException 删除失败时抛出
     */
    void delete(StorageKey storageKey) throws NacosException;
}
