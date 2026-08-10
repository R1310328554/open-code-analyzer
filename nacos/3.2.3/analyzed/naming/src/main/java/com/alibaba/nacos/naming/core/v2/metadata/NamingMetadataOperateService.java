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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.constants.Constants;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;

/**
 * 命名元数据 CP 写操作门面。
 *
 * <p>将服务/实例/集群元数据的增删改封装为 Raft {@link WriteRequest} 并提交至 CP 协议层。</p>
 *
 * @author xiweng.yy
 */
@Component
public class NamingMetadataOperateService {
    
    /** CP 一致性协议，用于提交元数据写日志。 */
    private final CPProtocol cpProtocol;
    
    /** 元数据操作序列化器。 */
    private final Serializer serializer;
    
    public NamingMetadataOperateService(ProtocolManager protocolManager) {
        this.cpProtocol = protocolManager.getCpProtocol();
        this.serializer = SerializeFactory.getDefault();
    }
    
    /**
     * 通过 CP 协议更新服务元数据。
     *
     * @param service         目标服务
     * @param serviceMetadata 新的服务元数据
     */
    public void updateServiceMetadata(Service service, ServiceMetadata serviceMetadata) {
        MetadataOperation<ServiceMetadata> operation = buildMetadataOperation(service);
        operation.setMetadata(serviceMetadata);
        WriteRequest operationLog = WriteRequest.newBuilder().setGroup(Constants.SERVICE_METADATA)
            .setOperation(DataOperation.CHANGE.name())
            .setData(ByteString.copyFrom(serializer.serialize(operation)))
            .build();
        submitMetadataOperation(operationLog);
    }
    
    /**
     * 通过 CP 协议删除服务元数据。
     *
     * @param service 目标服务
     */
    public void deleteServiceMetadata(Service service) {
        MetadataOperation<ServiceMetadata> operation = buildMetadataOperation(service);
        WriteRequest operationLog = WriteRequest.newBuilder().setGroup(Constants.SERVICE_METADATA)
            .setOperation(DataOperation.DELETE.name())
            .setData(ByteString.copyFrom(serializer.serialize(operation)))
            .build();
        submitMetadataOperation(operationLog);
    }
    
    /**
     * 通过 CP 协议更新实例元数据。
     *
     * @param service          目标服务
     * @param metadataId       实例元数据 ID
     * @param instanceMetadata 新的实例元数据
     */
    public void updateInstanceMetadata(Service service, String metadataId,
        InstanceMetadata instanceMetadata) {
        MetadataOperation<InstanceMetadata> operation = buildMetadataOperation(service);
        operation.setTag(metadataId);
        operation.setMetadata(instanceMetadata);
        WriteRequest operationLog = WriteRequest.newBuilder().setGroup(Constants.INSTANCE_METADATA)
            .setOperation(DataOperation.CHANGE.name())
            .setData(ByteString.copyFrom(serializer.serialize(operation)))
            .build();
        submitMetadataOperation(operationLog);
    }
    
    /**
     * 通过 CP 协议删除实例元数据。
     *
     * @param service    目标服务
     * @param metadataId 实例元数据 ID
     */
    public void deleteInstanceMetadata(Service service, String metadataId) {
        MetadataOperation<InstanceMetadata> operation = buildMetadataOperation(service);
        operation.setTag(metadataId);
        WriteRequest operationLog = WriteRequest.newBuilder().setGroup(Constants.INSTANCE_METADATA)
            .setOperation(DataOperation.DELETE.name())
            .setData(ByteString.copyFrom(serializer.serialize(operation)))
            .build();
        submitMetadataOperation(operationLog);
    }
    
    /**
     * 向服务元数据追加集群元数据（ADD 操作）。
     *
     * @param service         目标服务
     * @param clusterName     集群名
     * @param clusterMetadata 集群元数据
     */
    public void addClusterMetadata(Service service, String clusterName,
        ClusterMetadata clusterMetadata) {
        MetadataOperation<ServiceMetadata> operation = buildMetadataOperation(service);
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setEphemeral(service.isEphemeral());
        serviceMetadata.getClusters().put(clusterName, clusterMetadata);
        operation.setMetadata(serviceMetadata);
        WriteRequest operationLog = WriteRequest.newBuilder().setGroup(Constants.SERVICE_METADATA)
            .setOperation(DataOperation.ADD.name())
            .setData(ByteString.copyFrom(serializer.serialize(operation)))
            .build();
        submitMetadataOperation(operationLog);
    }
    
    private <T> MetadataOperation<T> buildMetadataOperation(Service service) {
        MetadataOperation<T> result = new MetadataOperation<>();
        result.setNamespace(service.getNamespace());
        result.setGroup(service.getGroup());
        result.setServiceName(service.getName());
        return result;
    }
    
    private void submitMetadataOperation(WriteRequest operationLog) {
        try {
            Response response = cpProtocol.write(operationLog);
            if (!response.getSuccess()) {
                throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                    "do metadata operation failed " + response.getErrMsg());
            }
        } catch (Exception e) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR,
                "do metadata operation failed", e);
        }
    }
}
