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

package com.alibaba.nacos.naming.core.v2.metadata;

import com.alibaba.nacos.common.utils.TypeUtils;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.RequestProcessor4CP;
import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.naming.constants.Constants;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 服务元数据 CP 协议处理器，通过 Raft 同步服务级元数据变更。
 *
 * <p>支持 ADD/CHANGE/DELETE 操作，并与 {@link ServiceStorage} 联动清理实例索引。</p>
 *
 * @author xiweng.yy
 */
@Component
public class ServiceMetadataProcessor extends RequestProcessor4CP {
    
    /** 命名元数据内存管理器。 */
    private final NamingMetadataManager namingMetadataManager;
    
    /** 服务实例存储，删除元数据时同步清理。 */
    private final ServiceStorage serviceStorage;
    
    private final Serializer serializer;
    
    private final Type processType;
    
    /** 快照读写锁，与快照操作共享。 */
    private final ReentrantReadWriteLock lock;
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    
    @SuppressWarnings("unchecked")
    public ServiceMetadataProcessor(NamingMetadataManager namingMetadataManager,
        ProtocolManager protocolManager,
        ServiceStorage serviceStorage) {
        this.namingMetadataManager = namingMetadataManager;
        this.serviceStorage = serviceStorage;
        this.serializer = SerializeFactory.getDefault();
        this.processType = TypeUtils.parameterize(MetadataOperation.class, ServiceMetadata.class);
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        protocolManager.getCpProtocol().addRequestProcessors(Collections.singletonList(this));
    }
    
    @Override
    public List<SnapshotOperation> loadSnapshotOperate() {
        return Collections
            .singletonList(new ServiceMetadataSnapshotOperation(namingMetadataManager, lock));
    }
    
    /** 服务元数据暂不支持只读请求。 */
    @Override
    public Response onRequest(ReadRequest request) {
        return null;
    }
    
    /** 应用 Raft 写请求：按操作类型增删改服务元数据。 */
    @Override
    public Response onApply(WriteRequest request) {
        readLock.lock();
        try {
            MetadataOperation<ServiceMetadata> op =
                serializer.deserialize(request.getData().toByteArray(), processType);
            switch (DataOperation.valueOf(request.getOperation())) {
                case ADD:
                    addClusterMetadataToService(op);
                    break;
                case CHANGE:
                    updateServiceMetadata(op);
                    break;
                case DELETE:
                    deleteServiceMetadata(op);
                    break;
                default:
                    return Response.newBuilder().setSuccess(false)
                        .setErrMsg("Unsupported operation " + request.getOperation()).build();
            }
            return Response.newBuilder().setSuccess(true).build();
        } catch (Exception e) {
            Loggers.RAFT.error("onApply {} service metadata operation failed. ",
                request.getOperation(), e);
            String errorMessage = null == e.getMessage() ? e.getClass().getName() : e.getMessage();
            return Response.newBuilder().setSuccess(false).setErrMsg(errorMessage).build();
        } finally {
            readLock.unlock();
        }
    }
    
    /** ADD：合并集群元数据到已有服务，或新建服务元数据。 */
    private void addClusterMetadataToService(MetadataOperation<ServiceMetadata> op) {
        Service service = Service
            .newService(op.getNamespace(), op.getGroup(), op.getServiceName(),
                op.getMetadata().isEphemeral());
        Optional<ServiceMetadata> currentMetadata =
            namingMetadataManager.getServiceMetadata(service);
        if (currentMetadata.isPresent()) {
            currentMetadata.get().getClusters().putAll(op.getMetadata().getClusters());
        } else {
            Service singleton = ServiceManager.getInstance().getSingleton(service);
            namingMetadataManager.updateServiceMetadata(singleton, op.getMetadata());
        }
    }
    
    /** CHANGE：合并新旧元数据后更新内存索引。 */
    private void updateServiceMetadata(MetadataOperation<ServiceMetadata> op) {
        Service service = Service
            .newService(op.getNamespace(), op.getGroup(), op.getServiceName(),
                op.getMetadata().isEphemeral());
        Optional<ServiceMetadata> currentMetadata =
            namingMetadataManager.getServiceMetadata(service);
        if (currentMetadata.isPresent()) {
            ServiceMetadata newMetadata = mergeMetadata(currentMetadata.get(), op.getMetadata());
            Service singleton = ServiceManager.getInstance().getSingleton(service);
            namingMetadataManager.updateServiceMetadata(singleton, newMetadata);
        } else {
            Service singleton = ServiceManager.getInstance().getSingleton(service);
            namingMetadataManager.updateServiceMetadata(singleton, op.getMetadata());
        }
    }
    
    /**
     * 合并元数据：不直接修改旧对象，避免并发读到半更新状态。
     *
     * <p>ephemeral 标志仅保留创建时的值，其余字段以新元数据为准。</p>
     *
     * <p>Ephemeral variable should only use the value the metadata create.
     *
     * @param oldMetadata old metadata
     * @param newMetadata new metadata
     * @return merged metadata
     */
    private ServiceMetadata mergeMetadata(ServiceMetadata oldMetadata,
        ServiceMetadata newMetadata) {
        ServiceMetadata result = new ServiceMetadata();
        result.setEphemeral(oldMetadata.isEphemeral());
        result.setClusters(oldMetadata.getClusters());
        result.setProtectThreshold(newMetadata.getProtectThreshold());
        result.setSelector(newMetadata.getSelector());
        result.setExtendData(newMetadata.getExtendData());
        return result;
    }
    
    /** DELETE：移除元数据、单例服务及实例存储数据。 */
    private void deleteServiceMetadata(MetadataOperation<ServiceMetadata> op) {
        Service service = Service.newService(op.getNamespace(), op.getGroup(), op.getServiceName());
        namingMetadataManager.removeServiceMetadata(service);
        Service removed = ServiceManager.getInstance().removeSingleton(service);
        if (removed != null) {
            service = removed;
        }
        serviceStorage.removeData(service);
    }
    
    @Override
    public String group() {
        return Constants.SERVICE_METADATA;
    }
}
