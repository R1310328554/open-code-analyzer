/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.connection.request;

import java.util.Map;

/**
 * 连接准入检查请求，携带客户端 IP、应用名、来源及可选标签。
 *
 * <p>由 {@link com.alibaba.nacos.plugin.control.connection.ConnectionControlManager}
 * 在客户端建连前校验总连接数是否超限。</p>
 *
 * @author shiyiyue
 */
public class ConnectionCheckRequest {
    
    /** 客户端 IP 地址。 */
    String clientIp;
    
    /** 客户端应用名。 */
    String appName;
    
    /** 连接来源标识（如协议或入口类型）。 */
    String source;
    
    /** 附加标签，用于监控或路由维度扩展。 */
    Map<String, String> labels;
    
    /**
     * 构造连接检查请求。
     *
     * @param clientIp 客户端 IP
     * @param appName  应用名
     * @param source   连接来源
     */
    public ConnectionCheckRequest(String clientIp, String appName, String source) {
        this.appName = appName;
        this.clientIp = clientIp;
        this.source = source;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
}
