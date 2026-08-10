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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.trace.event.naming.HealthStateChangeTraceEvent;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.event.client.ClientEvent;
import com.alibaba.nacos.naming.core.v2.event.service.ServiceEvent;
import com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Optional;

/**
 * 不健康实例心跳检查器：心跳超时时将实例健康状态置为 false。
 *
 * <p>Mark these instances healthy status {@code false} if beat time out.</p>
 * <p>超时阈值优先取自实例元数据，其次实例扩展字段，最后使用全局默认值。</p>
 *
 * @author xiweng.yy
 */
public class UnhealthyInstanceChecker implements InstanceBeatChecker {
    
    /** 仅当实例当前健康且心跳已超时时触发状态变更。 */
    @Override
    public void doCheck(Client client, Service service, HealthCheckInstancePublishInfo instance) {
        if (instance.isHealthy() && isUnhealthy(service, instance)) {
            changeHealthyStatus(client, service, instance);
        }
    }
    
    /** 比较当前时间与上次心跳时间是否超过超时阈值。 */
    private boolean isUnhealthy(Service service, HealthCheckInstancePublishInfo instance) {
        long beatTimeout = getTimeout(service, instance);
        return System.currentTimeMillis() - instance.getLastHeartBeatTime() > beatTimeout;
    }
    
    /** 解析心跳超时毫秒数：元数据 → 扩展字段 → 默认常量。 */
    private long getTimeout(Service service, InstancePublishInfo instance) {
        Optional<Object> timeout = getTimeoutFromMetadata(service, instance);
        if (!timeout.isPresent()) {
            timeout = Optional.ofNullable(
                instance.getExtendDatum().get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT));
        }
        return timeout.map(ConvertUtils::toLong).orElse(Constants.DEFAULT_HEART_BEAT_TIMEOUT);
    }
    
    /** 从 {@link InstanceMetadata} 扩展数据读取 HEART_BEAT_TIMEOUT。 */
    private Optional<Object> getTimeoutFromMetadata(Service service, InstancePublishInfo instance) {
        Optional<InstanceMetadata> instanceMetadata =
            ApplicationUtils.getBean(NamingMetadataManager.class)
                .getInstanceMetadata(service, instance.getMetadataId());
        return instanceMetadata.map(
            metadata -> metadata.getExtendData().get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT));
    }
    
    /** 置 unhealthy、写事件日志并发布服务变更、客户端变更与链路追踪事件。 */
    private void changeHealthyStatus(Client client, Service service,
        HealthCheckInstancePublishInfo instance) {
        instance.setHealthy(false);
        Loggers.EVT_LOG
            .info("{POS} {IP-DISABLED} valid: {}:{}@{}@{}, region: {}, msg: client last beat: {}",
                instance.getIp(),
                instance.getPort(), instance.getCluster(), service.getName(),
                UtilsAndCommons.LOCALHOST_SITE,
                instance.getLastHeartBeatTime());
        NotifyCenter.publishEvent(
            new ServiceEvent.ServiceChangedEvent(service, Constants.ServiceChangedType.HEART_BEAT));
        NotifyCenter.publishEvent(new ClientEvent.ClientChangedEvent(client));
        NotifyCenter.publishEvent(new HealthStateChangeTraceEvent(System.currentTimeMillis(),
            service.getNamespace(), service.getGroup(), service.getName(), instance.getIp(),
            instance.getPort(),
            false, "client_beat"));
    }
}
