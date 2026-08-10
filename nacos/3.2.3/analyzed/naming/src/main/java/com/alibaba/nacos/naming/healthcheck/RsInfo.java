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

package com.alibaba.nacos.naming.healthcheck;

import com.alibaba.nacos.common.utils.JacksonUtils;

import java.util.Map;

/**
 * 服务端实例指标与注册信息载体。
 *
 * <p>客户端心跳或健康检查上报时携带负载、CPU、QPS 等运行时指标，以及 IP、端口、集群、权重等注册属性；序列化为 JSON 供日志与事件处理使用。</p>
 *
 * @author nacos
 */
public class RsInfo {
    
    /** 系统负载指标。 */
    private double load;
    
    /** CPU 使用率。 */
    private double cpu;
    
    /** 平均响应时间（RT）。 */
    private double rt;
    
    /** 每秒查询数（QPS）。 */
    private double qps;
    
    /** 内存使用率。 */
    private double mem;
    
    /** 实例监听端口。 */
    private int port;
    
    /** 实例 IP 地址。 */
    private String ip;
    
    /** 服务名（可含分组前缀）。 */
    private String serviceName;
    
    /** 访问密钥标识（可选）。 */
    private String ak;
    
    /** 所属集群名。 */
    private String cluster;
    
    /** 实例权重。 */
    private double weight;
    
    /** 是否为临时实例，默认真。 */
    private boolean ephemeral = true;
    
    /** 实例元数据键值对。 */
    private Map<String, String> metadata;
    
    /** 返回服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 设置服务名。 */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /** 返回访问密钥。 */
    public String getAk() {
        return ak;
    }
    
    /** 设置访问密钥。 */
    public void setAk(String ak) {
        this.ak = ak;
    }
    
    /** 返回集群名。 */
    public String getCluster() {
        return cluster;
    }
    
    /** 设置集群名。 */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    /** 返回实例 IP。 */
    public String getIp() {
        return ip;
    }
    
    /** 设置实例 IP。 */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /** 返回监听端口。 */
    public int getPort() {
        return port;
    }
    
    /** 设置监听端口。 */
    public void setPort(int port) {
        this.port = port;
    }
    
    /** 返回负载指标。 */
    public double getLoad() {
        return load;
    }
    
    /** 设置负载指标。 */
    public void setLoad(double load) {
        this.load = load;
    }
    
    /** 返回 CPU 使用率。 */
    public double getCpu() {
        return cpu;
    }
    
    /** 设置 CPU 使用率。 */
    public void setCpu(double cpu) {
        this.cpu = cpu;
    }
    
    /** 返回平均响应时间。 */
    public double getRt() {
        return rt;
    }
    
    /** 设置平均响应时间。 */
    public void setRt(double rt) {
        this.rt = rt;
    }
    
    /** 返回 QPS。 */
    public double getQps() {
        return qps;
    }
    
    /** 设置 QPS。 */
    public void setQps(double qps) {
        this.qps = qps;
    }
    
    /** 返回内存使用率。 */
    public double getMem() {
        return mem;
    }
    
    /** 设置内存使用率。 */
    public void setMem(double mem) {
        this.mem = mem;
    }
    
    /** 返回实例权重。 */
    public double getWeight() {
        return weight;
    }
    
    /** 设置实例权重。 */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /** 是否为临时实例。 */
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    /** 设置临时实例标志。 */
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }
    
    /** 返回元数据映射。 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /** 设置元数据映射。 */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /** 序列化为 JSON 字符串。 */
    @Override
    public String toString() {
        return JacksonUtils.toJson(this);
    }
}
