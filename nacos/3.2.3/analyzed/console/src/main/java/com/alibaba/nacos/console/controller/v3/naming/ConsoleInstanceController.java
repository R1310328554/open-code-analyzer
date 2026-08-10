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
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.builder.InstanceBuilder;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.proxy.naming.InstanceProxy;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.InstanceForm;
import com.alibaba.nacos.naming.model.form.InstanceListForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.naming.web.CanDistro;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制台 v3 服务实例 REST 控制器：分页列表、更新与注销持久化实例。
 * Controller for handling HTTP requests related to instance operations.
 *
 * @author zhangyukun on:2024/8/16
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/ns/instance")
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class ConsoleInstanceController {
    
    /** 实例操作代理，对接 naming 实例维护服务 */
    private final InstanceProxy instanceProxy;
    
    /**
     * 注入实例代理并构造控制器。
     * Constructs a new ConsoleInstanceController with the provided InstanceProxy.
     *
     * @param instanceProxy 处理实例相关操作的代理组件
     */
    public ConsoleInstanceController(InstanceProxy instanceProxy) {
        this.instanceProxy = instanceProxy;
    }
    
    /**
     * 分页查询指定服务下的实例列表。
     * List instances of special service.
     *
     * @param instanceForm 实例列表查询表单
     * @param pageForm     分页参数表单
     * @return 实例分页结果
     */
    @Since("3.0.0")
    @Secured(action = ActionTypes.READ, apiType = ApiType.CONSOLE_API)
    @RequestMapping("/list")
    public Result<Page<? extends Instance>> getInstanceList(InstanceListForm instanceForm,
        PageForm pageForm)
        throws NacosException {
        instanceForm.validate();
        pageForm.validate();
        Page<? extends Instance> instancePage =
            instanceProxy.listInstances(instanceForm.getNamespaceId(),
                instanceForm.getServiceName(), instanceForm.getGroupName(),
                instanceForm.getClusterName(),
                pageForm.getPageNo(), pageForm.getPageSize());
        return Result.success(instancePage);
    }
    
    /**
     * 更新实例元数据（权重、健康状态、元数据等），受 TPS 与 Distro 约束。
     * Update instance.
     */
    @Since("3.0.0")
    @CanDistro
    @PutMapping
    @TpsControl(pointName = "NamingInstanceUpdate", name = "HttpNamingInstanceUpdate")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> updateInstance(InstanceForm instanceForm) throws NacosException {
        // 校验请求参数
        instanceForm.validate();
        checkWeight(instanceForm.getWeight());
        // 由表单构建 {@link Instance} 对象
        Instance instance = buildInstance(instanceForm);
        instanceProxy.updateInstance(instanceForm, instance);
        return Result.success("ok");
    }
    
    /**
     * 注销持久化实例（控制台不支持注销临时实例）。
     * Remove instance.
     */
    @Since("3.2.2")
    @CanDistro
    @DeleteMapping
    @TpsControl(pointName = "NamingInstanceDeregister", name = "HttpNamingInstanceDeregister")
    @Secured(action = ActionTypes.WRITE, apiType = ApiType.CONSOLE_API)
    public Result<String> removeInstance(InstanceForm instanceForm) throws NacosException {
        // check param
        instanceForm.validate();
        checkDeleteInstanceEphemeral(instanceForm.getEphemeral());
        // build instance
        Instance instance = buildInstance(instanceForm);
        instanceProxy.removeInstance(instanceForm, instance);
        return Result.success("ok");
    }
    
    private void checkWeight(Double weight) throws NacosException {
        if (weight > com.alibaba.nacos.naming.constants.Constants.MAX_WEIGHT_VALUE
            || weight < com.alibaba.nacos.naming.constants.Constants.MIN_WEIGHT_VALUE) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(), ErrorCode.WEIGHT_ERROR,
                "instance format invalid: The weights range from "
                    + com.alibaba.nacos.naming.constants.Constants.MIN_WEIGHT_VALUE + " to "
                    + com.alibaba.nacos.naming.constants.Constants.MAX_WEIGHT_VALUE);
        }
    }
    
    private void checkDeleteInstanceEphemeral(Boolean ephemeral) throws NacosApiException {
        if (Boolean.TRUE.equals(ephemeral)) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Console only supports deregistering persistent instances");
        }
    }
    
    private Instance buildInstance(InstanceForm instanceForm) throws NacosException {
        Instance instance =
            InstanceBuilder.newBuilder().setServiceName(buildCompositeServiceName(instanceForm))
                .setIp(instanceForm.getIp()).setClusterName(instanceForm.getClusterName())
                .setPort(instanceForm.getPort()).setHealthy(instanceForm.getHealthy())
                .setWeight(instanceForm.getWeight()).setEnabled(instanceForm.getEnabled())
                .setMetadata(UtilsAndCommons.parseMetadata(instanceForm.getMetadata()))
                .setEphemeral(instanceForm.getEphemeral()).build();
        if (instanceForm.getEphemeral() == null) {
            // 控制台注册/更新时未指定 ephemeral 则默认为持久实例
            instance.setEphemeral(false);
        }
        return instance;
    }
    
    private String buildCompositeServiceName(InstanceForm instanceForm) {
        return NamingUtils.getGroupedName(instanceForm.getServiceName(),
            instanceForm.getGroupName());
    }
    
}
