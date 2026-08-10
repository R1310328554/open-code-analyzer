/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.controllers.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.maintainer.InstanceMetadataBatchResult;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.trace.DeregisterInstanceReason;
import com.alibaba.nacos.common.trace.event.naming.DeregisterInstanceTraceEvent;
import com.alibaba.nacos.common.trace.event.naming.RegisterInstanceTraceEvent;
import com.alibaba.nacos.common.trace.event.naming.UpdateInstanceTraceEvent;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.core.CatalogService;
import com.alibaba.nacos.naming.core.InstanceOperatorClientImpl;
import com.alibaba.nacos.naming.core.InstancePatchObject;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.InstanceForm;
import com.alibaba.nacos.naming.model.form.InstanceListForm;
import com.alibaba.nacos.naming.model.form.InstanceMetadataBatchOperationForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.naming.paramcheck.NamingInstanceListHttpParamExtractor;
import com.alibaba.nacos.naming.paramcheck.NamingInstanceMetadataBatchHttpParamExtractor;
import com.alibaba.nacos.naming.pojo.InstanceOperationInfo;
import com.alibaba.nacos.naming.utils.InstanceUtil;
import com.alibaba.nacos.naming.utils.NamingRequestUtil;
import com.alibaba.nacos.naming.web.CanDistro;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.alibaba.nacos.naming.misc.UtilsAndCommons.DEFAULT_CLUSTER_NAME;

/**
 * 命名实例管理 v3 控制器（管理 API）。
 *
 * <p>提供实例注册、注销、更新、批量元数据操作、部分更新及列表/详情查询；支持 TPS 限流与 Distro 路由。</p>
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(UtilsAndCommons.INSTANCE_CONTROLLER_V3_ADMIN_PATH)
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class InstanceControllerV3 {
    
    /** 命名开关域，提供默认临时实例等运行时配置。 */
    private final SwitchDomain switchDomain;
    
    /** 实例 CRUD 业务实现。 */
    private final InstanceOperatorClientImpl instanceService;
    
    /** 服务目录查询，用于实例列表接口。 */
    private final CatalogService catalogService;
    
    public InstanceControllerV3(SwitchDomain switchDomain,
        InstanceOperatorClientImpl instanceService,
        CatalogService catalogService) {
        this.switchDomain = switchDomain;
        this.instanceService = instanceService;
        this.catalogService = catalogService;
    }
    
    /** 注册新实例到指定服务。 */
    @Since("3.0.0")
    @CanDistro
    @PostMapping
    @TpsControl(pointName = "NamingInstanceRegister", name = "HttpNamingInstanceRegister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> register(InstanceForm instanceForm) throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        NamingRequestUtil.checkWeight(instanceForm.getWeight());
        // 构建实例对象
        Instance instance =
            InstanceUtil.buildInstance(instanceForm, switchDomain.isDefaultInstanceEphemeral());
        String namespaceId = instanceForm.getNamespaceId();
        String groupName = instanceForm.getGroupName();
        String serviceName = instanceForm.getServiceName();
        instanceService.registerInstance(namespaceId, groupName, serviceName, instance);
        NotifyCenter.publishEvent(
            new RegisterInstanceTraceEvent(System.currentTimeMillis(),
                NamingRequestUtil.getSourceIp(), false,
                namespaceId, groupName, serviceName, instance.getIp(), instance.getPort()));
        
        return Result.success("ok");
    }
    
    /** 从指定服务注销实例。 */
    @Since("3.0.0")
    @CanDistro
    @DeleteMapping
    @TpsControl(pointName = "NamingInstanceDeregister", name = "HttpNamingInstanceDeregister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> deregister(InstanceForm instanceForm) throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        // 构建实例对象
        Instance instance =
            InstanceUtil.buildInstance(instanceForm, switchDomain.isDefaultInstanceEphemeral());
        instanceService.removeInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
            instanceForm.getServiceName(), instance);
        NotifyCenter.publishEvent(
            new DeregisterInstanceTraceEvent(System.currentTimeMillis(),
                NamingRequestUtil.getSourceIp(), false,
                DeregisterInstanceReason.REQUEST, instanceForm.getNamespaceId(),
                instanceForm.getGroupName(),
                instanceForm.getServiceName(), instance.getIp(), instance.getPort()));
        
        return Result.success("ok");
    }
    
    /** 全量更新实例属性。 */
    @Since("3.0.0")
    @CanDistro
    @PutMapping
    @TpsControl(pointName = "NamingInstanceUpdate", name = "HttpNamingInstanceUpdate")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> update(InstanceForm instanceForm) throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        NamingRequestUtil.checkWeight(instanceForm.getWeight());
        // 构建实例对象
        Instance instance =
            InstanceUtil.buildInstance(instanceForm, switchDomain.isDefaultInstanceEphemeral());
        instanceService.updateInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
            instanceForm.getServiceName(), instance);
        NotifyCenter.publishEvent(
            new UpdateInstanceTraceEvent(System.currentTimeMillis(),
                NamingRequestUtil.getSourceIp(),
                instanceForm.getNamespaceId(), instanceForm.getGroupName(),
                instanceForm.getServiceName(),
                instance.getIp(), instance.getPort(), instance.getMetadata()));
        
        return Result.success("ok");
    }
    
    /** 批量更新实例元数据：已有键更新，不存在则新增。 */
    @Since("3.0.0")
    @CanDistro
    @PutMapping(value = "/metadata/batch")
    @TpsControl(pointName = "NamingInstanceMetadataUpdate",
        name = "HttpNamingInstanceMetadataBatchUpdate")
    @ExtractorManager.Extractor(httpExtractor = NamingInstanceMetadataBatchHttpParamExtractor.class)
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<InstanceMetadataBatchResult> batchUpdateInstanceMetadata(
        InstanceMetadataBatchOperationForm form)
        throws NacosException {
        form.validate();
        
        List<Instance> targetInstances = parseBatchInstances(form.getInstances());
        Map<String, String> targetMetadata = UtilsAndCommons.parseMetadata(form.getMetadata());
        InstanceOperationInfo instanceOperationInfo =
            buildOperationInfo(buildCompositeServiceName(form),
                form.getConsistencyType(), targetInstances);
        
        List<String> operatedInstances = instanceService.batchUpdateMetadata(form.getNamespaceId(),
            instanceOperationInfo, targetMetadata);
        ArrayList<String> ipList = new ArrayList<>(operatedInstances);
        
        return Result.success(new InstanceMetadataBatchResult(ipList));
    }
    
    /** 批量删除实例元数据：已有键删除，不存在则跳过。 */
    @Since("3.0.0")
    @CanDistro
    @DeleteMapping("/metadata/batch")
    @TpsControl(pointName = "NamingInstanceMetadataUpdate",
        name = "HttpNamingInstanceMetadataBatchUpdate")
    @ExtractorManager.Extractor(httpExtractor = NamingInstanceMetadataBatchHttpParamExtractor.class)
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<InstanceMetadataBatchResult> batchDeleteInstanceMetadata(
        InstanceMetadataBatchOperationForm form)
        throws NacosException {
        form.validate();
        List<Instance> targetInstances = parseBatchInstances(form.getInstances());
        Map<String, String> targetMetadata = UtilsAndCommons.parseMetadata(form.getMetadata());
        InstanceOperationInfo instanceOperationInfo =
            buildOperationInfo(buildCompositeServiceName(form),
                form.getConsistencyType(), targetInstances);
        List<String> operatedInstances = instanceService.batchDeleteMetadata(form.getNamespaceId(),
            instanceOperationInfo, targetMetadata);
        ArrayList<String> ipList = new ArrayList<>(operatedInstances);
        
        return Result.success(new InstanceMetadataBatchResult(ipList));
    }
    
    /** 构建批量元数据操作上下文，补全默认集群名。 */
    private InstanceOperationInfo buildOperationInfo(String serviceName, String consistencyType,
        List<Instance> instances) {
        if (!CollectionUtils.isEmpty(instances)) {
            for (Instance instance : instances) {
                if (StringUtils.isBlank(instance.getClusterName())) {
                    instance.setClusterName(DEFAULT_CLUSTER_NAME);
                }
            }
        }
        return new InstanceOperationInfo(serviceName, consistencyType, instances);
    }
    
    /** 解析批量操作中的实例 JSON 列表，失败时返回空列表。 */
    private List<Instance> parseBatchInstances(String instances) {
        try {
            return JacksonUtils.toObj(instances, new TypeReference<List<Instance>>() {
            });
        } catch (Exception e) {
            Loggers.SRV_LOG
                .warn("UPDATE-METADATA: Param 'instances' is illegal, ignore this operation", e);
        }
        return Collections.emptyList();
    }
    
    /** 部分更新实例（仅修改提交的字段）。 */
    @Since("3.0.0")
    @CanDistro
    @PutMapping(value = "/partial")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> partialUpdateInstance(InstanceForm instanceForm) throws Exception {
        instanceForm.validate();
        InstancePatchObject patchObject =
            new InstancePatchObject(instanceForm.getClusterName(), instanceForm.getIp(),
                instanceForm.getPort());
        String metadata = instanceForm.getMetadata();
        if (StringUtils.isNotBlank(metadata)) {
            patchObject.setMetadata(UtilsAndCommons.parseMetadata(metadata));
        }
        Double weight = instanceForm.getWeight();
        if (weight != null) {
            NamingRequestUtil.checkWeight(weight);
            patchObject.setWeight(weight);
        }
        Boolean enabled = instanceForm.getEnabled();
        if (enabled != null) {
            patchObject.setEnabled(enabled);
        }
        instanceService.patchInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
            instanceForm.getServiceName(), patchObject);
        return Result.success("ok");
    }
    
    /** 获取指定服务的全部实例列表，可选仅返回健康实例。 */
    @Since("3.0.0")
    @GetMapping("/list")
    @TpsControl(pointName = "NamingServiceSubscribe", name = "HttpNamingServiceSubscribe")
    @ExtractorManager.Extractor(httpExtractor = NamingInstanceListHttpParamExtractor.class)
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<? extends Instance>> list(InstanceListForm instanceListForm)
        throws NacosException {
        instanceListForm.validate();
        List<? extends Instance> instances =
            catalogService.listInstances(instanceListForm.getNamespaceId(),
                instanceListForm.getGroupName(), instanceListForm.getServiceName(),
                instanceListForm.getClusterName());
        if (instanceListForm.getHealthyOnly()) {
            instances = instances.stream().filter(Instance::isHealthy).toList();
        }
        return Result.success(instances);
    }
    
    /** 获取指定实例的详细信息。 */
    @Since("3.0.0")
    @GetMapping
    @TpsControl(pointName = "NamingInstanceQuery", name = "HttpNamingInstanceQuery")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<Instance> detail(InstanceForm instanceForm) throws NacosException {
        instanceForm.validate();
        String namespaceId = instanceForm.getNamespaceId();
        String clusterName = instanceForm.getClusterName();
        String ip = instanceForm.getIp();
        int port = instanceForm.getPort();
        Instance instance = instanceService.getInstance(namespaceId, instanceForm.getGroupName(),
            instanceForm.getServiceName(), clusterName, ip, port);
        return Result.success(instance);
    }
    
    /** 拼接 group@@service 复合服务名。 */
    private String buildCompositeServiceName(InstanceMetadataBatchOperationForm form) {
        return NamingUtils.getGroupedName(form.getServiceName(), form.getGroupName());
    }
    
}
