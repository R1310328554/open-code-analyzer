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

package com.alibaba.nacos.config.server.model;

import java.io.Serializable;
import java.util.List;

/**
 * 配置 ACL 信息模型：是否开启 IP 白名单及允许的 IP 列表。
 * 用于持久化与 API 传输访问控制策略。
 * Acl info.
 *
 * @author Nacos
 */
public class AclInfo implements Serializable {
    
    private static final long serialVersionUID = 1383026926036269457L;
    
    /** 是否启用 ACL 限制 */
    private Boolean isOpen;
    
    /** 允许访问的 IP 地址列表 */
    private List<String> ips;
    
    /** 获取 IP 白名单列表 */
    public List<String> getIps() {
        return ips;
    }
    
    /** 设置 IP 白名单列表 */
    public void setIps(List<String> ips) {
        this.ips = ips;
    }
    
    /** 是否开启 ACL */
    public Boolean getIsOpen() {
        return isOpen;
    }
    
    /** 设置 ACL 开关 */
    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }
}
