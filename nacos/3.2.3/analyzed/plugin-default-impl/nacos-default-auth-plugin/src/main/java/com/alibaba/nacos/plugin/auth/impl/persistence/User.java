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
 * Nacos 控制台用户实体。
 *
 * <p>映射 {@code users} 表，包含用户名与密码字段； 密码在持久化层通常已加密，供 Spring Security 认证加载。</p>
 *
 * @author wfnuser
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 3371769277802700069L;
    
    /** 登录用户名，全局唯一。 */
    private String username;
    
    /** 用户密码（存储格式取决于加密策略）。 */
    private String password;
    
    /** 获取密码字段。 */
    public String getPassword() {
        return password;
    }
    
    /** 设置密码字段。 */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /** 获取用户名。 */
    public String getUsername() {
        return username;
    }
    
    /** 设置用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }
}
