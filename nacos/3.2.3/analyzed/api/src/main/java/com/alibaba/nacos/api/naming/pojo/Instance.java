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

package com.alibaba.nacos.api.naming.pojo;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.NacosForm;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.api.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 服务实例模型，表示注册到 Nacos 命名服务的一个可调用端点。
 *
 * <p>包含 IP、端口、权重、健康状态、集群与服务归属及扩展元数据，实现 {@link NacosForm} 以支持参数校验。</p>
 *
 * @author nkorange
 */
@JsonInclude(Include.NON_NULL)
public class Instance implements NacosForm {
    
    private static final long serialVersionUID = -742906310567291979L;
    
    // 端口合法上界
    private static final int MAX_PORT = 65535;
    
    /**
     * 实例唯一标识。
     */
    private String instanceId;
    
    /**
     * 实例 IP 地址。
     */
    private String ip;
    
    /**
     * 实例端口号。
     */
    private int port;
    
    /**
     * 负载均衡权重，默认 1.0。
     */
    private double weight = 1.0D;
    
    /**
     * 实例健康状态，{@code true} 表示健康。
     */
    private boolean healthy = true;
    
    /**
     * 实例是否启用，{@code false} 时不接收流量。
     */
    private boolean enabled = true;
    
    /**
     * 是否为临时实例；临时实例依赖心跳维持，断开连接后会被摘除。
     *
     * @since 1.0.0
     */
    private boolean ephemeral = true;
    
    /**
     * 实例所属集群名。
     */
    private String clusterName;
    
    /**
     * 实例所属服务名。
     */
    private String serviceName;
    
    /**
     * 用户自定义扩展元数据。
     */
    private Map<String, String> metadata = new HashMap<>();
    
    /** 获取实例 ID。 */
    public String getInstanceId() {
        return this.instanceId;
    }
    
    /** 设置实例 ID。 */
    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }
    
    /** 获取实例 IP。 */
    public String getIp() {
        return this.ip;
    }
    
    /** 设置实例 IP。 */
    public void setIp(final String ip) {
        this.ip = ip;
    }
    
    /** 获取实例端口。 */
    public int getPort() {
        return this.port;
    }
    
    /** 设置实例端口。 */
    public void setPort(final int port) {
        this.port = port;
    }
    
    /** 获取负载均衡权重。 */
    public double getWeight() {
        return this.weight;
    }
    
    /** 设置负载均衡权重。 */
    public void setWeight(final double weight) {
        this.weight = weight;
    }
    
    /** 实例是否健康。 */
    public boolean isHealthy() {
        return this.healthy;
    }
    
    /** 设置健康状态。 */
    public void setHealthy(final boolean healthy) {
        this.healthy = healthy;
    }
    
    /** 获取所属集群名。 */
    public String getClusterName() {
        return this.clusterName;
    }
    
    /** 设置所属集群名。 */
    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }
    
    /** 获取所属服务名。 */
    public String getServiceName() {
        return this.serviceName;
    }
    
    /** 设置所属服务名。 */
    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 获取扩展元数据。 */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }
    
    /** 设置扩展元数据。 */
    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * 向元数据中添加键值对。
     *
     * @param key   元数据键
     * @param value 元数据值
     */
    public void addMetadata(final String key, final String value) {
        if (metadata == null) {
            metadata = new HashMap<>(4);
        }
        metadata.put(key, value);
    }
    
    /** 实例是否启用。 */
    public boolean isEnabled() {
        return this.enabled;
    }
    
    /** 设置是否启用。 */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    /** 是否为临时实例。 */
    public boolean isEphemeral() {
        return this.ephemeral;
    }
    
    /** 设置是否为临时实例。 */
    public void setEphemeral(final boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    @Override
    public void validate() throws NacosApiException {
        fillDefaultValue();
        if (StringUtils.isBlank(ip)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.PARAMETER_MISSING,
                "Required parameter 'ip' type String is not present");
        }
        if (port < 0 || port > MAX_PORT) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Required parameter 'port' type int is require 0 ~ 65535");
        }
    }
    
    /** 填充集群名等默认值。 */
    private void fillDefaultValue() {
        if (StringUtils.isBlank(clusterName)) {
            clusterName = Constants.DEFAULT_CLUSTER_NAME;
        }
    }
    
    @Override
    public String toString() {
        return "Instance{" + "instanceId='" + instanceId + '\'' + ", ip='" + ip + '\'' + ", port="
            + port + ", weight="
            + weight + ", healthy=" + healthy + ", enabled=" + enabled + ", ephemeral=" + ephemeral
            + ", clusterName='" + clusterName + '\'' + ", serviceName='" + serviceName + '\''
            + ", metadata="
            + metadata + '}';
    }
    
    /** 返回 {@code ip:port} 格式的网络地址字符串。 */
    public String toInetAddr() {
        return ip + ":" + port;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Instance)) {
            return false;
        }
        
        final Instance host = (Instance) obj;
        return Instance.strEquals(host.toString(), toString());
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    /** 空安全字符串相等比较。 */
    private static boolean strEquals(final String str1, final String str2) {
        return Objects.equals(str1, str2);
    }
    
    /** 从元数据读取心跳间隔（毫秒），缺省使用全局默认值。 */
    public long getInstanceHeartBeatInterval() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.HEART_BEAT_INTERVAL,
            Constants.DEFAULT_HEART_BEAT_INTERVAL);
    }
    
    /** 从元数据读取心跳超时（毫秒），缺省使用全局默认值。 */
    public long getInstanceHeartBeatTimeOut() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.HEART_BEAT_TIMEOUT,
            Constants.DEFAULT_HEART_BEAT_TIMEOUT);
    }
    
    /** 从元数据读取 IP 删除超时（毫秒），缺省使用全局默认值。 */
    public long getIpDeleteTimeout() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.IP_DELETE_TIMEOUT,
            Constants.DEFAULT_IP_DELETE_TIMEOUT);
    }
    
    /** 从元数据读取实例 ID 生成策略，缺省使用全局默认值。 */
    public String getInstanceIdGenerator() {
        return getMetaDataByKeyWithDefault(PreservedMetadataKeys.INSTANCE_ID_GENERATOR,
            Constants.DEFAULT_INSTANCE_ID_GENERATOR);
    }
    
    /**
     * 判断元数据中是否包含指定键。
     *
     * @param key 元数据键
     * @return 包含该键返回 {@code true}，否则 {@code false}
     */
    public boolean containsMetadata(final String key) {
        if (getMetadata() == null || getMetadata().isEmpty()) {
            return false;
        }
        return getMetadata().containsKey(key);
    }
    
    /** 从元数据读取 long 类型值，无效或缺失时返回默认值。 */
    private long getMetaDataByKeyWithDefault(final String key, final long defaultValue) {
        if (getMetadata() == null || getMetadata().isEmpty()) {
            return defaultValue;
        }
        final String value = getMetadata().get(key);
        if (NamingUtils.isNumber(value)) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }
    
    /** 从元数据读取字符串值，缺失时返回默认值。 */
    private String getMetaDataByKeyWithDefault(final String key, final String defaultValue) {
        if (getMetadata() == null || getMetadata().isEmpty()) {
            return defaultValue;
        }
        return getMetadata().get(key);
    }
    
}
