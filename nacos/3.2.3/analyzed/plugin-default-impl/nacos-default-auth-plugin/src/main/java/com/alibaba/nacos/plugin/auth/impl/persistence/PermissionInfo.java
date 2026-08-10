/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.persistence;

import java.io.Serializable;

/**
 * 权限信息模型：角色对资源的操作授权。
 *
 * <p>三元组 {@code role + resource + action} 对应 RBAC 中一条权限记录， 序列化后用于持久化层与 API 传输。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class PermissionInfo implements Serializable {
    
    private static final long serialVersionUID = 388813573388837395L;
    
    /** 拥有该权限的角色名。 */
    private String role;
    
    /** 受控资源标识（如命名空间、配置路径等）。 */
    private String resource;
    
    /** 对资源允许的操作（读、写、删除等）。 */
    private String action;
    
    /** 获取角色名。 */
    public String getRole() {
        return role;
    }
    
    /** 设置角色名。 */
    public void setRole(String role) {
        this.role = role;
    }
    
    /** 获取资源标识。 */
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    /** 获取操作类型。 */
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
}
