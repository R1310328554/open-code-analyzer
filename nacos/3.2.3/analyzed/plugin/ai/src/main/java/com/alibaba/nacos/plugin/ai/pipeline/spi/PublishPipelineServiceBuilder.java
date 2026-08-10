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

import java.util.Properties;

/**
 * 用于创建 {@link PublishPipelineService} 实例的 Builder SPI。
 *
 * <p>SPI 加载的类通常通过无参构造实例化，因此采用 Builder 模式组装流水线实现。
 * 每个流水线插件应实现本接口并通过 SPI（META-INF/services）注册。</p>
 *
 * <p>{@link #build(Properties)} 接收来自流水线配置的 per-plugin 属性
 *（例如 {@code nacos.plugin.{pluginName}.{type}.*}），便于各插件以自定义参数
 *（API 端点、超时等）完成初始化。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public interface PublishPipelineServiceBuilder {
    
    /**
     * 流水线插件标识，与 {@link PublishPipelineService#pipelineId()} 对应。
     *
     * @return 流水线插件 ID，例如 {@code "ai-review"}、{@code "manual-confirm"}
     */
    String pipelineId();
    
    /**
     * 根据给定配置属性构建 {@link PublishPipelineService} 实例。
     *
     * <p>属性来源于流水线配置，键名与本 Builder 的 {@link #pipelineId()} 关联。
     * 例如 pipelineId 为 {@code "ai-review"} 时，可能包含 {@code endpoint}、
     * {@code timeout} 等条目。</p>
     *
     * @param properties 节点级配置属性，永不为 {@code null}（可为空）
     * @return 已完成初始化的 {@link PublishPipelineService} 实例
     */
    PublishPipelineService build(Properties properties);
}
