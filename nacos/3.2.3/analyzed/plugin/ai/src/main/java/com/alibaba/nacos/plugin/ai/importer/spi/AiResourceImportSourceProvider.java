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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportSource;

import java.util.Collection;
import java.util.Properties;

/**
 * 提供运维侧预置导入来源的 SPI 接口。
 *
 * <p>实现类只能返回源自服务端配置或可信默认值的来源列表；
 * 终端用户通过 sourceId 选择来源，不得通过请求参数自行指定网络端点。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public interface AiResourceImportSourceProvider {
    
    /**
     * 从服务端配置加载已启用的导入来源预置列表。
     *
     * @param properties 服务端配置属性，永不为 null
     * @return 导入来源预置集合
     * @throws NacosException 预置配置无效时抛出
     */
    Collection<AiResourceImportSource> loadSources(Properties properties) throws NacosException;
}
