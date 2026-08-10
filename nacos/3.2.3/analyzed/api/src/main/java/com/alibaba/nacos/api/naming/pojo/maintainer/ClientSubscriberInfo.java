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

package com.alibaba.nacos.api.naming.pojo.maintainer;

import java.io.Serializable;

/**
 * 命名客户端订阅者信息。
 *
 * <p>描述订阅某服务的客户端身份、应用名与连接地址。</p>
 *
 * @author xiweng.yy
 */
public class ClientSubscriberInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1889748153147644741L;
    
    /** 客户端唯一标识。 */
    private String clientId;
    
    /** 应用名称。 */
    private String appName;
    
    /** 客户端 SDK/代理标识。 */
    private String agent;
    
    /** 客户端连接地址（通常为 IP:端口）。 */
    private String address;
    
    /** 获取客户端 ID。 */
    public String getClientId() {
        return clientId;
    }
    
    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /** 获取应用名。 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置应用名。 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取客户端代理标识。 */
    public String getAgent() {
        return agent;
    }
    
    /** 设置客户端代理标识。 */
    public void setAgent(String agent) {
        this.agent = agent;
    }
    
    /** 获取客户端地址。 */
    public String getAddress() {
        return address;
    }
    
    /** 设置客户端地址。 */
    public void setAddress(String address) {
        this.address = address;
    }
}
