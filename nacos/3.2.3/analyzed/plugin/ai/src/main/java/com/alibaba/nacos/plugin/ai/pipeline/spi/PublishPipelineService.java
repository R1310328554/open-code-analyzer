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

package com.alibaba.nacos.plugin.ai.pipeline.spi;

import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineContext;
import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineResourceType;
import com.alibaba.nacos.plugin.ai.pipeline.model.PublishPipelineResult;

/**
 * 发布流水线服务 SPI 接口。
 *
 * <p>借鉴 {@code ConfigChangePluginService} 的拦截/审核思路，在 AI 资源正式发布前
 * 插入可插拔的审核环节，面向 Skill、Prompt、MCP 等通用 AI 资源，不限于单一类型。</p>
 *
 * <p>多个流水线插件按 {@link #getPreferOrder()} 升序串行执行，仅当前一个插件
 * 返回通过后才会继续执行下一个。</p>
 *
 * <p>实现类应通过 {@link PublishPipelineServiceBuilder} 创建实例。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public interface PublishPipelineService {
    
    /**
     * 本流水线插件的唯一标识，例如 {@code "ai-review"}、{@code "manual-confirm"}。
     *
     * @return 流水线插件 ID
     */
    String pipelineId();
    
    /**
     * 执行审核/拦截逻辑。
     *
     * @param context 发布上下文，含资源元数据、版本信息、文件内容等
     * @return 审核结果，包含是否通过与说明信息
     */
    PublishPipelineResult execute(PublishPipelineContext context);
    
    /**
     * 执行顺序，数值越小越先执行。
     * 设计思路参考 {@code ConfigChangePluginService.getOrder()}。
     *
     * @return 排序权重
     */
    int getPreferOrder();
    
    /**
     * 声明本插件支持审核的资源类型。
     * 设计思路参考 {@code ConfigChangePluginService.pointcutMethodNames()}，
     * 供 {@code PublishPipelineManager} 按资源类型路由到对应插件链。
     *
     * @return 支持的资源类型数组
     */
    PublishPipelineResourceType[] pipelineResourceTypes();
}
