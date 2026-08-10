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
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientPublisherInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientServiceInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientSubscriberInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientSummaryInfo;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.core.ClientService;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.ClientServiceForm;
import com.alibaba.nacos.naming.paramcheck.NamingDefaultHttpParamExtractor;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 命名客户端管理 v3 控制器。
 *
 * <p>提供客户端列表、详情、发布/订阅关系及 Distro 负责节点查询等管理 API。</p>
 *
 * @author Nacos
 */

@NacosApi
@RestController
@RequestMapping(UtilsAndCommons.CLIENT_CONTROLLER_V3_ADMIN_PATH)
@ExtractorManager.Extractor(httpExtractor = NamingDefaultHttpParamExtractor.class)
public class ClientControllerV3 {
    
    /** 客户端连接管理器，用于校验 clientId 是否存在。 */
    private final ClientManager clientManager;
    
    /** 客户端查询业务服务实现。 */
    private final ClientService clientServiceV2Impl;
    
    public ClientControllerV3(ClientManager clientManager, ClientService clientServiceV2Impl) {
        this.clientManager = clientManager;
        this.clientServiceV2Impl = clientServiceV2Impl;
    }
    
    /** 查询所有已连接客户端 ID 列表。 */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<String>> getClientList() {
        return Result.success(clientServiceV2Impl.getClientList());
    }
    
    /** 按 clientId 查询客户端摘要信息。 */
    @Since("3.0.0")
    @GetMapping()
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<ClientSummaryInfo> getClientDetail(@RequestParam("clientId") String clientId)
        throws NacosApiException {
        checkClientId(clientId);
        return Result.success(clientServiceV2Impl.getClientDetail(clientId));
    }
    
    /** 查询指定客户端已注册（发布）的服务列表。 */
    @Since("3.0.0")
    @GetMapping("/publish/list")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<ClientServiceInfo>> getPublishedServiceList(
        @RequestParam("clientId") String clientId)
        throws NacosApiException {
        checkClientId(clientId);
        return Result.success(clientServiceV2Impl.getPublishedServiceList(clientId));
    }
    
    /** 查询指定客户端已订阅的服务列表。 */
    @Since("3.0.0")
    @GetMapping("/subscribe/list")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<ClientServiceInfo>> getSubscribeServiceList(
        @RequestParam("clientId") String clientId)
        throws NacosApiException {
        checkClientId(clientId);
        return Result.success(clientServiceV2Impl.getSubscribeServiceList(clientId));
    }
    
    /** 查询已注册指定服务的客户端（发布者）列表。 */
    @Since("3.0.0")
    @GetMapping("/service/publisher/list")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<ClientPublisherInfo>> getPublishedClientList(
        ClientServiceForm clientServiceForm)
        throws NacosApiException {
        clientServiceForm.validate();
        return Result
            .success(clientServiceV2Impl.getPublishedClientList(clientServiceForm.getNamespaceId(),
                clientServiceForm.getGroupName(), clientServiceForm.getServiceName(),
                clientServiceForm.getIp(),
                clientServiceForm.getPort()));
    }
    
    /** 查询已订阅指定服务的客户端列表。 */
    @Since("3.0.0")
    @GetMapping("/service/subscriber/list")
    @Secured(action = ActionTypes.READ, apiType = ApiType.ADMIN_API)
    public Result<List<ClientSubscriberInfo>> getSubscribeClientList(
        ClientServiceForm clientServiceForm)
        throws NacosApiException {
        clientServiceForm.validate();
        return Result
            .success(clientServiceV2Impl.getSubscribeClientList(clientServiceForm.getNamespaceId(),
                clientServiceForm.getGroupName(), clientServiceForm.getServiceName(),
                clientServiceForm.getIp(),
                clientServiceForm.getPort()));
    }
    
    /** 根据客户端 IP 与端口查询 Distro 负责节点。 */
    @Since("3.0.0")
    @GetMapping("/distro")
    @Secured(resource = UtilsAndCommons.CLIENT_CONTROLLER_V3_ADMIN_PATH, action = ActionTypes.READ,
        apiType = ApiType.ADMIN_API)
    public Result<ObjectNode> getResponsibleServer4Client(@RequestParam String ip,
        @RequestParam String port) {
        return Result.success(clientServiceV2Impl.getResponsibleServer4Client(ip, port));
    }
    
    /** 校验 clientId 是否存在，不存在则抛出 404。 */
    private void checkClientId(String clientId) throws NacosApiException {
        if (!clientManager.contains(clientId)) {
            throw new NacosApiException(HttpStatus.NOT_FOUND.value(), ErrorCode.RESOURCE_NOT_FOUND,
                "clientId [ " + clientId + " ] not exist");
        }
    }
}
