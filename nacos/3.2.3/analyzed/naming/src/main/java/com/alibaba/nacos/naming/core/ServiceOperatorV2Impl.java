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

package com.alibaba.nacos.naming.core;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.core.utils.PageUtil;
import com.alibaba.nacos.naming.constants.FieldsConstants;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.event.metadata.InfoChangeEvent;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataOperateService;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link ServiceOperator} 的 V2 实现。
 *
 * <p>基于 {@link ServiceManager} 单例仓库与 {@link NamingMetadataOperateService} 完成服务元数据的增删改查，并通过 {@link SubscribeManager} 提供订阅者查询。</p>
 *
 * @author xiweng.yy
 */
@Component
public class ServiceOperatorV2Impl implements ServiceOperator {
    
    /** 元数据持久化操作服务。 */
    private final NamingMetadataOperateService metadataOperateService;
    
    /** 命名元数据内存管理器。 */
    private final NamingMetadataManager metadataManager;
    
    /** 服务实例存储，用于校验删除前是否仍有实例。 */
    private final ServiceStorage serviceStorage;
    
    /** 订阅关系管理器。 */
    private final SubscribeManager subscribeManager;
    
    /** 注入元数据操作、存储与订阅管理依赖。 */
    public ServiceOperatorV2Impl(NamingMetadataOperateService metadataOperateService,
        NamingMetadataManager metadataManager, ServiceStorage serviceStorage,
        SubscribeManager subscribeManager) {
        this.metadataOperateService = metadataOperateService;
        this.metadataManager = metadataManager;
        this.serviceStorage = serviceStorage;
        this.subscribeManager = subscribeManager;
    }
    
    @Override
    public void create(String namespaceId, String serviceName, ServiceMetadata metadata)
        throws NacosException {
        Service service =
            getServiceFromGroupedServiceName(namespaceId, serviceName, metadata.isEphemeral());
        create(service, metadata);
    }
    
    /**
     * 创建 V2 服务对象并写入元数据。
     *
     * @param service  V2 服务对象
     * @param metadata 服务元数据
     * @throws NacosException 服务已存在或写入失败时抛出
     */
    public void create(Service service, ServiceMetadata metadata) throws NacosException {
        if (ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.SERVICE_ALREADY_EXIST,
                String.format("specified service %s already exists!",
                    service.getGroupedServiceName()));
        }
        metadataOperateService.updateServiceMetadata(service, metadata);
    }
    
    @Override
    public void update(Service service, ServiceMetadata metadata) throws NacosException {
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SERVICE_NOT_EXIST,
                String.format("service %s not found!", service.getGroupedServiceName()));
        }
        metadataOperateService.updateServiceMetadata(service, metadata);
        NotifyCenter.publishEvent(new InfoChangeEvent.ServiceInfoChangeEvent(service));
    }
    
    @Override
    public void delete(String namespaceId, String serviceName) throws NacosException {
        Service service = getServiceFromGroupedServiceName(namespaceId, serviceName, true);
        delete(service);
    }
    
    /**
     * 删除 V2 服务（须无注册实例）。
     *
     * @param service V2 服务对象
     * @throws NacosException 服务不存在、仍有实例或删除失败时抛出
     */
    public void delete(Service service) throws NacosException {
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SERVICE_NOT_EXIST,
                String.format("service %s not found!", service.getGroupedServiceName()));
        }
        
        if (!serviceStorage.getPushData(service).getHosts().isEmpty()) {
            throw new NacosApiException(NacosException.INVALID_PARAM,
                ErrorCode.SERVICE_DELETE_FAILURE,
                "Service " + service.getGroupedServiceName()
                    + " is not empty, can't be delete. Please unregister instance first");
        }
        metadataOperateService.deleteServiceMetadata(service);
    }
    
    @Override
    public ObjectNode queryService(String namespaceId, String serviceName) throws NacosException {
        Service service = getServiceFromGroupedServiceName(namespaceId, serviceName, true);
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SERVICE_NOT_EXIST,
                "service not found, namespace: " + namespaceId + ", serviceName: " + serviceName);
        }
        ObjectNode result = JacksonUtils.createEmptyJsonNode();
        ServiceMetadata serviceMetadata =
            metadataManager.getServiceMetadata(service).orElse(new ServiceMetadata());
        setServiceMetadata(result, serviceMetadata, service);
        ArrayNode clusters = JacksonUtils.createEmptyArrayNode();
        for (String each : serviceStorage.getClusters(service)) {
            ClusterMetadata clusterMetadata =
                serviceMetadata.getClusters().containsKey(each)
                    ? serviceMetadata.getClusters().get(each)
                    : new ClusterMetadata();
            clusters.add(newClusterNode(each, clusterMetadata));
        }
        result.set(FieldsConstants.CLUSTERS, clusters);
        return result;
    }
    
    /**
     * 查询 V2 服务详情（含集群元数据）。
     *
     * @param service 服务对象
     * @return 服务详情 DTO
     * @throws NacosException 服务不存在时抛出
     */
    public ServiceDetailInfo queryService(Service service) throws NacosException {
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SERVICE_NOT_EXIST,
                "service not found, namespace: " + service.getNamespace() + ", serviceName: "
                    + service.getGroupedServiceName());
        }
        Service singleton = ServiceManager.getInstance().getSingleton(service);
        ServiceDetailInfo result = new ServiceDetailInfo();
        ServiceMetadata serviceMetadata =
            metadataManager.getServiceMetadata(singleton).orElse(new ServiceMetadata());
        setServiceMetadata(result, serviceMetadata, singleton);
        Map<String, ClusterInfo> clusters = new HashMap<>(2);
        for (String each : serviceStorage.getClusters(singleton)) {
            ClusterMetadata clusterMetadata =
                serviceMetadata.getClusters().containsKey(each)
                    ? serviceMetadata.getClusters().get(each)
                    : new ClusterMetadata();
            clusters.put(each, newClusterNodeV2(each, clusterMetadata));
        }
        result.setClusterMap(clusters);
        result.setEphemeral(singleton.isEphemeral());
        return result;
    }
    
    /** 将服务元数据填充到 JSON 详情节点。 */
    private void setServiceMetadata(ObjectNode serviceDetail, ServiceMetadata serviceMetadata,
        Service service) {
        serviceDetail.put(FieldsConstants.NAME_SPACE_ID, service.getNamespace());
        serviceDetail.put(FieldsConstants.GROUP_NAME, service.getGroup());
        serviceDetail.put(FieldsConstants.NAME, service.getName());
        serviceDetail.put(FieldsConstants.PROTECT_THRESHOLD, serviceMetadata.getProtectThreshold());
        serviceDetail.replace(FieldsConstants.METADATA,
            JacksonUtils.transferToJsonNode(serviceMetadata.getExtendData()));
        serviceDetail.replace(FieldsConstants.SELECTOR,
            JacksonUtils.transferToJsonNode(serviceMetadata.getSelector()));
    }
    
    /** 将服务元数据填充到 {@link ServiceDetailInfo} DTO。 */
    private void setServiceMetadata(ServiceDetailInfo serviceDetail,
        ServiceMetadata serviceMetadata, Service service) {
        serviceDetail.setNamespaceId(service.getNamespace());
        serviceDetail.setGroupName(service.getGroup());
        serviceDetail.setServiceName(service.getName());
        serviceDetail.setProtectThreshold(serviceMetadata.getProtectThreshold());
        serviceDetail.setMetadata(serviceMetadata.getExtendData());
        serviceDetail.setSelector(serviceMetadata.getSelector());
    }
    
    /** 构建单个集群的 JSON 节点。 */
    private ObjectNode newClusterNode(String clusterName, ClusterMetadata clusterMetadata) {
        ObjectNode result = JacksonUtils.createEmptyJsonNode();
        result.put(FieldsConstants.NAME, clusterName);
        result.replace(FieldsConstants.HEALTH_CHECKER,
            JacksonUtils.transferToJsonNode(clusterMetadata.getHealthChecker()));
        result.replace(FieldsConstants.METADATA,
            JacksonUtils.transferToJsonNode(clusterMetadata.getExtendData()));
        return result;
    }
    
    /** 构建单个集群的 {@link ClusterInfo} 对象。 */
    private ClusterInfo newClusterNodeV2(String clusterName, ClusterMetadata clusterMetadata) {
        ClusterInfo result = new ClusterInfo();
        result.setClusterName(clusterName);
        result.setHealthChecker(clusterMetadata.getHealthChecker());
        result.setMetadata(clusterMetadata.getExtendData());
        result.setUseInstancePortForCheck(clusterMetadata.isUseInstancePortForCheck());
        result.setHealthyCheckPort(clusterMetadata.getHealthyCheckPort());
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<String> listService(String namespaceId, String groupName, String selector)
        throws NacosException {
        Collection<Service> services = ServiceManager.getInstance().getSingletons(namespaceId);
        if (services.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // TODO 按 selector 过滤服务（尚未实现）
        return selectServiceWithGroupName(services, groupName);
    }
    
    /** 按分组名过滤服务集合并返回分组服务名。 */
    private Collection<String> selectServiceWithGroupName(Collection<Service> serviceSet,
        String groupName) {
        Collection<String> result = new HashSet<>(serviceSet.size());
        for (Service each : serviceSet) {
            if (Objects.equals(groupName, each.getGroup())) {
                result.add(each.getGroupedServiceName());
            }
        }
        return result;
    }
    
    /** 从分组服务名解析并构造 {@link Service} 对象。 */
    private Service getServiceFromGroupedServiceName(String namespaceId, String groupedServiceName,
        boolean ephemeral) {
        String groupName = NamingUtils.getGroupName(groupedServiceName);
        String serviceName = NamingUtils.getServiceName(groupedServiceName);
        return Service.newService(namespaceId, groupName, serviceName, ephemeral);
    }
    
    @Override
    public Collection<String> listAllNamespace() {
        return ServiceManager.getInstance().getAllNamespaces();
    }
    
    @Override
    public Collection<String> searchServiceName(String namespaceId, String expr)
        throws NacosException {
        String regex = Constants.ANY_PATTERN + expr + Constants.ANY_PATTERN;
        Collection<String> result = new HashSet<>();
        for (Service each : ServiceManager.getInstance().getSingletons(namespaceId)) {
            String groupedServiceName = each.getGroupedServiceName();
            if (groupedServiceName.matches(regex)) {
                result.add(groupedServiceName);
            }
        }
        return result;
    }
    
    @Override
    public Page<SubscriberInfo> getSubscribers(String namespaceId, String serviceName,
        String groupName,
        boolean aggregation, int pageNo, int pageSize) throws NacosException {
        Service service = Service.newService(namespaceId, groupName, serviceName);
        Page<SubscriberInfo> result = new Page<>();
        try {
            List<Subscriber> subscribers = subscribeManager.getSubscribers(service, aggregation);
            result = convertToSubscriberInfoPage(PageUtil.subPage(subscribers, pageNo, pageSize));
        } catch (Exception e) {
            Loggers.SRV_LOG.warn("query subscribers failed!", e);
        }
        return result;
    }
    
    /** 将内部 {@link Subscriber} 分页结果转换为 API {@link SubscriberInfo} 分页。 */
    private Page<SubscriberInfo> convertToSubscriberInfoPage(Page<Subscriber> page) {
        Page<SubscriberInfo> result = new Page<>();
        result.setPageItems(page.getPageItems().stream().map(subscriber -> {
            SubscriberInfo subscriberInfo = new SubscriberInfo();
            subscriberInfo.setNamespaceId(subscriber.getNamespaceId());
            String groupedServiceName = subscriber.getServiceName();
            subscriberInfo.setServiceName(NamingUtils.getServiceName(groupedServiceName));
            subscriberInfo.setGroupName(NamingUtils.getGroupName(groupedServiceName));
            subscriberInfo.setAppName(subscriber.getApp());
            subscriberInfo.setIp(subscriber.getIp());
            subscriberInfo.setPort(subscriber.getPort());
            subscriberInfo.setAgent(subscriber.getAgent());
            return subscriberInfo;
        }).collect(Collectors.toList()));
        result.setTotalCount(page.getTotalCount());
        result.setPagesAvailable(page.getPagesAvailable());
        result.setPageNumber(page.getPageNumber());
        return result;
    }
}
