/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.importer.spi;

import java.util.Properties;

/**
 * 用于创建 {@link AiResourceImportService} 实例的构建器 SPI。
 *
 * <p>SPI 加载的类通常通过无参构造实例化，本构建器模式允许导入器
 * 在创建时注入插件私有配置。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public interface AiResourceImportServiceBuilder {
    
    /**
     * 导入器实现名称。
     *
     * @return 导入器类型，例如 {@code mcp-registry}；与
     *         {@link AiResourceImportService#importerType()} 对应
     */
    String importerType();
    
    /**
     * 使用给定配置属性构建 {@link AiResourceImportService} 实例。
     *
     * @param properties 导入器配置属性，永不为 null
     * @return 已初始化的导入服务实例
     */
    AiResourceImportService build(Properties properties);
}
