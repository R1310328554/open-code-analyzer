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

package com.alibaba.nacos.core.remote;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.common.utils.ConnLabelsUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.common.Constants.VIPSERVER_TAG;

/**
 * RPC 连接元数据，记录客户端/服务端地址、标签、活跃时间与推送队列阻塞信息。
 * ConnectionMetaInfo.
 *
 * @author liuzunfei
 * @version $Id: ConnectionMetaInfo.java, v 0.1 2020年07月13日 7:28 PM liuzunfei Exp $
 */
public class ConnectionMeta {
    
    /**
     * 连接类型（如 gRPC）。
     * ConnectionType.
     */
    String connectType;
    
    /**
     * 客户端 IP 地址。
     * Client IP Address.
     */
    String clientIp;
    
    /**
     * 远端 IP 地址。
     * Remote IP Address.
     */
    String remoteIp;
    
    /**
     * 远端端口。
     * Remote IP Port.
     */
    int remotePort;
    
    /**
     * 本地监听端口。
     * Local Ip Port.
     */
    int localPort;
    
    /**
     * 客户端版本号。
     * Client version.
     */
    String version;
    
    /**
     * 全局唯一连接标识。
     * Identify Unique connectionId.
     */
    String connectionId;
    
    /**
     * 连接建立时间。
     * create time.
     */
    Date createTime;
    
    /**
     * 最后一次活跃时间戳（毫秒）。
     * lastActiveTime.
     */
    long lastActiveTime;
    
    /**
     * 应用名称。
     * String appName.
     */
    String appName;
    
    /**
     * 命名空间 ID。
     * namespaceId.
     */
    String namespaceId;
    
    /** 首次推送队列阻塞时间戳。 */
    long firstPushQueueBlockTime = 0;
    
    /** 最近一次推送队列阻塞时间戳。 */
    long lastPushQueueBlockTime = 0;
    
    /** 连接标签键值对（来源、模块等）。 */
    protected Map<String, String> labels = new HashMap<>();
    
    /** 是否启用 TLS 保护。 */
    boolean tlsProtected = false;
    
    public String getLabel(String labelKey) {
        return labels.get(labelKey);
    }
    
    public String getTag() {
        return labels.get(VIPSERVER_TAG);
    }
    
    public ConnectionMeta(String connectionId, String clientIp, String remoteIp, int remotePort,
        int localPort,
        String connectType, String version, String appName, Map<String, String> labels) {
        this.connectionId = connectionId;
        this.clientIp = clientIp;
        this.connectType = connectType;
        this.version = version;
        this.appName = appName;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.createTime = new Date();
        this.lastActiveTime = System.currentTimeMillis();
        this.labels.putAll(labels);
    }
    
    /**
     * 判断连接来源是否为 SDK 客户端。
     * check if this connection is sdk source.
     *
     * @return if this connection is sdk source.
     */
    public boolean isSdkSource() {
        String source = labels.get(RemoteConstants.LABEL_SOURCE);
        return RemoteConstants.LABEL_SOURCE_SDK.equalsIgnoreCase(source);
    }
    
    /**
     * 判断连接来源是否为集群节点。
     * check if this connection is sdk source.
     *
     * @return if this connection is sdk source.
     */
    public boolean isClusterSource() {
        String source = labels.get(RemoteConstants.LABEL_SOURCE);
        return RemoteConstants.LABEL_SOURCE_CLUSTER.equalsIgnoreCase(source);
    }
    
    /**
     * Getter method for property <tt>labels</tt>.
     *
     * @return property value of labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }
    
    /**
     * 提取以 {@link Constants#APP_CONN_PREFIX} 开头的应用标签并去除前缀后返回新 Map。
     * get labels map with filter of starting with prefix #{@link Constants#APP_CONN_PREFIX}
     * and return a new map trim the prefix #{@link Constants#APP_CONN_PREFIX}.
     * @date 2024/2/29
     * @return map of labels.
     */
    public Map<String, String> getAppLabels() {
        HashMap<String, String> labelsMap = new HashMap<String, String>(8) {
            
            {
                put(Constants.APPNAME, labels.get(Constants.APPNAME));
                put(Constants.CLIENT_VERSION_KEY, version);
            }
        };
        return ConnLabelsUtils.mergeMapByOrder(labelsMap, labels.entrySet().stream()
            .filter(Objects::nonNull)
            .filter(e -> e.getKey().startsWith(Constants.APP_CONN_PREFIX)
                && e.getKey().length() > Constants.APP_CONN_PREFIX.length()
                && StringUtils.isNotBlank(e.getValue()))
            .collect(
                Collectors.toMap(k -> k.getKey().substring(Constants.APP_CONN_PREFIX.length()),
                    Map.Entry::getValue)));
    }
    
    /**
     * Setter method for property <tt>labels</tt>.
     *
     * @param labels value to be assigned to property labels
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    /**
     * Getter method for property <tt>clientIp</tt>.
     *
     * @return property value of clientIp
     */
    public String getClientIp() {
        return clientIp;
    }
    
    /**
     * Setter method for property <tt>clientIp</tt>.
     *
     * @param clientIp value to be assigned to property clientIp
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    /**
     * Getter method for property <tt>remoteIp</tt>.
     *
     * @return property value of remoteIp
     */
    public String getRemoteIp() {
        return remoteIp;
    }
    
    /**
     * Getter method for property <tt>remotePort</tt>.
     *
     * @return property value of remotePort
     */
    public int getRemotePort() {
        return remotePort;
    }
    
    /**
     * Getter method for property <tt>connectionId</tt>.
     *
     * @return property value of connectionId
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * Setter method for property <tt>connectionId</tt>.
     *
     * @param connectionId value to be assigned to property connectionId
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * Getter method for property <tt>createTime</tt>.
     *
     * @return property value of createTime
     */
    public Date getCreateTime() {
        return createTime;
    }
    
    /**
     * Setter method for property <tt>createTime</tt>.
     *
     * @param createTime value to be assigned to property createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    /**
     * Getter method for property <tt>lastActiveTime</tt>.
     *
     * @return property value of lastActiveTime
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    /**
     * Setter method for property <tt>lastActiveTime</tt>.
     *
     * @param lastActiveTime value to be assigned to property lastActiveTime
     */
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    /**
     * Getter method for property <tt>connectType</tt>.
     *
     * @return property value of connectType
     */
    public String getConnectType() {
        return connectType;
    }
    
    /**
     * Setter method for property <tt>connectType</tt>.
     *
     * @param connectType value to be assigned to property connectType
     */
    public void setConnectType(String connectType) {
        this.connectType = connectType;
    }
    
    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Setter method for property <tt>version</tt>.
     *
     * @param version value to be assigned to property version
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Getter method for property <tt>localPort</tt>.
     *
     * @return property value of localPort
     */
    public int getLocalPort() {
        return localPort;
    }
    
    /**
     * Setter method for property <tt>localPort</tt>.
     *
     * @param localPort value to be assigned to property localPort
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    /**
     * 记录推送队列阻塞发生次数（首次与最近一次时间戳）。
     * recordPushQueueBlockTimes.
     */
    public void recordPushQueueBlockTimes() {
        if (this.firstPushQueueBlockTime == 0) {
            firstPushQueueBlockTime = System.currentTimeMillis();
        } else {
            lastPushQueueBlockTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 清除推送队列阻塞时间记录。
     * clear push queue block times.
     */
    public void clearPushQueueBlockTimes() {
        this.firstPushQueueBlockTime = 0;
        this.lastPushQueueBlockTime = 0;
    }
    
    public boolean isTlsProtected() {
        return tlsProtected;
    }
    
    public void setTlsProtected(boolean tlsProtected) {
        this.tlsProtected = tlsProtected;
    }
    
    /**
     * 判断最近一次阻塞与首次阻塞的时间差是否超过给定阈值。
     * check block greater than the specific time.
     * @param timeMillsSeconds check times.
     * @return 是否超过阈值
     */
    public boolean pushQueueBlockTimesLastOver(long timeMillsSeconds) {
        return this.lastPushQueueBlockTime - this.firstPushQueueBlockTime > timeMillsSeconds;
    }
    
    @Override
    public String toString() {
        return "ConnectionMeta{" + "connectType='" + connectType + '\'' + ", clientIp='" + clientIp
            + '\''
            + ", remoteIp='" + remoteIp + '\'' + ", remotePort=" + remotePort + ", localPort="
            + localPort
            + ", version='" + version + '\'' + ", connectionId='" + connectionId + '\''
            + ", createTime="
            + createTime + ", lastActiveTime=" + lastActiveTime + ", appName='" + appName + '\''
            + ", tenant='"
            + namespaceId + '\'' + ", labels=" + labels + '}';
    }
}
