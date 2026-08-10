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

import java.util.ArrayList;
import java.util.List;

/**
 * 与存储无关的已授权资源集合。
 *
 * <p>用于可见性查询规划，描述某资源类型下当前用户可访问的资源名称列表。</p>
 *
 * @author xiweng.yy
 */
public class AuthorizedResources {
    
    /** 资源类型标识。 */
    private String resourceType;
    
    /** 已授权的资源名称列表。 */
    private List<String> resources = new ArrayList<>();
    
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
    
    /**
     * 获取已授权资源名称列表。
     *
     * @return 资源名称列表
     */
    public List<String> getResources() {
        return resources;
    }
    
    /**
     * 设置已授权资源名称列表。
     *
     * @param resources 资源名称列表
     */
    public void setResources(List<String> resources) {
        this.resources = resources;
    }
}
