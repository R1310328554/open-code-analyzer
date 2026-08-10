/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.controller.v3.naming;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.healthcheck.HealthCheckerFactory;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.api.selector.Selector;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.proxy.naming.ServiceProxy;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.model.form.AggregationForm;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.ServiceForm;
import com.alibaba.nacos.naming.model.form.ServiceListForm;
import com.alibaba.nacos.naming.model.form.UpdateClusterForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.naming.selector.NoneSelector;
import com.alibaba.nacos.naming.selector.SelectorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 控制台 v3 服务管理 REST 控制器：服务的 CRUD、订阅者查询、集群健康检查配置等。
 * Controller for handling HTTP requests related to service operations.
 *
 * @author zhangyukun on:2024/8/16
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/ns/service")
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class ConsoleServiceController {
    
    /** 服务操作代理，对接 naming 服务维护层 */
    private final ServiceProxy serviceProxy;
    
    /** 路由选择器管理器，用于解析服务关联的选择器 JSON */
    private final SelectorManager selectorManager;
    
    /** 注入服务代理与选择器管理器 */
    public ConsoleServiceController(ServiceProxy serviceProxy, SelectorManager selectorManager) {
        this.serviceProxy = serviceProxy;
        this.selectorManager = selectorManager;
    }
    
    /**
     * 创建新服务（默认创建持久化服务并写入元数据与选择器）。
     * Create a new service. This API will create a persistence service.
     */
    @Since("3.0.0")
    @PostMapping()
    @TpsControl(pointName = "NamingServiceRegister", name = "HttpNamingServiceRegister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> createService(ServiceForm serviceForm) throws Exception {
        serviceForm.validate();
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setProtectThreshold(serviceForm.getProtectThreshold());
        serviceMetadata.setSelector(parseSelector(serviceForm.getSelector()));
        serviceMetadata.setExtendData(UtilsAndCommons.parseMetadata(serviceForm.getMetadata()));
        serviceMetadata.setEphemeral(serviceForm.getEphemeral());
        
        serviceProxy.createService(serviceForm, serviceMetadata);
        return Result.success("ok");
    }
    
    /**
     * 删除指定命名空间下的服务。
     * Remove service.
     */
    @Since("3.0.0")
    @DeleteMapping()
    @TpsControl(pointName = "NamingServiceDeregister", name = "HttpNamingServiceDeregister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> deleteService(ServiceForm serviceForm) throws Exception {
        serviceForm.validate();
        serviceProxy.deleteService(serviceForm.getNamespaceId(), serviceForm.getServiceName(),
            serviceForm.getGroupName());
        return Result.success("ok");
    }
    
    /**
     * 更新服务保护阈值、元数据与路由选择器。
     * Update service.
     */
    @Since("3.0.0")
    @PutMapping()
    @TpsControl(pointName = "NamingServiceUpdate", name = "HttpNamingServiceUpdate")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> updateService(ServiceForm serviceForm) throws Exception {
        serviceForm.validate();
        Map<String, String> metadata = UtilsAndCommons.parseMetadata(serviceForm.getMetadata());
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setProtectThreshold(serviceForm.getProtectThreshold());
        serviceMetadata.setExtendData(metadata);
        serviceMetadata.setSelector(parseSelector(serviceForm.getSelector()));
        serviceProxy.updateService(serviceForm, serviceMetadata);
        return Result.success("ok");
    }
    
    /**
     * 获取控制台支持的全部 {@link Selector} 类型名称。
     * Get all {@link Selector} types.
     *
     * @return {@link Selector} 类型列表
     */
    @Since("3.0.0")
    @GetMapping("/selector/types")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "naming", action = ActionTypes.READ, apiType = ApiType.CONSOLE_API,
        tags = Constants.Tag.ONLY_IDENTITY)
    public Result<List<String>> getSelectorTypeList() throws NacosException {
        return Result.success(serviceProxy.getSelectorTypeList());
    }
    
    /**
     * 分页查询指定服务的订阅者列表，可选聚合模式。
     * get subscriber list.
     *
     * @param serviceForm     服务定位表单
     * @param pageForm        分页参数
     * @param aggregationForm 是否启用聚合查询
     * @return 订阅者分页结果
     * @throws Exception 查询过程中的任意异常
     */
    @Since("3.0.0")
    @GetMapping("/subscribers")
    @Secured(action = ActionTypes.READ, apiType = ApiType.CONSOLE_API)
    public Result<Page<SubscriberInfo>> subscribers(ServiceForm serviceForm, PageForm pageForm,
        AggregationForm aggregationForm) throws Exception {
        serviceForm.validate();
        pageForm.validate();
        int pageNo = pageForm.getPageNo();
        int pageSize = pageForm.getPageSize();
        String namespaceId = serviceForm.getNamespaceId();
        String serviceName = serviceForm.getServiceName();
        String groupName = serviceForm.getGroupName();
        boolean aggregation = aggregationForm.isAggregation();
        Page<SubscriberInfo> subscribers =
            serviceProxy.getSubscribers(pageNo, pageSize, namespaceId, serviceName,
                groupName, aggregation);
        return Result.success(subscribers);
    }
    
    /**
     * 分页列出服务详情；是否附带实例由 withInstances 参数决定。
     * List service detail information.
     *
     * @param serviceListForm 服务列表查询表单
     * @param pageForm        分页参数
     * @return 服务详情或视图列表（取决于 withInstances）
     */
    @Since("3.0.0")
    @Secured(action = ActionTypes.READ, apiType = ApiType.CONSOLE_API)
    @GetMapping("/list")
    public Result<Object> getServiceList(ServiceListForm serviceListForm, PageForm pageForm)
        throws NacosException {
        serviceListForm.validate();
        pageForm.validate();
        String namespaceId = serviceListForm.getNamespaceId();
        String serviceName = serviceListForm.getServiceNameParam();
        String groupName = serviceListForm.getGroupNameParam();
        boolean hasIpCount = serviceListForm.isIgnoreEmptyService();
        boolean withInstances = serviceListForm.isWithInstances();
        return Result.success(
            serviceProxy.getServiceList(withInstances, namespaceId, pageForm.getPageNo(),
                pageForm.getPageSize(),
                serviceName, groupName, hasIpCount));
    }
    
    /**
     * 获取单个服务的完整详情。
     * Get service detail.
     *
     * @param serviceForm 服务定位表单
     * @return 服务详情信息
     * @throws NacosException Nacos 业务异常
     */
    @Since("3.0.0")
    @Secured(action = ActionTypes.READ, apiType = ApiType.CONSOLE_API)
    @GetMapping()
    public Result<ServiceDetailInfo> getServiceDetail(ServiceForm serviceForm)
        throws NacosException {
        serviceForm.validate();
        ServiceDetailInfo result = serviceProxy.getServiceDetail(serviceForm.getNamespaceId(),
            serviceForm.getServiceName(), serviceForm.getGroupName());
        return Result.success(result);
    }
    
    /**
     * 更新服务下某集群的健康检查端口、检查器与扩展元数据。
     * Update cluster.
     *
     * @param updateClusterForm 集群更新表单
     * @return 成功时返回 'ok'
     * @throws Exception 更新失败时抛出
     */
    @Since("3.0.0")
    @PutMapping("/cluster")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> updateCluster(UpdateClusterForm updateClusterForm) throws Exception {
        updateClusterForm.validate();
        final String namespaceId = updateClusterForm.getNamespaceId();
        final String clusterName = updateClusterForm.getClusterName();
        final String serviceName = updateClusterForm.getServiceName();
        final String groupName = updateClusterForm.getGroupName();
        ClusterMetadata clusterMetadata = new ClusterMetadata();
        clusterMetadata.setHealthyCheckPort(updateClusterForm.getCheckPort());
        clusterMetadata.setUseInstancePortForCheck(updateClusterForm.isUseInstancePort4Check());
        AbstractHealthChecker healthChecker =
            HealthCheckerFactory.deserialize(updateClusterForm.getHealthChecker());
        clusterMetadata.setHealthChecker(healthChecker);
        clusterMetadata.setHealthyCheckType(healthChecker.getType());
        clusterMetadata
            .setExtendData(UtilsAndCommons.parseMetadata(updateClusterForm.getMetadata()));
        serviceProxy.updateClusterMetadata(namespaceId, groupName, serviceName, clusterName,
            clusterMetadata);
        return Result.success("ok");
    }
    
    private Selector parseSelector(String selectorJsonString) throws Exception {
        if (StringUtils.isBlank(selectorJsonString)) {
            return new NoneSelector();
        }
        
        JsonNode selectorJson = JacksonUtils.toObj(URLDecoder.decode(selectorJsonString, "UTF-8"));
        String type = Optional.ofNullable(selectorJson.get("type")).orElseThrow(
            () -> new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SELECTOR_ERROR,
                "not match any type of selector!"))
            .asText();
        String expression =
            Optional.ofNullable(selectorJson.get("expression")).map(JsonNode::asText).orElse(null);
        Selector selector = selectorManager.parseSelector(type, expression);
        if (Objects.isNull(selector)) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.SELECTOR_ERROR,
                "not match any type of selector!");
        }
        return selector;
    }
}
