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

package com.alibaba.nacos.console.controller.v3.core;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.NacosMember;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.proxy.core.ClusterProxy;
import com.alibaba.nacos.core.utils.Commons;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * 控制台 v3 集群管理 REST 控制器：提供集群节点列表查询等 HTTP 接口。
 * Controller for handling HTTP requests related to cluster operations.
 *
 * @author zhangyukun on:2024/8/16
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/core/cluster")
public class ConsoleClusterController {
    
    /** 集群操作代理，封装对 core 模块集群服务的远程/本地调用 */
    private final ClusterProxy clusterProxy;
    
    /**
     * 注入集群代理并构造控制器。
     * Constructs a new ConsoleClusterController with the provided ClusterProxy.
     *
     * @param clusterProxy 处理集群相关操作的代理组件
     */
    public ConsoleClusterController(ClusterProxy clusterProxy) {
        this.clusterProxy = clusterProxy;
    }
    
    /**
     * 控制台展示集群成员节点列表，可按 IP 关键字过滤。
     * The console displays the list of cluster members.
     *
     * @param ipKeyWord 节点 IP 搜索关键字（可选）
     * @return 匹配的集群成员集合
     */
    @Since("3.0.0")
    @GetMapping(value = "/nodes")
    @Secured(resource = Commons.NACOS_CORE_CONTEXT
        + "/cluster", action = ActionTypes.READ, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    public Result<Collection<NacosMember>> getNodeList(
        @RequestParam(value = "keyword", required = false) String ipKeyWord) throws NacosException {
        Collection<NacosMember> result = clusterProxy.getNodeList(ipKeyWord);
        return Result.success(result);
    }
}
