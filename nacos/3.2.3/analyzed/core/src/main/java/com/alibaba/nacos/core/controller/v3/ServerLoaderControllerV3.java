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

package com.alibaba.nacos.core.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.model.response.ServerLoaderMetrics;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.service.NacosServerLoaderService;
import com.alibaba.nacos.core.utils.WebUtils;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.alibaba.nacos.core.utils.Commons.NACOS_ADMIN_CORE_CONTEXT_V3;

/**
 * 服务端连接负载均衡 HTTP 接口 v3：查看 SDK 连接、手动/智能重平衡与单连接迁移。
 * controller to control server loader v3.
 *
 * @author yunye
 * @since 3.0.0
 */
@NacosApi
@RestController
@RequestMapping(NACOS_ADMIN_CORE_CONTEXT_V3 + "/loader")
public class ServerLoaderControllerV3 {
    
    /** 本 Controller 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLoaderControllerV3.class);
    
    /** 服务端连接负载服务。 */
    private final NacosServerLoaderService serverLoaderService;
    
    /**
     * 注入连接负载服务。
     *
     * @param serverLoaderService 负载均衡服务
     */
    public ServerLoaderControllerV3(NacosServerLoaderService serverLoaderService) {
        this.serverLoaderService = serverLoaderService;
    }
    
    /**
     * 获取本节点当前全部 SDK 客户端连接。
     *
     * @return state json.
     */
    @Since("3.0.0")
    @GetMapping("/current")
    @Secured(resource = NACOS_ADMIN_CORE_CONTEXT_V3 + "/loader", action = ActionTypes.READ,
        apiType = ApiType.ADMIN_API)
    public Result<Map<String, Connection>> currentClients() {
        return Result.success(serverLoaderService.getAllClients());
    }
    
    /**
     * 将本节点 SDK 连接数重平衡到指定数量，可选重定向地址。
     *
     * @return state json.
     */
    @Secured(resource = NACOS_ADMIN_CORE_CONTEXT_V3
        + "/loader", action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    @Since("3.0.0")
    @PostMapping("/reloadCurrent")
    public Result<String> reloadCount(@RequestParam Integer count,
        @RequestParam(value = "redirectAddress", required = false) String redirectAddress) {
        serverLoaderService.reloadCount(count, redirectAddress);
        return Result.success();
    }
    
    /**
     * 按集群各节点 SDK 连接总数智能重平衡各节点负载。
     *
     * @return state json.
     */
    @Since("3.0.0")
    @PostMapping("/smartReloadCluster")
    @Secured(resource = NACOS_ADMIN_CORE_CONTEXT_V3
        + "/loader", action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> smartReload(HttpServletRequest request,
        @RequestParam(value = "loaderFactor", defaultValue = "0.1f") String loaderFactorStr) {
        LOGGER.info("Smart reload request receive,requestIp={}", WebUtils.getRemoteIp(request));
        float loaderFactor = Float.parseFloat(loaderFactorStr);
        if (!serverLoaderService.smartReload(loaderFactor)) {
            return Result.failure(ErrorCode.SERVER_ERROR,
                "Smart reload failed, please try again later.");
        }
        return Result.success();
    }
    
    /**
     * 按连接 ID 向指定 SDK 连接发送 ConnectReset 请求以迁移连接。
     *
     * @return state json.
     */
    @Since("3.0.0")
    @PostMapping("/reloadClient")
    @Secured(resource = NACOS_ADMIN_CORE_CONTEXT_V3
        + "/loader", action = ActionTypes.WRITE, apiType = ApiType.ADMIN_API)
    public Result<String> reloadSingle(@RequestParam String connectionId,
        @RequestParam(value = "redirectAddress", required = false) String redirectAddress) {
        serverLoaderService.reloadClient(connectionId, redirectAddress);
        return Result.success();
    }
    
    /**
     * Get current clients.
     *
     * @return state json.
      * <p>连接负载 v3 接口；详见类级说明。</p>
     */
    /**
     * 获取集群级连接负载指标。
     *
     * @return 负载指标
     */
    @Since("3.0.0")
    @GetMapping("/cluster")
    @Secured(resource = NACOS_ADMIN_CORE_CONTEXT_V3 + "/loader", action = ActionTypes.READ,
        apiType = ApiType.ADMIN_API)
    public Result<ServerLoaderMetrics> loaderMetrics() {
        return Result.success(serverLoaderService.getServerLoaderMetrics());
    }
}
