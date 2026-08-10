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

package com.alibaba.nacos.ai.config;

import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;

import java.util.List;

/**
 * 迁移期间读取旧版 Prompt 数据的 SPI 接口。
 *
 * <p>不同环境（开源 Nacos 与商业版）可能以不同旧版格式存储 Prompt。
 * 实现类负责扫描并读取旧版数据，供迁移至 DB + 类型化存储架构。</p>
 *
 * <p>默认实现（{@code nacos}）从 Nacos Config（{@code nacos-ai-prompt} 分组）读取。
 * 商业版可提供自定义 {@code @Component} Bean 覆盖。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public interface PromptLegacyDataReader {
    
    /**
     * 提供者类型标识，通过配置项 {@code nacos.ai.prompt.migration.provider} 选择活动读取器。
     *
     * @return type string, e.g. "nacos"
     */
    String type();
    
    /**
     * 扫描旧版存储，返回全部 Prompt 及其元数据与版本列表。
     * 不包含版本内容；按需通过 {@link #readVersionContent} 加载。
     *
     * @return list of legacy prompt data
     */
    List<LegacyPromptData> scanLegacyPrompts();
    
    /**
     * 从旧版存储读取指定 Prompt 版本的内容。
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param version     version string
     * @return version info with template/variables/srcUser/commitMsg, or null if not found
     */
    PromptVersionInfo readVersionContent(String namespaceId, String promptKey, String version);
    
    /**
     * 新系统中删除 Prompt 后清理旧版存储条目，防止迁移任务在下次重启时重新导入。
     *
     * <p>默认空实现，保持与既有实现类的向后兼容。</p>
     *
     * @param namespaceId namespace ID
     * @param promptKey   prompt key
     * @param versions    version strings to clean up
     */
    default void cleanupLegacyData(String namespaceId, String promptKey, List<String> versions) {
    }
}
