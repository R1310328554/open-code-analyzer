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
import com.alibaba.nacos.common.trace.DeregisterInstanceReason;
import com.alibaba.nacos.common.trace.event.naming.DeregisterInstanceTraceEvent;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.event.client.ClientOperationEvent;
import com.alibaba.nacos.naming.core.v2.event.metadata.MetadataEvent;
import com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Optional;

/**
 * 过期实例心跳检查器。
 *
 * <p>当全局配置允许且距最后心跳超过超时阈值时，自动注销该实例并发布相关事件。</p>
 *
 * @author xiweng.yy
 */
public class ExpiredInstanceChecker implements InstanceBeatChecker {
    
    /** 若实例心跳超时则触发自动删除。 */
    @Override
    public void doCheck(Client client, Service service, HealthCheckInstancePublishInfo instance) {
        boolean expireInstance = ApplicationUtils.getBean(GlobalConfig.class).isExpireInstance();
        if (expireInstance && isExpireInstance(service, instance)) {
            deleteIp(client, service, instance);
        }
    }
    
    /** 判断实例是否已超过删除超时时间。 */
    private boolean isExpireInstance(Service service, HealthCheckInstancePublishInfo instance) {
        long deleteTimeout = getTimeout(service, instance);
        return System.currentTimeMillis() - instance.getLastHeartBeatTime() > deleteTimeout;
    }
    
    /** 从元数据或扩展字段解析 IP 删除超时毫秒数。 */
    private long getTimeout(Service service, InstancePublishInfo instance) {
        Optional<Object> timeout = getTimeoutFromMetadata(service, instance);
        if (!timeout.isPresent()) {
            timeout = Optional
                .ofNullable(instance.getExtendDatum().get(PreservedMetadataKeys.IP_DELETE_TIMEOUT));
        }
        return timeout.map(ConvertUtils::toLong).orElse(Constants.DEFAULT_IP_DELETE_TIMEOUT);
    }
    
    /** 从命名元数据管理器读取超时配置。 */
    private Optional<Object> getTimeoutFromMetadata(Service service, InstancePublishInfo instance) {
        Optional<InstanceMetadata> instanceMetadata =
            ApplicationUtils.getBean(NamingMetadataManager.class)
                .getInstanceMetadata(service, instance.getMetadataId());
        return instanceMetadata
            .map(metadata -> metadata.getExtendData().get(PreservedMetadataKeys.IP_DELETE_TIMEOUT));
    }
    
    /** 移除过期实例并发布注销、元数据与追踪事件。 */
    private void deleteIp(Client client, Service service, InstancePublishInfo instance) {
        Loggers.SRV_LOG.info("[AUTO-DELETE-IP] service: {}, ip: {}", service.toString(),
            JacksonUtils.toJson(instance));
        client.removeServiceInstance(service);
        NotifyCenter.publishEvent(
            new ClientOperationEvent.ClientDeregisterServiceEvent(service, client.getClientId()));
        NotifyCenter.publishEvent(
            new MetadataEvent.InstanceMetadataEvent(service, instance.getMetadataId(), true));
        NotifyCenter.publishEvent(new DeregisterInstanceTraceEvent(System.currentTimeMillis(), "",
            false, DeregisterInstanceReason.HEARTBEAT_EXPIRE, service.getNamespace(),
            service.getGroup(),
            service.getName(), instance.getIp(), instance.getPort()));
    }
}
