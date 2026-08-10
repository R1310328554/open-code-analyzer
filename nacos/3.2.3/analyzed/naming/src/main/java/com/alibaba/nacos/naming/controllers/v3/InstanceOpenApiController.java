/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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
import com.alibaba.nacos.api.common.ResponseCode;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.trace.DeregisterInstanceReason;
import com.alibaba.nacos.common.trace.event.naming.DeregisterInstanceTraceEvent;
import com.alibaba.nacos.common.trace.event.naming.RegisterInstanceTraceEvent;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.core.InstanceOperator;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.InstanceForm;
import com.alibaba.nacos.naming.model.form.InstanceListForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.naming.paramcheck.NamingInstanceListHttpParamExtractor;
import com.alibaba.nacos.naming.pojo.instance.BeatInfoInstanceBuilder;
import com.alibaba.nacos.naming.utils.InstanceUtil;
import com.alibaba.nacos.naming.utils.NamingRequestUtil;
import com.alibaba.nacos.naming.web.CanDistro;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 命名模块客户端 HTTP Open API 控制器。
 *
 * <p>面向不支持 gRPC 的语言，提供实例注册/注销/心跳及实例列表查询；不支持 HTTP 订阅，订阅请使用 gRPC。</p>
 *
 * @author xiweng.yy
 */
@NacosApi
@RestController
@RequestMapping(UtilsAndCommons.INSTANCE_V3_CLIENT_API_PATH)
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class InstanceOpenApiController {
    
    /** 实例操作入口（客户端 Open API 专用实现）。 */
    private final InstanceOperator instanceOperator;
    
    /** 命名开关域，提供默认临时实例等配置。 */
    private final SwitchDomain switchDomain;
    
    public InstanceOpenApiController(InstanceOperator instanceOperator, SwitchDomain switchDomain) {
        this.instanceOperator = instanceOperator;
        this.switchDomain = switchDomain;
    }
    
    /**
     * 注册实例或发送心跳到 Nacos。
     *
     * @param instanceForm 实例表单
     * @param heartBeat 是否为心跳请求
     * @return 注册或心跳结果；心跳时实例不存在返回错误码 21003，提示需先注册
     * @throws NacosException 注册或心跳异常
     */
    @Since("3.0.0")
    @CanDistro
    @PostMapping
    @TpsControl(pointName = "NamingInstanceRegister", name = "HttpNamingInstanceRegister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.OPEN_API)
    public Result<String> register(InstanceForm instanceForm,
        @RequestParam(defaultValue = "false") boolean heartBeat)
        throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        if (heartBeat) {
            if (ResponseCode.OK != doHeartBeat(instanceForm)) {
                return Result.failure(ErrorCode.INSTANCE_NOT_FOUND, null);
            }
        } else {
            doRegisterInstance(instanceForm);
        }
        return Result.success("ok");
    }
    
    /**
     * 从 Nacos 注销实例。
     *
     * @param instanceForm 实例表单
     * @return 注销结果；实例不存在时也返回成功
     * @throws NacosException 注销异常
     */
    @Since("3.0.0")
    @CanDistro
    @DeleteMapping
    @TpsControl(pointName = "NamingInstanceDeregister", name = "HttpNamingInstanceDeregister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.OPEN_API)
    public Result<String> deregister(InstanceForm instanceForm) throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        Instance instance =
            InstanceUtil.buildInstance(instanceForm, switchDomain.isDefaultInstanceEphemeral());
        instanceOperator.removeInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
            instanceForm.getServiceName(), instance);
        NotifyCenter.publishEvent(
            new DeregisterInstanceTraceEvent(System.currentTimeMillis(),
                NamingRequestUtil.getSourceIp(), false,
                DeregisterInstanceReason.REQUEST, instanceForm.getNamespaceId(),
                instanceForm.getGroupName(),
                instanceForm.getServiceName(), instance.getIp(), instance.getPort()));
        return Result.success("ok");
    }
    
    /**
     * 获取指定服务的全部可用实例列表。
     *
     * <p>不返回 {@code enabled=false} 的下线实例，供自定义客户端拉取。</p>
     *
     * @param instanceForm 订阅者表单（ip/port 为订阅方，其余为目标服务信息）
     * @return 不含下线实例的实例列表
     * @throws Exception 查询过程中的异常
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @TpsControl(pointName = "NamingServiceSubscribe", name = "HttpNamingServiceSubscribe")
    @Secured(action = ActionTypes.READ, apiType = ApiType.OPEN_API)
    @ExtractorManager.Extractor(httpExtractor = NamingInstanceListHttpParamExtractor.class)
    public Result<List<Instance>> list(InstanceListForm instanceForm) throws Exception {
        // 校验请求参数
        instanceForm.validate();
        String namespaceId = instanceForm.getNamespaceId();
        String groupName = instanceForm.getGroupName();
        String serviceName = instanceForm.getServiceName();
        ServiceInfo serviceInfo =
            instanceOperator.listInstance(namespaceId, groupName, serviceName, null,
                instanceForm.getClusterName(), false);
        return Result.success(serviceInfo.getHosts());
    }
    
    /** 处理心跳请求，返回响应码。 */
    private int doHeartBeat(InstanceForm instanceForm) throws NacosException {
        BeatInfoInstanceBuilder builder = BeatInfoInstanceBuilder.newBuilder();
        return instanceOperator.handleBeat(instanceForm.getNamespaceId(),
            instanceForm.getGroupName(),
            instanceForm.getServiceName(), instanceForm.getIp(), instanceForm.getPort(),
            instanceForm.getClusterName(), null, builder);
    }
    
    /** 校验权重并注册实例，发布注册追踪事件。 */
    private void doRegisterInstance(InstanceForm instanceForm) throws NacosException {
        NamingRequestUtil.checkWeight(instanceForm.getWeight());
        Instance instance =
            InstanceUtil.buildInstance(instanceForm, switchDomain.isDefaultInstanceEphemeral());
        String namespaceId = instanceForm.getNamespaceId();
        String groupName = instanceForm.getGroupName();
        String serviceName = instanceForm.getServiceName();
        instanceOperator.registerInstance(namespaceId, groupName, serviceName, instance);
        NotifyCenter.publishEvent(
            new RegisterInstanceTraceEvent(System.currentTimeMillis(),
                NamingRequestUtil.getSourceIp(), false,
                namespaceId, groupName, serviceName, instance.getIp(), instance.getPort()));
    }
}
