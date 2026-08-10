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

package com.alibaba.nacos.core.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.core.cluster.health.ModuleHealthCheckerHolder;
import com.alibaba.nacos.core.cluster.health.ReadinessResult;
import com.alibaba.nacos.core.service.NacosServerStateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.alibaba.nacos.core.utils.Commons.NACOS_ADMIN_CORE_CONTEXT_V3;

/**
 * 服务端状态 HTTP 接口：运行态信息、存活探针（liveness）与就绪探针（readiness）。
 * Server state controller for admin API.
 *
 * @author xiweng.yy
 */
@NacosApi
@RestController
@RequestMapping(NACOS_ADMIN_CORE_CONTEXT_V3 + "/state")
public class ServerStateController {
    
    /** 服务端状态查询服务。 */
    private final NacosServerStateService stateService;
    
    /**
     * 注入状态服务。
     *
     * @param stateService 服务端状态服务
     */
    public ServerStateController(NacosServerStateService stateService) {
        this.stateService = stateService;
    }
    
    /**
     * 获取当前节点运行状态键值对。
     *
     * @return state key-value map.
     */
    @Since("3.0.0")
    @GetMapping()
    public Result<Map<String, String>> serverState() {
        return Result.success(stateService.getServerState());
    }
    
    /**
     * 存活探针：判断 Nacos 是否处于不可自愈的故障态（需重启恢复）。
     *
     * @return HTTP code equal to 200 indicates that Nacos is in right states. HTTP code equal to 500 indicates that
     * Nacos is in broken states.
     */
    @Since("3.0.0")
    @GetMapping("/liveness")
    public Result<String> liveness() {
        return Result.success("ok");
    }
    
    /**
     * 就绪探针：聚合各模块 {@link ModuleHealthCheckerHolder} 检查是否可接收流量。
     *
     * @return HTTP code equal to 200 indicates that Nacos is ready. HTTP code equal to 500 indicates that Nacos is not
     * ready.
     */
    @Since("3.0.0")
    @GetMapping("/readiness")
    public Result<String> readiness() throws NacosException {
        ReadinessResult result = ModuleHealthCheckerHolder.getInstance().checkReadiness();
        if (result.isSuccess()) {
            return Result.success("ok");
        }
        return Result.failure(result.getResultMessage());
    }
}
