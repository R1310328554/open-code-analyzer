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

import com.alibaba.nacos.common.notify.NotifyCenter;
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
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.event.service.ServiceEvent;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.constants.Constants;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 实例元数据 CP 一致性处理器。
 *
 * <p>注册到 Raft CP 协议，处理实例元数据的增删改写请求，并提供 {@link InstanceMetadataSnapshotOperation} 快照能力。</p>
 *
 * @author xiweng.yy
 */
@Component
public class InstanceMetadataProcessor extends RequestProcessor4CP {
    
    /** 命名元数据内存管理器。 */
    private final NamingMetadataManager namingMetadataManager;
    
    /** 元数据操作日志序列化器。 */
    private final Serializer serializer;
    
    /** {@link MetadataOperation}{@code <InstanceMetadata>} 反序列化类型。 */
    private final Type processType;
    
    /** 快照读写与 apply 操作共享的读写锁。 */
    private final ReentrantReadWriteLock lock;
    
    /** apply 阶段持有的读锁，与快照写互斥。 */
    private final ReentrantReadWriteLock.ReadLock readLock;
    
    @SuppressWarnings("unchecked")
    public InstanceMetadataProcessor(NamingMetadataManager namingMetadataManager,
        ProtocolManager protocolManager) {
        this.namingMetadataManager = namingMetadataManager;
        this.serializer = SerializeFactory.getDefault();
        this.processType = TypeUtils.parameterize(MetadataOperation.class, InstanceMetadata.class);
        this.lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        protocolManager.getCpProtocol().addRequestProcessors(Collections.singletonList(this));
    }
    
    /** 注册实例元数据快照操作。 */
    @Override
    public List<SnapshotOperation> loadSnapshotOperate() {
        return Collections
            .singletonList(new InstanceMetadataSnapshotOperation(namingMetadataManager, lock));
    }
    
    @Override
    public Response onRequest(ReadRequest request) {
        return null;
    }
    
    /** 应用实例元数据写请求（ADD/CHANGE/DELETE）。 */
    @Override
    public Response onApply(WriteRequest request) {
        readLock.lock();
        try {
            MetadataOperation<InstanceMetadata> op =
                serializer.deserialize(request.getData().toByteArray(), processType);
            switch (DataOperation.valueOf(request.getOperation())) {
                case ADD:
                case CHANGE:
                    updateInstanceMetadata(op);
                    break;
                case DELETE:
                    deleteInstanceMetadata(op);
                    break;
                default:
                    return Response.newBuilder().setSuccess(false)
                        .setErrMsg("Unsupported operation " + request.getOperation()).build();
            }
            return Response.newBuilder().setSuccess(true).build();
        } catch (Exception e) {
            Loggers.RAFT.error("onApply {} instance metadata operation failed. ",
                request.getOperation(), e);
            String errorMessage = null == e.getMessage() ? e.getClass().getName() : e.getMessage();
            return Response.newBuilder().setSuccess(false).setErrMsg(errorMessage).build();
        } finally {
            readLock.unlock();
        }
    }
    
    private void updateInstanceMetadata(MetadataOperation<InstanceMetadata> op) {
        Service service = Service.newService(op.getNamespace(), op.getGroup(), op.getServiceName());
        service = ServiceManager.getInstance().getSingleton(service);
        namingMetadataManager.updateInstanceMetadata(service, op.getTag(), op.getMetadata());
        NotifyCenter.publishEvent(new ServiceEvent.ServiceChangedEvent(service,
            com.alibaba.nacos.api.common.Constants.ServiceChangedType.INSTANCE_CHANGED, true));
    }
    
    private void deleteInstanceMetadata(MetadataOperation<InstanceMetadata> op) {
        Service service = Service.newService(op.getNamespace(), op.getGroup(), op.getServiceName());
        service = ServiceManager.getInstance().getSingleton(service);
        namingMetadataManager.removeInstanceMetadata(service, op.getTag());
    }
    
    /** 返回 CP 处理器分组名 {@link Constants#INSTANCE_METADATA}。 */
    @Override
    public String group() {
        return Constants.INSTANCE_METADATA;
    }
}
