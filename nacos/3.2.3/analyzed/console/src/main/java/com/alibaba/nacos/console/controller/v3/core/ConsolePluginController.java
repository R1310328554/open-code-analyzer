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

package com.alibaba.nacos.console.controller.v3.core;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.paramcheck.ConsoleDefaultHttpParamExtractor;
import com.alibaba.nacos.console.proxy.core.PluginProxy;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.plugin.model.form.PluginConfigForm;
import com.alibaba.nacos.core.plugin.model.vo.PluginDetailVO;
import com.alibaba.nacos.core.plugin.model.vo.PluginInfoVO;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 控制台 v3 插件管理 REST 控制器：查询、启停、配置插件及查看集群可用性。
 * Controller for handling HTTP requests related to plugin operations.
 *
 * @author WangzJi
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/plugin")
@ExtractorManager.Extractor(httpExtractor = ConsoleDefaultHttpParamExtractor.class)
public class ConsolePluginController {
    
    /** 插件操作代理，对接 core 插件管理服务 */
    private final PluginProxy pluginProxy;
    
    /** 注入插件代理并构造控制器 */
    public ConsolePluginController(PluginProxy pluginProxy) {
        this.pluginProxy = pluginProxy;
    }
    
    /**
     * 获取插件列表，可按插件类型过滤。
     * Get plugin list.
     *
     * @param pluginType 插件类型过滤条件（可选）
     * @return 插件信息列表
     */
    @Since("3.2.0")
    @GetMapping("/list")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "plugins", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<List<PluginInfoVO>> getPluginList(
        @RequestParam(value = "pluginType", required = false) String pluginType)
        throws NacosException {
        return Result.success(pluginProxy.listPlugins(pluginType));
    }
    
    /**
     * 查询指定插件的详细配置与状态。
     * Get plugin detail.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @return 插件详情视图
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "plugins", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<PluginDetailVO> getPluginDetail(@RequestParam("pluginType") String pluginType,
        @RequestParam("pluginName") String pluginName) throws NacosException {
        return Result.success(pluginProxy.getPluginDetail(pluginType, pluginName));
    }
    
    /**
     * 启用或禁用指定插件，支持仅本地节点生效。
     * Enable or disable plugin.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @param enabled    是否启用
     * @return 操作成功提示
     */
    @Since("3.2.0")
    @PutMapping("/status")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "plugins", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<String> updatePluginStatus(@RequestParam("pluginType") String pluginType,
        @RequestParam("pluginName") String pluginName, @RequestParam("enabled") boolean enabled,
        @RequestParam(value = "localOnly", defaultValue = "false") boolean localOnly)
        throws NacosException {
        pluginProxy.updatePluginStatus(pluginType, pluginName, enabled, localOnly);
        return Result.success("Plugin status updated successfully");
    }
    
    /**
     * 更新插件运行配置（校验类型、名称与配置体非空）。
     * Update plugin configuration.
     *
     * @param form 插件配置表单
     * @return 操作成功提示
     */
    @Since("3.2.0")
    @PutMapping("/config")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "plugins", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<String> updatePluginConfig(PluginConfigForm form) throws NacosException {
        if (StringUtils.isBlank(form.getPluginType())
            || StringUtils.isBlank(form.getPluginName())) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Plugin type and name are required");
        }
        if (form.getConfig() == null) {
            throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                ErrorCode.PARAMETER_VALIDATE_ERROR,
                "Plugin configuration is required");
        }
        pluginProxy.updatePluginConfig(form.getPluginType(), form.getPluginName(), form.getConfig(),
            form.isLocalOnly());
        return Result.success("Plugin configuration updated successfully");
    }
    
    /**
     * 查询插件在各集群节点上的可用性分布。
     * Get plugin availability across cluster nodes.
     *
     * @param pluginType 插件类型
     * @param pluginName 插件名称
     * @return 节点地址到可用性布尔值的映射
     */
    @Since("3.2.0")
    @GetMapping("/availability")
    @Secured(resource = Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX
        + "plugins", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Map<String, Boolean>> getPluginAvailability(
        @RequestParam("pluginType") String pluginType,
        @RequestParam("pluginName") String pluginName) throws NacosException {
        return Result.success(pluginProxy.getPluginAvailability(pluginType, pluginName));
    }
}
