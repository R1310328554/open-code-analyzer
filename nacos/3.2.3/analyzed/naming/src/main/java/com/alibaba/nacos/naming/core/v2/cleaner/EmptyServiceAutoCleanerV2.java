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

package com.alibaba.nacos.naming.core.v2.cleaner;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.event.metadata.MetadataEvent;
import com.alibaba.nacos.naming.core.v2.index.ClientServiceIndexesManager;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * V2 空服务自动清理器。
 *
 * <p>定期扫描无注册客户端且超过空闲阈值的服务单例，移除索引、存储并发布元数据删除事件。</p>
 *
 * @author xiweng.yy
 */
@Component
public class EmptyServiceAutoCleanerV2 extends AbstractNamingCleaner {
    
    /** 清理器类型标识。 */
    private static final String EMPTY_SERVICE = "emptyService";
    
    /** 客户端-服务索引管理器。 */
    private final ClientServiceIndexesManager clientServiceIndexesManager;
    
    /** 服务实例数据存储。 */
    private final ServiceStorage serviceStorage;
    
    /** 注册定时清理任务到全局执行器。 */
    public EmptyServiceAutoCleanerV2(ClientServiceIndexesManager clientServiceIndexesManager,
        ServiceStorage serviceStorage) {
        this.clientServiceIndexesManager = clientServiceIndexesManager;
        this.serviceStorage = serviceStorage;
        GlobalExecutor.scheduleExpiredClientCleaner(this, TimeUnit.SECONDS.toMillis(30),
            GlobalConfig.getEmptyServiceCleanInterval(), TimeUnit.MILLISECONDS);
        
    }
    
    @Override
    public String getType() {
        return EMPTY_SERVICE;
    }
    
    @Override
    public void doClean() {
        ServiceManager serviceManager = ServiceManager.getInstance();
        // 并行流开启阈值：服务数超过该值时使用 parallelStream
        int parallelSize = 100;
        
        for (String each : serviceManager.getAllNamespaces()) {
            Set<Service> services = serviceManager.getSingletons(each);
            Stream<Service> stream =
                services.size() > parallelSize ? services.parallelStream() : services.stream();
            stream.forEach(this::cleanEmptyService);
        }
    }
    
    /** 清理单个无客户端且已超时的空服务。 */
    private void cleanEmptyService(Service service) {
        Collection<String> registeredService =
            clientServiceIndexesManager.getAllClientsRegisteredService(service);
        if (registeredService.isEmpty() && isTimeExpired(service)) {
            Loggers.SRV_LOG.warn("namespace : {}, [{}] services are automatically cleaned",
                service.getNamespace(),
                service.getGroupedServiceName());
            clientServiceIndexesManager.removePublisherIndexesByEmptyService(service);
            ServiceManager.getInstance().removeSingleton(service);
            serviceStorage.removeData(service);
            NotifyCenter.publishEvent(new MetadataEvent.ServiceMetadataEvent(service, true));
        }
    }
    
    /** 判断服务自上次更新起是否已超过空服务过期时间。 */
    private boolean isTimeExpired(Service service) {
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - service.getLastUpdatedTime() >= GlobalConfig
            .getEmptyServiceExpiredTime();
    }
}
