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

/**
 * 用于创建 {@link AiResourceStorage} 实例的 Builder SPI。
 *
 * <p>SPI 加载的类通常通过无参构造实例化，因此采用 Builder 模式组装存储实现。
 * 每个存储 provider 应实现本接口并通过 SPI（META-INF/services）注册。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public interface AiResourceStorageBuilder {
    
    /**
     * 类型标识，与 {@link AiResourceStorage#type()} 对应。
     *
     * @return 存储 provider 类型，例如 {@code "nacos_config"}、{@code "oss"}
     */
    String type();
    
    /**
     * 构建 {@link AiResourceStorage} 实例。
     *
     * @return 已完成初始化的 {@link AiResourceStorage} 实例
     */
    AiResourceStorage build();
}
