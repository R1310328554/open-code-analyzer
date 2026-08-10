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
 * 命名 {@link com.alibaba.nacos.naming.core.v2.client.Client} 摘要信息。
 *
 * <p>运维侧展示客户端类型、连接方式、版本及最后更新时间等概要字段。</p>
 *
 * @author xiweng.yy
 */
public class ClientSummaryInfo implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = -4482158251664716884L;
    
    /** 客户端唯一标识。 */
    private String clientId;
    
    /** 是否为临时（ephemeral）客户端。 */
    private boolean ephemeral;
    
    /** 最后更新时间戳（毫秒）。 */
    private long lastUpdatedTime;
    
    /**
     * 客户端类型：2.0 及以上长连接客户端为 {@code connection}，否则为 {@code ipPort}。
     */
    private String clientType;
    
    /** 以下字段仅当 {@link #clientType} 为 {@code connection} 时有意义。 */
    // connectType、appName 等长连接专属属性
    /** 长连接类型（如 gRPC）。 */
    private String connectType;
    
    /** 应用名称。 */
    private String appName;
    
    /** 客户端 SDK 版本。 */
    private String version;
    
    /** 客户端 IP。 */
    private String clientIp;
    
    /** 客户端端口。 */
    private int clientPort;
    
    /** 获取客户端 ID。 */
    public String getClientId() {
        return clientId;
    }
    
    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /** 是否为临时客户端。 */
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    /** 设置是否临时客户端。 */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    /** 获取最后更新时间。 */
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    
    /** 设置最后更新时间。 */
    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
    
    /** 获取客户端类型。 */
    public String getClientType() {
        return clientType;
    }
    
    /** 设置客户端类型。 */
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    
    /** 获取连接类型。 */
    public String getConnectType() {
        return connectType;
    }
    
    /** 设置连接类型。 */
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
    
    /** 获取应用名。 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置应用名。 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取客户端版本。 */
    public String getVersion() {
        return version;
    }
    
    /** 设置客户端版本。 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 获取客户端 IP。 */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 设置客户端 IP。 */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    /** 获取客户端端口。 */
    public int getClientPort() {
        return clientPort;
    }
    
    /** 设置客户端端口。 */
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
}
