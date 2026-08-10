/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.remote.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.console.handler.impl.ConditionFunctionEnabled;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.console.handler.naming.ServiceHandler;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.model.form.ServiceForm;
import org.springframework.context.annotation.Conditional;

import java.util.List;

/**
 * 服务管理远程 Handler：CRUD、订阅者查询、集群元数据更新，通过 {@link NacosMaintainerClientHolder} 调用远端 Naming Maintainer API。
 * Remote Implementation of ServiceHandler that handles service-related operations.
 *
 * @author xiweng.yy
 */
@org.springframework.stereotype.Service
@EnabledRemoteHandler
@Conditional(ConditionFunctionEnabled.ConditionNamingEnabled.class)
public class ServiceRemoteHandler implements ServiceHandler {
    
    /** 运维客户端持有者，提供 Naming Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public ServiceRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 在远端创建服务并附带元数据与选择器。 */
    @Override
    public void createService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        Service service = buildService(serviceForm, serviceMetadata);
        clientHolder.getNamingMaintainerService().createService(service);
    }
    
    /** 删除远端指定服务。 */
    @Override
    public void deleteService(String namespaceId, String serviceName, String groupName)
        throws Exception {
        clientHolder.getNamingMaintainerService().removeService(namespaceId, groupName,
            serviceName);
    }
    
    /** 更新远端服务元数据与选择器。 */
    @Override
    public void updateService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        Service servicePojo = buildService(serviceForm, serviceMetadata);
        clientHolder.getNamingMaintainerService().updateService(servicePojo);
    }
    
    /** 获取远端支持的服务选择器类型列表。 */
    @Override
    public List<String> getSelectorTypeList() throws NacosException {
        return clientHolder.getNamingMaintainerService().listSelectorTypes();
    }
    
    /** 分页查询远端服务的订阅者列表，可选聚合模式。 */
    @Override
    public Page<SubscriberInfo> getSubscribers(int pageNo, int pageSize, String namespaceId,
        String serviceName,
        String groupName, boolean aggregation) throws Exception {
        return clientHolder.getNamingMaintainerService()
            .getSubscribers(namespaceId, groupName, serviceName, pageNo, pageSize, aggregation);
    }
    
    /** 分页列出远端服务，可选附带实例详情或忽略空服务。 */
    @Override
    public Object getServiceList(boolean withInstances, String namespaceId, int pageNo,
        int pageSize,
        String serviceName, String groupName, boolean ignoreEmptyService) throws NacosException {
        if (withInstances) {
            return clientHolder.getNamingMaintainerService()
                .listServicesWithDetail(namespaceId, groupName, serviceName, pageNo, pageSize);
        }
        return clientHolder.getNamingMaintainerService()
            .listServices(namespaceId, groupName, serviceName, ignoreEmptyService, pageNo,
                pageSize);
    }
    
    /** 获取远端服务详情（含集群与实例摘要）。 */
    @Override
    public ServiceDetailInfo getServiceDetail(String namespaceId, String serviceName,
        String groupName)
        throws NacosException {
        return clientHolder.getNamingMaintainerService().getServiceDetail(namespaceId, groupName,
            serviceName);
    }
    
    /** 更新远端服务下指定集群的健康检查与扩展元数据。 */
    @Override
    public void updateClusterMetadata(String namespaceId, String groupName, String serviceName,
        String clusterName,
        ClusterMetadata clusterMetadata) throws Exception {
        Service service = new Service();
        service.setNamespaceId(namespaceId);
        service.setGroupName(groupName);
        service.setName(serviceName);
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setClusterName(clusterName);
        clusterInfo.setHealthChecker(clusterMetadata.getHealthChecker());
        clusterInfo.setMetadata(clusterMetadata.getExtendData());
        clusterInfo.setUseInstancePortForCheck(clusterMetadata.isUseInstancePortForCheck());
        clusterInfo.setHealthyCheckPort(clusterMetadata.getHealthyCheckPort());
        clientHolder.getNamingMaintainerService().updateCluster(service, clusterInfo);
    }
    
    /** 将表单与元数据组装为 {@link Service} POJO 供远端 API 调用。 */
    private Service buildService(ServiceForm serviceForm, ServiceMetadata metadata) {
        Service service = new Service();
        service.setNamespaceId(serviceForm.getNamespaceId());
        service.setName(serviceForm.getServiceName());
        service.setGroupName(serviceForm.getGroupName());
        service.setProtectThreshold(serviceForm.getProtectThreshold());
        service.setEphemeral(serviceForm.getEphemeral());
        service.setMetadata(metadata.getExtendData());
        service.setSelector(metadata.getSelector());
        return service;
    }
}
