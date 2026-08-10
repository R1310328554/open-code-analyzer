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

package com.alibaba.nacos.prometheus.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.naming.core.InstanceOperatorClientImpl;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.prometheus.api.ApiConstants;
import com.alibaba.nacos.prometheus.utils.PrometheusUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Prometheus 服务发现（HTTP SD）REST 控制器。
 *
 * <p>暴露 JSON 格式的 targets/labels 列表，供 Prometheus 抓取 Nacos 注册实例； 支持全量、按命名空间、按服务三种粒度查询。</p>
 *
 * @author karsonto
 */
@RestController
@ConditionalOnProperty(name = "nacos.prometheus.metrics.enabled", havingValue = "true")
public class PrometheusController {
    
    /** 命名服务实例查询客户端（V2）。 */
    @Autowired
    private InstanceOperatorClientImpl instanceServiceV2;
    
    /** 命名空间与服务元数据管理器。 */
    private final ServiceManager serviceManager;
    
    /** 初始化并绑定 {@link ServiceManager} 单例。 */
    public PrometheusController() {
        this.serviceManager = ServiceManager.getInstance();
    }
    
    /**
     * 返回所有命名空间下全部健康实例的 Prometheus SD JSON。
     *
     * @throws NacosException 实例列表查询失败时抛出
     */
    @Since("2.2.0")
    @GetMapping(value = ApiConstants.PROMETHEUS_CONTROLLER_PATH,
        produces = "application/json; charset=UTF-8")
    /** 遍历全部命名空间与服务，组装 targets 数组。 */
    public ResponseEntity<String> metric() throws NacosException {
        ArrayNode arrayNode = JacksonUtils.createEmptyArrayNode();
        Set<Instance> targetSet = new HashSet<>();
        Set<String> allNamespaces = serviceManager.getAllNamespaces();
        for (String namespace : allNamespaces) {
            Set<Service> singletons = serviceManager.getSingletons(namespace);
            for (Service service : singletons) {
                
                List<? extends Instance> instances = instanceServiceV2.listAllInstances(namespace,
                    service.getGroupedServiceName());
                
                targetSet.addAll(instances);
                
            }
        }
        
        PrometheusUtils.assembleArrayNodes(targetSet, arrayNode);
        return ResponseEntity.ok().body(arrayNode.toString());
    }
    
    /**
     * 返回指定命名空间内全部实例的 Prometheus SD JSON。
     *
     * @throws NacosException 实例列表查询失败时抛出
     */
    @Since("2.3.0")
    @GetMapping(value = ApiConstants.PROMETHEUS_CONTROLLER_NAMESPACE_PATH,
        produces = "application/json; charset=UTF-8")
    /** 按 namespaceId 过滤服务后返回 SD JSON。 */
    public ResponseEntity<String> metricNamespace(@PathVariable("namespaceId") String namespaceId)
        throws NacosException {
        ArrayNode arrayNode = getServiceArrayNode(namespaceId, s -> true);
        
        return ResponseEntity.ok().body(arrayNode.toString());
    }
    
    /**
     * 返回指定命名空间与单个服务下实例的 Prometheus SD JSON。
     *
     * @throws NacosException 实例列表查询失败时抛出
     */
    @Since("2.3.0")
    @GetMapping(value = ApiConstants.PROMETHEUS_CONTROLLER_SERVICE_PATH,
        produces = "application/json; charset=UTF-8")
    /** 按 namespaceId 与 service 名精确过滤实例。 */
    public ResponseEntity<String> metricNamespaceService(
        @PathVariable("namespaceId") String namespaceId,
        @PathVariable("service") String service) throws NacosException {
        ArrayNode arrayNode = getServiceArrayNode(namespaceId, s -> s.getName().equals(service));
        
        return ResponseEntity.ok().body(arrayNode.toString());
    }
    
    /** 内部方法：按命名空间与服务谓词组装 SD 数组节点。 */
    private ArrayNode getServiceArrayNode(String namespaceId, Predicate<Service> serviceFilter)
        throws NacosException {
        ArrayNode arrayNode = JacksonUtils.createEmptyArrayNode();
        Set<String> allNamespaces = serviceManager.getAllNamespaces();
        if (!allNamespaces.contains(namespaceId)) {
            return arrayNode;
        }
        
        Set<Instance> targetSet = new HashSet<>();
        
        Set<Service> singletons = serviceManager.getSingletons(namespaceId);
        for (Service existService : singletons) {
            if (!serviceFilter.test(existService)) {
                continue;
            }
            List<? extends Instance> instances = instanceServiceV2.listAllInstances(namespaceId,
                existService.getGroupedServiceName());
            
            targetSet.addAll(instances);
            
        }
        
        PrometheusUtils.assembleArrayNodes(targetSet, arrayNode);
        
        return arrayNode;
    }
}
