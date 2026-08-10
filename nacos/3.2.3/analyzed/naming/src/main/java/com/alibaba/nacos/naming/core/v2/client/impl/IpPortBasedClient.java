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

package com.alibaba.nacos.naming.core.v2.client.impl;

import com.alibaba.nacos.naming.core.v2.client.AbstractClient;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.HealthCheckReactor;
import com.alibaba.nacos.naming.healthcheck.heartbeat.ClientBeatCheckTaskV2;
import com.alibaba.nacos.naming.healthcheck.v2.HealthCheckTaskV2;
import com.alibaba.nacos.naming.misc.ClientConfig;
import com.alibaba.nacos.naming.monitor.MetricsMonitor;

import java.util.Collection;

/**
 * 基于 IP 与端口的 Nacos 命名客户端。
 *
 * <p>以注册地址抽象模拟 TCP 会话客户端，适用于 HTTP/Open API 注册场景。</p>
 *
 * @author xiweng.yy
 */
public class IpPortBasedClient extends AbstractClient {
    
    /** 客户端 ID 中地址与 ephemeral 标志的分隔符。 */
    public static final String ID_DELIMITER = "#";
    
    /** 格式为 {@code address#ephemeral} 的客户端标识。 */
    private final String clientId;
    
    /** 是否为临时实例客户端。 */
    private final boolean ephemeral;
    
    /** Distro 分片负责节点标识（clientId 中 # 前的地址部分）。 */
    private final String responsibleId;
    
    private ClientBeatCheckTaskV2 beatCheckTask;
    
    private HealthCheckTaskV2 healthCheckTaskV2;
    
    public IpPortBasedClient(String clientId, boolean ephemeral) {
        this(clientId, ephemeral, null);
    }
    
    public IpPortBasedClient(String clientId, boolean ephemeral, Long revision) {
        super(revision);
        this.ephemeral = ephemeral;
        this.clientId = clientId;
        this.responsibleId = getResponsibleTagFromId();
    }
    
    private String getResponsibleTagFromId() {
        int index = clientId.indexOf(IpPortBasedClient.ID_DELIMITER);
        return clientId.substring(0, index);
    }
    
    /** 根据地址与 ephemeral 标志构造标准 clientId。 */
    public static String getClientId(String address, boolean ephemeral) {
        return address + ID_DELIMITER + ephemeral;
    }
    
    @Override
    public String getClientId() {
        return clientId;
    }
    
    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }
    
    public String getResponsibleId() {
        return responsibleId;
    }
    
    @Override
    public boolean addServiceInstance(Service service, InstancePublishInfo instancePublishInfo) {
        return super.addServiceInstance(service, parseToHealthCheckInstance(instancePublishInfo));
    }
    
    @Override
    public boolean isExpire(long currentTime) {
        return isEphemeral() && getAllPublishedService().isEmpty()
            && currentTime - getLastUpdatedTime() > ClientConfig
                .getInstance().getClientExpiredTime();
    }
    
    public Collection<InstancePublishInfo> getAllInstancePublishInfo() {
        return publishers.values();
    }
    
    @Override
    public void release() {
        super.release();
        if (ephemeral) {
            HealthCheckReactor.cancelCheck(beatCheckTask);
        } else {
            healthCheckTaskV2.setCancelled(true);
        }
    }
    
    private HealthCheckInstancePublishInfo parseToHealthCheckInstance(
        InstancePublishInfo instancePublishInfo) {
        HealthCheckInstancePublishInfo result;
        if (instancePublishInfo instanceof HealthCheckInstancePublishInfo) {
            result = (HealthCheckInstancePublishInfo) instancePublishInfo;
        } else {
            result = new HealthCheckInstancePublishInfo();
            result.setIp(instancePublishInfo.getIp());
            result.setPort(instancePublishInfo.getPort());
            result.setHealthy(instancePublishInfo.isHealthy());
            result.setCluster(instancePublishInfo.getCluster());
            result.setExtendDatum(instancePublishInfo.getExtendDatum());
        }
        if (!ephemeral) {
            result.initHealthCheck();
        }
        return result;
    }
    
    /** 初始化客户端：临时实例调度心跳检测，持久实例调度 V2 健康检查。 */
    /**
     * Init client.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public void init() {
        if (ephemeral) {
            beatCheckTask = new ClientBeatCheckTaskV2(this);
            HealthCheckReactor.scheduleCheck(beatCheckTask);
        } else {
            healthCheckTaskV2 = new HealthCheckTaskV2(this);
            HealthCheckReactor.scheduleCheck(healthCheckTaskV2);
        }
    }
    
    /** 静默写入实例到服务映射，不发布变更事件（用于快照恢复等场景）。 */
    /**
     * Purely put instance into service without publish events.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public void putServiceInstance(Service service, InstancePublishInfo instance) {
        if (null == publishers.put(service, parseToHealthCheckInstance(instance))) {
            MetricsMonitor.incrementInstanceCount();
        }
    }
}
