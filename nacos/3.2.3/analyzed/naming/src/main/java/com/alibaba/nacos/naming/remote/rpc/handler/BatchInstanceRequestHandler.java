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
import com.alibaba.nacos.api.naming.remote.NamingRemoteConstants;
import com.alibaba.nacos.api.naming.remote.request.BatchInstanceRequest;
import com.alibaba.nacos.api.naming.remote.response.BatchInstanceResponse;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.trace.event.naming.BatchRegisterInstanceTraceEvent;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.namespace.filter.NamespaceValidation;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.core.paramcheck.impl.BatchInstanceRequestParamExtractor;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.core.v2.service.impl.EphemeralClientOperationServiceImpl;
import com.alibaba.nacos.naming.utils.InstanceUtil;
import com.alibaba.nacos.naming.utils.NamingRequestUtil;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import org.springframework.stereotype.Component;

/**
 * 批量实例注册 RPC 请求处理器。
 *
 * <p>处理 {@link BatchInstanceRequest}，当前支持 {@link NamingRemoteConstants#BATCH_REGISTER_INSTANCE}；经命名空间校验、TPS 限流与鉴权后，委托 {@link EphemeralClientOperationServiceImpl#batchRegisterInstance} 批量注册临时实例并发布链路追踪事件。</p>
 *
 * @author <a href="mailto:chenhao26@xiaomi.com">chenhao26</a>
 */
@Since("2.1.1")
@Component("batchInstanceRequestHandler")
public class BatchInstanceRequestHandler
    extends RequestHandler<BatchInstanceRequest, BatchInstanceResponse> {
    
    private final EphemeralClientOperationServiceImpl clientOperationService;
    
    public BatchInstanceRequestHandler(EphemeralClientOperationServiceImpl clientOperationService) {
        this.clientOperationService = clientOperationService;
    }
    
    @Override
    @NamespaceValidation
    @TpsControl(pointName = "RemoteNamingInstanceBatchRegister",
        name = "RemoteNamingInstanceBatchRegister")
    @Secured(action = ActionTypes.WRITE)
    @ExtractorManager.Extractor(rpcExtractor = BatchInstanceRequestParamExtractor.class)
    /** 解析请求、补全 instanceId 并按 type 分发批量注册逻辑。 */
    public BatchInstanceResponse handle(BatchInstanceRequest request, RequestMeta meta)
        throws NacosException {
        Service service = Service.newService(request.getNamespace(), request.getGroupName(),
            request.getServiceName(),
            true);
        InstanceUtil.batchSetInstanceIdIfEmpty(request.getInstances(),
            service.getGroupedServiceName());
        switch (request.getType()) {
            case NamingRemoteConstants.BATCH_REGISTER_INSTANCE:
                return batchRegisterInstance(service, request, meta);
            default:
                throw new NacosException(NacosException.INVALID_PARAM,
                    String.format("Unsupported request type %s", request.getType()));
        }
    }
    
    /** 执行批量注册并返回 BATCH_REGISTER_INSTANCE 响应。 */
    private BatchInstanceResponse batchRegisterInstance(Service service,
        BatchInstanceRequest request,
        RequestMeta meta) {
        clientOperationService.batchRegisterInstance(service, request.getInstances(),
            meta.getConnectionId());
        publishBatchRegisterInstanceTraceEvent(service, request, meta);
        return new BatchInstanceResponse(NamingRemoteConstants.BATCH_REGISTER_INSTANCE);
    }
    
    /** 为每个实例发布 {@link BatchRegisterInstanceTraceEvent} 链路事件。 */
    private void publishBatchRegisterInstanceTraceEvent(Service service,
        BatchInstanceRequest request,
        RequestMeta meta) {
        long eventTime = System.currentTimeMillis();
        String clientIp = NamingRequestUtil.getSourceIpForGrpcRequest(meta);
        request.getInstances().forEach(instance -> NotifyCenter.publishEvent(
            new BatchRegisterInstanceTraceEvent(eventTime, clientIp, true, service.getNamespace(),
                service.getGroup(), service.getName(), instance.getIp(), instance.getPort())));
    }
}
