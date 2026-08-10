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
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportArtifact;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportCandidatePage;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportContext;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportItem;

import java.util.Set;

/**
 * AI 资源导入服务 SPI 接口。
 *
 * <p>实现类仅负责外部来源的发现与转换为 {@link AiResourceImportArtifact}，
 * 不得直接写入 Nacos 持久化存储；持久化由 Nacos 资源操作器完成。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public interface AiResourceImportService {
    
    /**
     * 导入器实现名称，例如 {@code mcp-registry}。
     *
     * @return 导入器类型标识
     */
    String importerType();
    
    /**
     * 该导入器支持的资源类型集合，例如 {@code mcp} 或 {@code skill}。
     *
     * @return 支持的资源类型集合
     */
    Set<String> supportedResourceTypes();
    
    /**
     * 从已解析的来源搜索外部候选资源。
     *
     * <p>返回的候选项仅含元数据摘要；完整可导入载荷只能通过
     * {@link #fetch(AiResourceImportContext, AiResourceImportItem)} 获取。</p>
     *
     * @param context 含已解析来源与查询选项的导入上下文
     * @return 候选资源分页结果
     * @throws NacosException 来源不可达或搜索失败时抛出
     */
    AiResourceImportCandidatePage search(AiResourceImportContext context) throws NacosException;
    
    /**
     * 拉取用户选中的外部条目并转换为导入 Artifact。
     *
     * @param context 含已解析来源与运行时限额的导入上下文
     * @param item    用户选中的外部资源条目
     * @return 可交给资源操作器处理的导入 Artifact
     * @throws NacosException 拉取或转换失败时抛出
     */
    AiResourceImportArtifact fetch(AiResourceImportContext context, AiResourceImportItem item)
        throws NacosException;
}
