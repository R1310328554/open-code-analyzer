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

package com.alibaba.nacos.plugin.visibility.model;

/**
 * 可见性查询规划的最小上下文。
 *
 * <p>携带命名空间与资源类型信息，供可见性插件生成查询建议。</p>
 *
 * @author xiweng.yy
 */
public class VisibilityQueryContext {
    
    /** 命名空间 ID。 */
    private String namespaceId;
    
    /** 资源类型。 */
    private String resourceType;
    
    /**
     * 获取命名空间 ID。
     *
     * @return 命名空间 ID
     */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /**
     * 设置命名空间 ID。
     *
     * @param namespaceId 命名空间 ID
     */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /**
     * 获取资源类型。
     *
     * @return 资源类型
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * 设置资源类型。
     *
     * @param resourceType 资源类型
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
