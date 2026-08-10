/*
 *
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.istio.model;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.protobuf.UInt32Value;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.Locality;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.endpoint.v3.Endpoint;
import io.envoyproxy.envoy.config.endpoint.v3.LbEndpoint;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.alibaba.nacos.istio.util.IstioCrdUtil.ISTIO_HOSTNAME;

/**
 * 将 Nacos 注册实例转换为 Istio/Envoy 可识别的负载均衡端点视图。
 *
 * <p>封装 {@link Instance} 元数据，按需构建 {@link LbEndpoint} 与 {@link Locality}。</p>
 *
 * @author RocketEngine26
 * @date 2022/8/9 10:29
 */
public class IstioEndpoint {
    /** 缓存的 Envoy {@link LbEndpoint}，按需懒构建。 */
    private LbEndpoint lbEndpoint;
    
    /** 原始 Nacos 注册实例。 */
    private Instance instance;
    
    /** 地域/可用区/子区三元组，用于 locality 负载均衡。 */
    private Locality locality;
    
    /** 服务协议（http/grpc），由实例 metadata 推导。 */
    private String protocol;
    
    /** Istio 主机名，来自 metadata {@link com.alibaba.nacos.istio.util.IstioCrdUtil#ISTIO_HOSTNAME}。 */
    private String hostName;
    
    /** Nacos 集群名，写入 WorkloadEntry 标签。 */
    private String clusterName;
    
    /**
     * 从 Nacos 实例构造 Istio 端点，解析协议、主机名与地域信息。
     *
     * @param instance Nacos 命名服务实例
     */
    public IstioEndpoint(Instance instance) {
        this.instance = instance;
        this.hostName = StringUtils.isNotEmpty(instance.getMetadata().get(ISTIO_HOSTNAME)) ? instance.getMetadata().get(ISTIO_HOSTNAME) : "";
        this.clusterName = StringUtils.isNotEmpty(instance.getClusterName()) ? instance.getClusterName() : "";
        
        if (StringUtils.isNotEmpty(instance.getMetadata().get("protocol"))) {
            this.protocol = instance.getMetadata().get("protocol");
        
            // triple/tri 为 Dubbo3 协议别名，统一映射为 grpc
            if ("triple".equals(this.protocol) || "tri".equals(this.protocol)) {
                this.protocol = "grpc";
            }
        } else {
            this.protocol = "http";
        }
        
        buildLocality();
    }
    
    /** 从实例 metadata 的 region/zone/subzone 构建 Envoy Locality。 */
    private void buildLocality() {
        String region = instance.getMetadata().getOrDefault("region", "");
        String zone = instance.getMetadata().getOrDefault("zone", "");
        String subzone = instance.getMetadata().getOrDefault("subzone", "");
        
        this.locality = Locality.newBuilder().setRegion(region).setZone(zone).setSubZone(subzone).build();
    }
    
    /** 按 IP、端口与权重构建 Envoy {@link LbEndpoint}。 */
    private LbEndpoint buildLbEndpoint() {
        Address adder = Address.newBuilder().setSocketAddress(SocketAddress.newBuilder().setAddress(instance.getIp())
                .setPortValue(this.instance.getPort()).setProtocol(SocketAddress.Protocol.TCP).build()).build();
        this.lbEndpoint = LbEndpoint.newBuilder().setLoadBalancingWeight(UInt32Value.newBuilder().setValue(
                (int) this.instance.getWeight())).setEndpoint(Endpoint.newBuilder().setAddress(adder).build()).build();
        
        return this.lbEndpoint;
    }
    
    public Map<String, String> getLabels() {
        return instance.getMetadata();
    }
    
    public String getAdder() {
        return instance.getIp();
    }
    
    public LbEndpoint getLbEndpoint() {
        return buildLbEndpoint();
    }
    
    public String getStringLocality() {
        return locality.getRegion() + "." +  locality.getZone() + "." + locality.getSubZone();
    }
    
    public Locality getLocality() {
        return locality;
    }
    
    public int getPort() {
        return instance.getPort();
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public int getWeight() {
        return (int) instance.getWeight();
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public String getClusterName() {
        return clusterName;
    }
    
    public boolean isHealthy() {
        return instance.isHealthy();
    }
    
    public boolean isEnabled() {
        return instance.isEnabled();
    }
}