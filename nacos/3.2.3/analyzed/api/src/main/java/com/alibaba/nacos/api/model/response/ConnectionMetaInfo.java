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

package com.alibaba.nacos.api.model.response;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 客户端连接元数据。
 *
 * <p>记录连接类型、IP/端口、客户端版本、应用名、命名空间及标签等诊断信息。</p>
 *
 * @author Nacos
 */
public class ConnectionMetaInfo {
    
    /** 连接类型（如 gRPC、HTTP）。 */
    String connectType;
    
    /** 客户端本地 IP。 */
    String clientIp;
    
    /** 远端（服务端）IP。 */
    String remoteIp;
    
    /** 远端端口。 */
    int remotePort;
    
    /** 本地端口。 */
    int localPort;
    
    /** 客户端 SDK 版本。 */
    String version;
    
    /** 连接唯一标识。 */
    String connectionId;
    
    /** 连接建立时间。 */
    Date createTime;
    
    /** 最后活跃时间戳（毫秒）。 */
    long lastActiveTime;
    
    /** 客户端应用名称。 */
    String appName;
    
    /** 命名空间 ID。 */
    String namespaceId;
    
    /** 连接标签键值对。 */
    private Map<String, String> labels = new HashMap<>();
    
    /** 获取连接标签。 */
    public Map<String, String> getLabels() {
        return labels;
    }
    
    /** 设置连接标签。 */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    /** 获取客户端 IP。 */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 设置客户端 IP。 */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    /** 获取远端 IP。 */
    public String getRemoteIp() {
        return remoteIp;
    }
    
    /** 设置远端 IP。 */
    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }
    
    /** 获取远端端口。 */
    public int getRemotePort() {
        return remotePort;
    }
    
    /** 设置远端端口。 */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    
    /** 获取连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /** 设置连接 ID。 */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /** 获取连接建立时间。 */
    public Date getCreateTime() {
        return createTime;
    }
    
    /** 设置连接建立时间。 */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    /** 获取最后活跃时间戳。 */
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    /** 设置最后活跃时间戳。 */
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    /** 获取连接类型。 */
    public String getConnectType() {
        return connectType;
    }
    
    /** 设置连接类型。 */
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
    
    /** 获取客户端版本。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置客户端版本。 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 获取本地端口。 */
    public int getLocalPort() {
        return localPort;
    }
    
    /** 设置本地端口。 */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    /** 获取应用名称。 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置应用名称。 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取命名空间 ID。 */
    public String getNamespaceId() {
        return namespaceId;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
