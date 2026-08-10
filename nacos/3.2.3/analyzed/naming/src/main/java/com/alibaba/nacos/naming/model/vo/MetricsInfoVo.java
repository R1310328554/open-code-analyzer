/*
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
 */

package com.alibaba.nacos.naming.model.vo;

import com.alibaba.nacos.api.naming.pojo.maintainer.MetricsInfo;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Naming 模块监控指标视图对象（旧版）。
 *
 * <p>汇总服务数、实例数、订阅数、各类客户端数量及 CPU/负载/内存等运行时指标；已废弃，请使用 {@link com.alibaba.nacos.api.naming.pojo.maintainer.MetricsInfo}。</p>
 *
 * @author dongyafei
 * @date 2022/9/15
 * @deprecated use {@link com.alibaba.nacos.api.naming.pojo.maintainer.MetricsInfo} replaced.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated
public class MetricsInfoVo implements Serializable {
    
    private static final long serialVersionUID = -5064297490423743871L;
    
    /** 节点运行状态（如 UP/DOWN）。 */
    private String status;
    
    /** 当前注册的服务总数。 */
    private Integer serviceCount;
    
    /** 当前注册的实例总数。 */
    private Integer instanceCount;
    
    /** 订阅关系总数。 */
    private Integer subscribeCount;
    
    /** 客户端连接总数。 */
    private Integer clientCount;
    
    /** 基于长连接的 v2 客户端数量。 */
    private Integer connectionBasedClientCount;
    
    /** 临时 IP:Port 客户端数量（v1 协议）。 */
    private Integer ephemeralIpPortClientCount;
    
    /** 持久 IP:Port 客户端数量（v1 协议）。 */
    private Integer persistentIpPortClientCount;
    
    /** 本节点负责的客户端数量（Distro 分片）。 */
    private Integer responsibleClientCount;
    
    /** CPU 使用率（已从新 API 移除）。 */
    private Float cpu;
    
    /** 系统负载（已从新 API 移除）。 */
    private Float load;
    
    /** 内存使用率（已从新 API 移除）。 */
    private Float mem;
    
    public MetricsInfoVo() {
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getServiceCount() {
        return serviceCount;
    }
    
    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
    
    public Integer getInstanceCount() {
        return instanceCount;
    }
    
    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }
    
    public Integer getSubscribeCount() {
        return subscribeCount;
    }
    
    public void setSubscribeCount(Integer subscribeCount) {
        this.subscribeCount = subscribeCount;
    }
    
    public Integer getClientCount() {
        return clientCount;
    }
    
    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }
    
    public Integer getConnectionBasedClientCount() {
        return connectionBasedClientCount;
    }
    
    public void setConnectionBasedClientCount(Integer connectionBasedClientCount) {
        this.connectionBasedClientCount = connectionBasedClientCount;
    }
    
    public Integer getEphemeralIpPortClientCount() {
        return ephemeralIpPortClientCount;
    }
    
    public void setEphemeralIpPortClientCount(Integer ephemeralIpPortClientCount) {
        this.ephemeralIpPortClientCount = ephemeralIpPortClientCount;
    }
    
    public Integer getPersistentIpPortClientCount() {
        return persistentIpPortClientCount;
    }
    
    public void setPersistentIpPortClientCount(Integer persistentIpPortClientCount) {
        this.persistentIpPortClientCount = persistentIpPortClientCount;
    }
    
    public Integer getResponsibleClientCount() {
        return responsibleClientCount;
    }
    
    public void setResponsibleClientCount(Integer responsibleClientCount) {
        this.responsibleClientCount = responsibleClientCount;
    }
    
    public Float getCpu() {
        return cpu;
    }
    
    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }
    
    public Float getLoad() {
        return load;
    }
    
    public void setLoad(Float load) {
        this.load = load;
    }
    
    public Float getMem() {
        return mem;
    }
    
    public void setMem(Float mem) {
        this.mem = mem;
    }
    
    /**
     * 转换为新版 {@link MetricsInfo}，不含 CPU/负载/内存字段（采集开销大且精度低）。
     *
     * @param metricsInfoVo 旧版指标视图对象
     * @return 新版 MetricsInfo 实例
     */
    public static MetricsInfo toNewMetricsInfo(MetricsInfoVo metricsInfoVo) {
        MetricsInfo metricsInfo = new MetricsInfo();
        metricsInfo.setStatus(metricsInfoVo.getStatus());
        metricsInfo.setServiceCount(metricsInfoVo.getServiceCount());
        metricsInfo.setInstanceCount(metricsInfoVo.getInstanceCount());
        metricsInfo.setSubscribeCount(metricsInfoVo.getSubscribeCount());
        metricsInfo.setClientCount(metricsInfoVo.getClientCount());
        metricsInfo.setConnectionBasedClientCount(metricsInfoVo.getConnectionBasedClientCount());
        metricsInfo.setEphemeralIpPortClientCount(metricsInfoVo.getEphemeralIpPortClientCount());
        metricsInfo.setPersistentIpPortClientCount(metricsInfoVo.getPersistentIpPortClientCount());
        metricsInfo.setResponsibleClientCount(metricsInfoVo.getResponsibleClientCount());
        return metricsInfo;
    }
}
