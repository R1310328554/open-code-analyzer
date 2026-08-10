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

import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;

/**
 * 支持可见性校验的资源基类。
 *
 * <p>子类需实现命名空间、资源名与资源类型等标识方法，
 * 并携带可见范围（scope）与所有者（owner）属性。</p>
 *
 * @author xiweng.yy
 */
public abstract class VisibilityResource {
    
    /** 可见范围，默认为私有。 */
    private String scope = VisibilityConstants.SCOPE_PRIVATE;
    
    /** 资源所有者标识。 */
    private String owner = "";
    
    /**
     * 获取资源所属命名空间 ID。
     *
     * @return namespace id
     */
    public abstract String getNamespaceId();
    
    /**
     * 获取资源在其命名空间与类型下的唯一名称。
     *
     * @return resource name
     */
    public abstract String getResourceName();
    
    /**
     * 获取资源子类型，例如 "skill"、"mcp"、"prompt"、"a2a"。
     *
     * @return resource type
     */
    public abstract String getResourceType();
    
    /**
     * 获取可见范围。
     *
     * @return 可见范围
     */
    public String getScope() {
        return scope;
    }
    
    /**
     * 设置可见范围。
     *
     * @param scope 可见范围
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    /**
     * 获取资源所有者。
     *
     * @return 所有者标识
     */
    public String getOwner() {
        return owner;
    }
    
    /**
     * 设置资源所有者。
     *
     * @param owner 所有者标识
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
}
