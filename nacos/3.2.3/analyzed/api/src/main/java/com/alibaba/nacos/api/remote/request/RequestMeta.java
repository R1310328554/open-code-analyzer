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

package com.alibaba.nacos.api.remote.request;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.common.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RPC 请求的客户端连接元数据。
 *
 * <p>服务端在处理 {@link Request} 时附加，包含 {@link #connectionId}、{@link #clientIp}、{@link #clientVersion}、标签与 {@link #abilityTable} 能力表，供路由、鉴权与能力协商使用。</p>
 *
 * @author liuzunfei
 * @version $Id: RequestMeta.java, v 0.1 2020年07月14日 10:32 AM liuzunfei Exp $
 */
public class RequestMeta {
    
    /** gRPC 连接唯一标识。 */
    private String connectionId = "";
    
    /** 客户端 IP 地址。 */
    private String clientIp = "";
    
    /** 客户端 SDK 版本号。 */
    private String clientVersion = "";
    
    /** 连接标签（来源、模块、应用名等）。 */
    private Map<String, String> labels = new HashMap<>();
    
    /** 从 labels 提取的应用级标签（去除 {@link Constants#APP_CONN_PREFIX} 前缀）。 */
    private Map<String, String> appLabels = new HashMap<>();
    
    /** 客户端能力表（特性名 → 是否支持）。 */
    private Map<String, Boolean> abilityTable;
    
    /**
     * 查询连接对指定能力的支持状态。
     *
     * @param abilityKey 能力键
     * @return 支持、不支持或未知
     */
    public AbilityStatus getConnectionAbility(AbilityKey abilityKey) {
        if (abilityTable == null || !abilityTable.containsKey(abilityKey.getName())) {
            return AbilityStatus.UNKNOWN;
        }
        return abilityTable.get(abilityKey.getName()) ? AbilityStatus.SUPPORTED
            : AbilityStatus.NOT_SUPPORTED;
    }
    
    /**
     * 设置客户端能力表。
     *
     * @param abilityTable 能力映射
     */
    public void setAbilityTable(Map<String, Boolean> abilityTable) {
        this.abilityTable = abilityTable;
    }
    
    /** 返回客户端 SDK 版本号。 */
    public String getClientVersion() {
        return clientVersion;
    }
    
    /**
     * 设置客户端 SDK 版本号。
     *
     * @param clientVersion 版本字符串
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
    
    /** 返回连接标签映射。 */
    public Map<String, String> getLabels() {
        return labels;
    }
    
    /**
     * 设置连接标签并刷新 {@link #appLabels}。
     *
     * @param labels 标签映射
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
        extractAppLabels();
    }
    
    /** 从 {@link #labels} 提取应用级标签（过滤 {@link Constants#APP_CONN_PREFIX} 前缀）。 */
    private void extractAppLabels() {
        HashMap<String, String> applabelsMap = new HashMap<String, String>(8) {
            
            {
                put(Constants.APPNAME, labels.get(Constants.APPNAME));
                put(Constants.CLIENT_VERSION_KEY, clientVersion);
                put(Constants.CLIENT_IP, clientIp);
            }
        };
        labels.entrySet().stream().filter(Objects::nonNull)
            .filter(e -> e.getKey().startsWith(Constants.APP_CONN_PREFIX)
                && e.getKey().length() > Constants.APP_CONN_PREFIX.length()
                && !e.getValue().trim().isEmpty())
            .forEach(entry -> {
                applabelsMap.putIfAbsent(
                    entry.getKey().substring(Constants.APP_CONN_PREFIX.length()),
                    entry.getValue());
            });
        this.appLabels = applabelsMap;
    }
    
    /**
     * 返回应用级标签映射（已去除 {@link Constants#APP_CONN_PREFIX} 前缀）。
     *
     * @return 应用标签映射
     * @date 2024/2/29
     */
    public Map<String, String> getAppLabels() {
        return appLabels;
    }
    
    /** 返回 gRPC 连接 ID。 */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * 设置 gRPC 连接 ID。
     *
     * @param connectionId 连接标识
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /** 返回客户端 IP。 */
    public String getClientIp() {
        return clientIp;
    }
    
    /**
     * 设置客户端 IP。
     *
     * @param clientIp IP 地址
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    @Override
    public String toString() {
        return "RequestMeta{" + "connectionId='" + connectionId + '\'' + ", clientIp='" + clientIp
            + '\''
            + ", clientVersion='" + clientVersion + '\'' + ", labels=" + labels + '}';
    }
}
