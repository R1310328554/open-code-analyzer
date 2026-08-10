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

package com.alibaba.nacos.naming.remote.rpc.handler;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.remote.request.ServiceQueryRequest;
import com.alibaba.nacos.api.naming.remote.response.QueryServiceResponse;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.namespace.filter.NamespaceValidation;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.impl.ServiceQueryRequestParamExtractor;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.utils.NamingRequestUtil;
import com.alibaba.nacos.naming.utils.ServiceUtil;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.stereotype.Component;

/**
 * 服务实例查询 RPC 请求处理器。
 *
 * <p>从 {@link ServiceStorage} 读取实例列表，结合元数据与健康保护策略过滤后返回。</p>
 *
 * @author xiweng.yy
 */
@Since("2.0.0")
@Component
public class ServiceQueryRequestHandler
    extends RequestHandler<ServiceQueryRequest, QueryServiceResponse> {
    
    /** 服务实例存储索引。 */
    private final ServiceStorage serviceStorage;
    
    /** 服务元数据管理器，供健康保护使用。 */
    private final NamingMetadataManager metadataManager;
    
    /** 注入存储与元数据组件。 */
    public ServiceQueryRequestHandler(ServiceStorage serviceStorage,
        NamingMetadataManager metadataManager) {
        this.serviceStorage = serviceStorage;
        this.metadataManager = metadataManager;
    }
    
    @Override
    @NamespaceValidation
    @TpsControl(pointName = "RemoteNamingServiceQuery", name = "RemoteNamingServiceQuery")
    @Secured(action = ActionTypes.READ)
    @ExtractorManager.Extractor(rpcExtractor = ServiceQueryRequestParamExtractor.class)
    /** 查询实例并按集群、healthyOnly 与健康保护规则过滤。 */
    public QueryServiceResponse handle(ServiceQueryRequest request, RequestMeta meta)
        throws NacosException {
        String namespaceId = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        Service service = Service.newService(namespaceId, groupName, serviceName);
        String cluster = null == request.getCluster() ? "" : request.getCluster();
        boolean healthyOnly = request.isHealthyOnly();
        ServiceInfo result = serviceStorage.getData(service);
        ServiceMetadata serviceMetadata = metadataManager.getServiceMetadata(service).orElse(null);
        result = ServiceUtil.selectInstancesWithHealthyProtection(result, serviceMetadata, cluster,
            healthyOnly, true,
            NamingRequestUtil.getSourceIpForGrpcRequest(meta));
        return QueryServiceResponse.buildSuccessResponse(result);
    }
}
