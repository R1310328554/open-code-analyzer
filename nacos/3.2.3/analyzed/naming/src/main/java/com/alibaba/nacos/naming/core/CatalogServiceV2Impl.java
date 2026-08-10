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
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceView;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.PageUtil;
import com.alibaba.nacos.naming.constants.FieldsConstants;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.utils.ServiceUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * V2 服务目录实现，基于 {@link ServiceStorage} 与 {@link NamingMetadataManager} 聚合查询。
 *
 * <p>支持模糊匹配、分页、健康实例统计与保护阈值触发标记。</p>
 *
 * @author xiweng.yy
 */
@Component()
public class CatalogServiceV2Impl implements CatalogService {
    
    /** 服务实例与集群索引存储。 */
    private final ServiceStorage serviceStorage;
    
    /** 服务/集群/实例元数据管理器。 */
    private final NamingMetadataManager metadataManager;
    
    /** 默认 HTTP 端口，用于部分统计场景。 */
    private static final int DEFAULT_PORT = 80;
    
    public CatalogServiceV2Impl(ServiceStorage serviceStorage,
        NamingMetadataManager metadataManager) {
        this.serviceStorage = serviceStorage;
        this.metadataManager = metadataManager;
    }
    
    @Override
    public ServiceDetailInfo getServiceDetail(String namespaceId, String groupName,
        String serviceName)
        throws NacosException {
        Service service = Service.newService(namespaceId, groupName, serviceName);
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosException(NacosException.NOT_FOUND,
                String.format("service %s@@%s is not found!", groupName, serviceName));
        }
        service = ServiceManager.getInstance().getSingleton(service);
        Optional<ServiceMetadata> metadata = metadataManager.getServiceMetadata(service);
        ServiceMetadata detailedService = metadata.orElseGet(ServiceMetadata::new);
        
        ServiceDetailInfo result = new ServiceDetailInfo();
        result.setNamespaceId(service.getNamespace());
        result.setGroupName(service.getGroup());
        result.setServiceName(serviceName);
        result.setEphemeral(service.isEphemeral());
        result.setProtectThreshold(detailedService.getProtectThreshold());
        result.setSelector(detailedService.getSelector());
        result.setMetadata(detailedService.getExtendData());
        
        Map<String, ClusterInfo> clusters =
            new HashMap<>(serviceStorage.getClusters(service).size());
        for (String each : serviceStorage.getClusters(service)) {
            ClusterMetadata clusterMetadata =
                detailedService.getClusters().containsKey(each)
                    ? detailedService.getClusters().get(each)
                    : new ClusterMetadata();
            ClusterInfo clusterInfo = new ClusterInfo();
            clusterInfo.setClusterName(each);
            clusterInfo.setHealthChecker(clusterMetadata.getHealthChecker());
            clusterInfo.setMetadata(clusterMetadata.getExtendData());
            clusterInfo.setUseInstancePortForCheck(clusterMetadata.isUseInstancePortForCheck());
            clusterInfo.setHealthyCheckPort(clusterMetadata.getHealthyCheckPort());
            clusters.put(each, clusterInfo);
        }
        result.setClusterMap(clusters);
        return result;
    }
    
    @Override
    public List<? extends Instance> listInstances(String namespaceId, String groupName,
        String serviceName,
        String clusterName) throws NacosException {
        Service service = Service.newService(namespaceId, groupName, serviceName);
        if (!ServiceManager.getInstance().containSingleton(service)) {
            throw new NacosException(NacosException.NOT_FOUND,
                String.format("service %s@@%s is not found!", groupName, serviceName));
        }
        if (StringUtils.isNotBlank(clusterName)
            && !serviceStorage.getClusters(service).contains(clusterName)) {
            throw new NacosException(NacosException.NOT_FOUND,
                "cluster " + clusterName + " is not found!");
        }
        ServiceInfo serviceInfo = serviceStorage.getData(service);
        ServiceInfo result = ServiceUtil.selectInstances(serviceInfo, clusterName);
        return result.getHosts();
    }
    
    @Override
    public List<? extends Instance> listAllInstances(String namespaceId, String groupName,
        String serviceName) {
        Service service = Service.newService(namespaceId, groupName, serviceName);
        if (!ServiceManager.getInstance().containSingleton(service)) {
            return Collections.EMPTY_LIST;
        }
        
        ServiceInfo serviceInfo = serviceStorage.getData(service);
        
        return serviceInfo.getHosts();
    }
    
    @Override
    public Object pageListService(String namespaceId, String groupName, String serviceName,
        int pageNo, int pageSize,
        String instancePattern, boolean ignoreEmptyService) throws NacosException {
        ObjectNode result = JacksonUtils.createEmptyJsonNode();
        List<ServiceView> serviceViews = new LinkedList<>();
        Collection<Service> services = patternServices(namespaceId, groupName, serviceName);
        if (ignoreEmptyService) {
            services = services.stream().filter(each -> 0 != serviceStorage.getData(each).ipCount())
                .collect(Collectors.toList());
        }
        result.put(FieldsConstants.COUNT, services.size());
        services = doPage(services, pageNo - 1, pageSize);
        for (Service each : services) {
            ServiceMetadata serviceMetadata =
                metadataManager.getServiceMetadata(each).orElseGet(ServiceMetadata::new);
            ServiceView serviceView = new ServiceView();
            serviceView.setName(each.getName());
            serviceView.setGroupName(each.getGroup());
            serviceView.setClusterCount(serviceStorage.getClusters(each).size());
            serviceView.setIpCount(serviceStorage.getData(each).ipCount());
            serviceView.setHealthyInstanceCount(countHealthyInstance(serviceStorage.getData(each)));
            serviceView.setTriggerFlag(
                isProtectThreshold(serviceView, serviceMetadata) ? "true" : "false");
            serviceViews.add(serviceView);
        }
        result.set(FieldsConstants.SERVICE_LIST, JacksonUtils.transferToJsonNode(serviceViews));
        return result;
    }
    
    @Override
    public Page<ServiceView> listService(String namespaceId, String groupName, String serviceName,
        int pageNo,
        int pageSize, boolean ignoreEmptyService) throws NacosException {
        Page<ServiceView> serviceViews = new Page<>();
        Collection<Service> services = patternServices(namespaceId, groupName, serviceName);
        if (ignoreEmptyService) {
            services = services.stream().filter(each -> 0 != serviceStorage.getData(each).ipCount())
                .toList();
        }
        Page<Service> page = PageUtil.subPage(services.stream().toList(), pageNo, pageSize);
        serviceViews.setTotalCount(page.getTotalCount());
        serviceViews.setPageNumber(page.getPageNumber());
        serviceViews.setPagesAvailable(page.getPagesAvailable());
        for (Service each : page.getPageItems()) {
            ServiceMetadata serviceMetadata =
                metadataManager.getServiceMetadata(each).orElseGet(ServiceMetadata::new);
            ServiceView serviceView = new ServiceView();
            serviceView.setName(each.getName());
            serviceView.setGroupName(each.getGroup());
            serviceView.setClusterCount(serviceStorage.getClusters(each).size());
            serviceView.setIpCount(serviceStorage.getData(each).ipCount());
            serviceView.setHealthyInstanceCount(countHealthyInstance(serviceStorage.getData(each)));
            serviceView.setTriggerFlag(
                isProtectThreshold(serviceView, serviceMetadata) ? "true" : "false");
            serviceViews.getPageItems().add(serviceView);
        }
        return serviceViews;
    }
    
    /** 统计服务下健康实例数量。 */
    private int countHealthyInstance(ServiceInfo data) {
        int result = 0;
        for (Instance each : data.getHosts()) {
            if (each.isHealthy()) {
                result++;
            }
        }
        return result;
    }
    
    /** 判断健康实例比例是否低于保护阈值（触发降级标记）。 */
    private boolean isProtectThreshold(ServiceView serviceView, ServiceMetadata metadata) {
        return (serviceView.getHealthyInstanceCount() * 1.0 / serviceView.getIpCount()) <= metadata
            .getProtectThreshold();
    }
    
    @Override
    public Page<ServiceDetailInfo> pageListServiceDetail(String namespaceId, String groupName,
        String serviceName,
        int pageNo, int pageSize) throws NacosException {
        Collection<Service> services = patternServices(namespaceId, groupName, serviceName);
        Page<Service> servicePage = PageUtil.subPage(services.stream().toList(), pageNo, pageSize);
        Page<ServiceDetailInfo> result = new Page<>();
        result.setPagesAvailable(servicePage.getPagesAvailable());
        result.setPageNumber(servicePage.getPageNumber());
        result.setTotalCount(servicePage.getTotalCount());
        List<ServiceDetailInfo> pagedItem = new LinkedList<>();
        for (Service each : servicePage.getPageItems()) {
            ServiceDetailInfo serviceDetailInfo = new ServiceDetailInfo();
            serviceDetailInfo.setServiceName(each.getName());
            serviceDetailInfo.setGroupName(each.getGroup());
            ServiceMetadata serviceMetadata =
                metadataManager.getServiceMetadata(each).orElseGet(ServiceMetadata::new);
            serviceDetailInfo.setMetadata(serviceMetadata.getExtendData());
            serviceDetailInfo.setClusterMap(getClusterMap(each));
            pagedItem.add(serviceDetailInfo);
        }
        result.setPageItems(pagedItem);
        return result;
    }
    
    /** 按集群分组聚合实例列表。 */
    private Map<String, ClusterInfo> getClusterMap(Service service) {
        Map<String, ClusterInfo> result = new HashMap<>(1);
        for (Instance each : serviceStorage.getData(service).getHosts()) {
            if (!result.containsKey(each.getClusterName())) {
                ClusterInfo clusterInfo = new ClusterInfo();
                clusterInfo.setHosts(new LinkedList<>());
                result.put(each.getClusterName(), clusterInfo);
            }
            result.get(each.getClusterName()).getHosts().add(each);
        }
        return result;
    }
    
    /** 按命名空间与 group/service 模糊模式筛选服务集合。 */
    private Collection<Service> patternServices(String namespaceId, String group,
        String serviceName) {
        boolean noFilter = StringUtils.isBlank(serviceName) && StringUtils.isBlank(group);
        if (noFilter) {
            return ServiceManager.getInstance().getSingletons(namespaceId);
        }
        Collection<Service> result = new LinkedList<>();
        StringJoiner regex = new StringJoiner(Constants.SERVICE_INFO_SPLITER);
        regex.add(getRegexString(group));
        regex.add(getRegexString(serviceName));
        String regexString = regex.toString();
        for (Service each : ServiceManager.getInstance().getSingletons(namespaceId)) {
            if (each.getGroupedServiceName().matches(regexString)) {
                result.add(each);
            }
        }
        return result;
    }
    
    /** 将通配符目标转换为正则匹配片段。 */
    private String getRegexString(String target) {
        return StringUtils.isBlank(target) ? Constants.ANY_PATTERN
            : Constants.ANY_PATTERN + target + Constants.ANY_PATTERN;
    }
    
    /** 对服务集合执行内存分页（兼容旧 API）。 */
    private Collection<Service> doPage(Collection<Service> services, int pageNo, int pageSize) {
        if (pageNo == 0 && services.size() < pageSize) {
            return services;
        }
        int start = pageNo * pageSize;
        if (start > services.size()) {
            return Collections.emptyList();
        }
        Collection<Service> result = new LinkedList<>();
        int i = 0;
        for (Service each : services) {
            if (i++ < start) {
                continue;
            }
            result.add(each);
            if (result.size() >= pageSize) {
                break;
            }
        }
        return result;
    }
}
