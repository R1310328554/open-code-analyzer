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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务实例快照信息，含实例列表但不含集群详情，用于数据推送与客户端本地缓存。
 *
 * <p>由 nacos-client 订阅更新后缓存在内存与磁盘中，支持过期判断、校验和比对及深拷贝。</p>
 *
 * @author nkorange
 * @author shizhengxing
 */
@JsonInclude(Include.NON_NULL)
public class ServiceInfo implements Cloneable {
    
    /**
     * 缓存文件名格式：{@code groupName@@name@@clusters} 各段在数组中的下标。
     */
    private static final int GROUP_POSITION = 0;
    
    /** 服务名在缓存键各段中的下标。 */
    private static final int SERVICE_POSITION = 1;
    
    /** 集群名在缓存键各段中的下标。 */
    private static final int CLUSTER_POSITION = 2;
    
    /** 完整缓存键期望的分段数量。 */
    private static final int FILE_NAME_PARTS = 3;
    
    /** 服务端下发的原始 JSON 字符串，不参与序列化输出。 */
    @JsonIgnore
    private String jsonFromServer = EMPTY;
    
    private static final String EMPTY = "";
    
    private static final String DEFAULT_CHARSET = "UTF-8";
    
    /** 服务名。 */
    private String name;
    
    /** 分组名。 */
    private String groupName;
    
    /** 订阅的集群名（可为空或逗号分隔）。 */
    private String clusters;
    
    /** 本地缓存过期时间（毫秒）。 */
    private long cacheMillis = 1000L;
    
    /** 服务实例列表。 */
    private List<Instance> hosts = new ArrayList<>();
    
    /** 最近一次刷新时间戳（毫秒）。 */
    private long lastRefTime = 0L;
    
    /** 实例列表校验和，用于与服务端比对是否变更。 */
    private String checksum = "";
    
    /** 是否订阅全部 IP（不区分集群）。 */
    private volatile boolean allIps = false;
    
    /** 是否已达到健康实例保护阈值。 */
    private volatile boolean reachProtectionThreshold = false;
    
    /** 无参构造。 */
    public ServiceInfo() {
    }
    
    /** 是否订阅全部 IP。 */
    public boolean isAllIps() {
        return allIps;
    }
    
    /** 设置是否订阅全部 IP。 */
    public void setAllIps(boolean allIps) {
        this.allIps = allIps;
    }
    
    /**
     * 从缓存键解析构造 {@link ServiceInfo}，键格式为 {@code groupName@@name@@clusters}。
     *
     * <p>供 {@code DiskCache.read(String)} 与 {@code FailoverReactor.FailoverFileReader} 使用；
     * {@code groupName} 不可为空，{@code clusters} 可为空。</p>
     *
     * @param key 缓存键字符串
     */
    public ServiceInfo(final String key) {
        String[] keys = key.split(Constants.SERVICE_INFO_SPLITER);
        if (keys.length >= FILE_NAME_PARTS) {
            this.groupName = keys[GROUP_POSITION];
            this.name = keys[SERVICE_POSITION];
            this.clusters = keys[CLUSTER_POSITION];
        } else if (keys.length == CLUSTER_POSITION) {
            this.groupName = keys[GROUP_POSITION];
            this.name = keys[SERVICE_POSITION];
        } else {
            // 防御性编程：无法解析出 groupName 时抛出异常
            throw new IllegalArgumentException(
                "Can't parse out 'groupName',but it must not be null!");
        }
    }
    
    /**
     * 按服务名与集群名构造。
     *
     * @param name     服务名
     * @param clusters 集群名
     */
    public ServiceInfo(String name, String clusters) {
        this.name = name;
        this.clusters = clusters;
    }
    
    /** 返回当前实例列表中的 IP 数量。 */
    public int ipCount() {
        return hosts.size();
    }
    
    /** 判断本地缓存是否已过期（超过 {@link #cacheMillis} 未刷新）。 */
    public boolean expired() {
        return System.currentTimeMillis() - lastRefTime > cacheMillis;
    }
    
    /** 替换实例列表。 */
    public void setHosts(List<Instance> hosts) {
        this.hosts = hosts;
    }
    
    /** 追加单个实例。 */
    public void addHost(Instance host) {
        hosts.add(host);
    }
    
    /** 批量追加实例。 */
    public void addAllHosts(List<? extends Instance> hosts) {
        this.hosts.addAll(hosts);
    }
    
    /** 返回实例列表的防御性副本。 */
    public List<Instance> getHosts() {
        return new ArrayList<>(hosts);
    }
    
    /** 实例列表是否有效（非 null）。 */
    public boolean isValid() {
        return hosts != null;
    }
    
    /** 获取服务名。 */
    public String getName() {
        return name;
    }
    
    /** 设置服务名。 */
    public void setName(String name) {
        this.name = name;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 设置最近刷新时间戳。 */
    public void setLastRefTime(long lastRefTime) {
        this.lastRefTime = lastRefTime;
    }
    
    /** 获取最近刷新时间戳。 */
    public long getLastRefTime() {
        return lastRefTime;
    }
    
    /** 获取集群名。 */
    public String getClusters() {
        return clusters;
    }
    
    /** 设置集群名。 */
    public void setClusters(String clusters) {
        this.clusters = clusters;
    }
    
    /** 获取缓存过期时间（毫秒）。 */
    public long getCacheMillis() {
        return cacheMillis;
    }
    
    /** 设置缓存过期时间（毫秒）。 */
    public void setCacheMillis(long cacheMillis) {
        this.cacheMillis = cacheMillis;
    }
    
    /**
     * 判断当前服务信息是否可用于流量路由。
     *
     * <p>订阅全部 IP 时直接有效；否则需存在至少一个健康且权重大于 0 的实例。</p>
     *
     * @return 有效返回 {@code true}，否则 {@code false}
     */
    public boolean validate() {
        if (isAllIps()) {
            return true;
        }
        
        if (hosts == null) {
            return false;
        }
        
        boolean existValidHosts = false;
        for (Instance host : hosts) {
            if (host.isHealthy() && host.getWeight() > 0) {
                existValidHosts = true;
                break;
            }
        }
        return existValidHosts;
    }
    
    /** 获取服务端下发的原始 JSON。 */
    @JsonIgnore
    public String getJsonFromServer() {
        return jsonFromServer;
    }
    
    /** 设置服务端下发的原始 JSON。 */
    public void setJsonFromServer(String jsonFromServer) {
        this.jsonFromServer = jsonFromServer;
    }
    
    /** 生成含分组与集群的缓存键。 */
    @JsonIgnore
    public String getKey() {
        String serviceName = getGroupedServiceName();
        return getKey(serviceName, clusters);
    }
    
    /**
     * 根据服务名与集群名拼接缓存键。
     *
     * @param name     服务名（可含分组前缀）
     * @param clusters 集群名，为空则省略
     * @return 缓存键字符串
     */
    @JsonIgnore
    public static String getKey(String name, String clusters) {
        if (!isEmpty(clusters)) {
            return name + Constants.SERVICE_INFO_SPLITER + clusters;
        }
        return name;
    }
    
    /** 生成不含集群段的缓存键（即分组@@服务名）。 */
    @JsonIgnore
    public String getKeyWithoutClusters() {
        return getGroupedServiceName();
    }
    
    /** 生成 URL 编码后的缓存键，便于磁盘文件名使用。 */
    @JsonIgnore
    public String getKeyEncoded() {
        String serviceName = getGroupedServiceName();
        try {
            serviceName = URLEncoder.encode(serviceName, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException ignored) {
        }
        return getKey(serviceName, clusters);
    }
    
    /** 拼接 {@code groupName@@name} 格式的分组服务名。 */
    private String getGroupedServiceName() {
        String serviceName = this.name;
        if (!isEmpty(groupName) && !serviceName.contains(Constants.SERVICE_INFO_SPLITER)) {
            serviceName = groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
        }
        return serviceName;
    }
    
    /**
     * 从缓存键解析并创建 {@link ServiceInfo}。
     *
     * @param key 缓存键
     * @return 新的服务信息对象
     */
    public static ServiceInfo fromKey(final String key) {
        return new ServiceInfo(key);
    }
    
    @Override
    public String toString() {
        return getKey();
    }
    
    /** 获取实例列表校验和。 */
    public String getChecksum() {
        return checksum;
    }
    
    /** 设置实例列表校验和。 */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /** 判断字符串是否为空或 null。 */
    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /** 是否已达到健康实例保护阈值。 */
    public boolean isReachProtectionThreshold() {
        return reachProtectionThreshold;
    }
    
    /** 设置是否达到健康实例保护阈值。 */
    public void setReachProtectionThreshold(boolean reachProtectionThreshold) {
        this.reachProtectionThreshold = reachProtectionThreshold;
    }
    
    /** 深拷贝当前服务信息及全部实例。 */
    @Override
    public ServiceInfo clone() {
        ServiceInfo cloned = new ServiceInfo();
        cloned.jsonFromServer = this.jsonFromServer;
        cloned.name = this.name;
        cloned.groupName = this.groupName;
        cloned.clusters = this.clusters;
        cloned.cacheMillis = this.cacheMillis;
        cloned.lastRefTime = this.lastRefTime;
        cloned.checksum = this.checksum;
        cloned.allIps = this.allIps;
        cloned.reachProtectionThreshold = this.reachProtectionThreshold;
        cloned.hosts = new ArrayList<>();
        
        if (this.hosts != null) {
            for (Instance host : this.hosts) {
                Instance clonedHost = new Instance();
                clonedHost.setInstanceId(host.getInstanceId());
                clonedHost.setIp(host.getIp());
                clonedHost.setPort(host.getPort());
                clonedHost.setWeight(host.getWeight());
                clonedHost.setHealthy(host.isHealthy());
                clonedHost.setEnabled(host.isEnabled());
                clonedHost.setEphemeral(host.isEphemeral());
                clonedHost.setClusterName(host.getClusterName());
                clonedHost.setServiceName(host.getServiceName());
                
                if (host.getMetadata() != null) {
                    Map<String, String> clonedMetadata = new HashMap<>(host.getMetadata());
                    clonedHost.setMetadata(clonedMetadata);
                }
                
                cloned.hosts.add(clonedHost);
            }
        }
        
        return cloned;
    }
}
