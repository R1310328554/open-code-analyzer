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

package com.alibaba.nacos.plugin.ai.pipeline.model;

/**
 * 发布流水线上下文基类，封装各类 AI 资源发布审核共用的元数据字段。
 *
 * <p>不同资源类型通过子类扩展各自特有字段。流水线插件在 {@code execute()} 中可依据
 * {@link #getResourceType()} 将基类向下转型为对应子类，再读取类型专属上下文信息。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public class PublishPipelineContext {
    
    /**
     * 资源类型（SKILL / PROMPT / AGENTSPEC 等）。
     */
    private PublishPipelineResourceType resourceType;
    
    /**
     * 资源名称，例如 {@code "nacos-skill-registry"}。
     */
    private String resourceName;
    
    /**
     * 命名空间 ID。
     */
    private String namespaceId;
    
    /**
     * 当前待审核版本号，例如 {@code "v4"}。
     */
    private String version;
    
    /** 无参构造，供序列化与子类初始化使用。 */
    public PublishPipelineContext() {
    }
    
    /** @return 资源类型枚举值 */
    public PublishPipelineResourceType getResourceType() {
        return resourceType;
    }
    
    /** @param resourceType 资源类型 */
    public void setResourceType(PublishPipelineResourceType resourceType) {
        this.resourceType = resourceType;
    }
    
    /** @return 资源名称 */
    public String getResourceName() {
        return resourceName;
    }
    
    /** @param resourceName 资源名称 */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    /** @return 命名空间 ID */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** @param namespaceId 命名空间 ID */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /** @return 待审核版本号 */
    public String getVersion() {
        return version;
    }
    
    /** @param version 待审核版本号 */
    public void setVersion(String version) {
        this.version = version;
    }
}
