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
 * 角色信息模型：用户与角色的绑定关系。
 *
 * <p>{@code roles} 表一行对应一个 {@link RoleInfo}， 包含角色名与所属用户名，用于控制台展示与鉴权校验。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class RoleInfo implements Serializable {
    
    private static final long serialVersionUID = 5946986388047856568L;
    
    /** 角色名称。 */
    private String role;
    
    /** 被分配该角色的用户名。 */
    private String username;
    
    /** 获取角色名。 */
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    /** 获取用户名。 */
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    /** 返回角色与用户的调试字符串。 */
    @Override
    public String toString() {
        return "RoleInfo{" + "role='" + role + '\'' + ", username='" + username + '\'' + '}';
    }
}
