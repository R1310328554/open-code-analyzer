/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.users;

/**
 * 鉴权上下文中的 Nacos 用户模型。
 *
 * <p>扩展持久层 {@link User}，附加 JWT token 与是否全局管理员标记。</p>
 *
 * @author nkorange
 * @since 1.2.0
 */
public class NacosUser extends User {
    
    /** 当前会话 JWT 令牌。 */
    private String token;
    
    /** 是否为 GLOBAL_ADMIN 角色用户。 */
    private boolean globalAdmin = false;
    
    public NacosUser() {
    }
    
    public NacosUser(String userName) {
        setUserName(userName);
    }
    
    /** 构造带用户名与 token 的用户对象。 */
    public NacosUser(String userName, String token) {
        setUserName(userName);
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public boolean isGlobalAdmin() {
        return globalAdmin;
    }
    
    public void setGlobalAdmin(boolean globalAdmin) {
        this.globalAdmin = globalAdmin;
    }
    
    @Override
    public String toString() {
        return "NacosUser{" + "token='" + token + '\'' + ", globalAdmin=" + globalAdmin + '}';
    }
}
