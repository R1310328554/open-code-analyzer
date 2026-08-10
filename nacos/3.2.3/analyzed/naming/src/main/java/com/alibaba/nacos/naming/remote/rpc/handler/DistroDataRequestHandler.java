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
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.distributed.distro.DistroProtocol;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.core.remote.grpc.InvokeSource;
import com.alibaba.nacos.naming.cluster.remote.request.DistroDataRequest;
import com.alibaba.nacos.naming.cluster.remote.response.DistroDataResponse;
import com.alibaba.nacos.naming.consistency.ephemeral.distro.v2.DistroClientDataProcessor;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

/**
 * 集群 Distro 数据同步 RPC 请求处理器。
 *
 * <p>处理 VERIFY、SNAPSHOT、ADD/CHANGE/DELETE、QUERY 等 {@link DistroDataRequest} 操作，委托 {@link DistroProtocol} 完成临时客户端数据校验、快照与增量同步。</p>
 *
 * @author xiweng.yy
 */
@Since("2.0.0")
@InvokeSource(source = {RemoteConstants.LABEL_SOURCE_CLUSTER})
@Component
public class DistroDataRequestHandler
    extends RequestHandler<DistroDataRequest, DistroDataResponse> {
    
    /** Distro 协议核心，负责数据校验、接收与查询。 */
    private final DistroProtocol distroProtocol;
    
    /** 注入 Distro 协议实现。 */
    public DistroDataRequestHandler(DistroProtocol distroProtocol) {
        this.distroProtocol = distroProtocol;
    }
    
    @Override
    @Secured(apiType = ApiType.INNER_API)
    /** 按 {@link DistroDataRequest#getDataOperation()} 分发到对应处理分支。 */
    public DistroDataResponse handle(DistroDataRequest request, RequestMeta meta)
        throws NacosException {
        try {
            switch (request.getDataOperation()) {
                case VERIFY:
                    return handleVerify(request.getDistroData(), meta);
                case SNAPSHOT:
                    return handleSnapshot();
                case ADD:
                case CHANGE:
                case DELETE:
                    return handleSyncData(request.getDistroData());
                case QUERY:
                    return handleQueryData(request.getDistroData());
                default:
                    return new DistroDataResponse();
            }
        } catch (Exception e) {
            Loggers.DISTRO.error("[DISTRO-FAILED] distro handle with exception", e);
            DistroDataResponse result = new DistroDataResponse();
            result.setResultCode(ResponseCode.FAIL.getCode());
            result.setErrorCode(ResponseCode.FAIL.getCode());
            result.setMessage("handle distro request with exception");
            return result;
        }
    }
    
    /** 校验对端 Distro 数据摘要是否与本地一致。 */
    private DistroDataResponse handleVerify(DistroData distroData, RequestMeta meta) {
        DistroDataResponse result = new DistroDataResponse();
        if (!distroProtocol.onVerify(distroData, meta.getClientIp())) {
            result.setErrorInfo(ResponseCode.FAIL.getCode(),
                "[DISTRO-FAILED] distro data verify failed");
        }
        return result;
    }
    
    /** 导出 {@link DistroClientDataProcessor#TYPE} 类型客户端的全量快照。 */
    private DistroDataResponse handleSnapshot() {
        DistroDataResponse result = new DistroDataResponse();
        DistroData distroData = distroProtocol.onSnapshot(DistroClientDataProcessor.TYPE);
        result.setDistroData(distroData);
        return result;
    }
    
    /** 接收并应用 ADD/CHANGE/DELETE 增量 Distro 数据。 */
    private DistroDataResponse handleSyncData(DistroData distroData) {
        DistroDataResponse result = new DistroDataResponse();
        if (!distroProtocol.onReceive(distroData)) {
            result.setErrorCode(ResponseCode.FAIL.getCode());
            result.setMessage("[DISTRO-FAILED] distro data handle failed");
        }
        return result;
    }
    
    /** 按 {@link DistroKey} 查询本地 Distro 数据。 */
    private DistroDataResponse handleQueryData(DistroData distroData) {
        DistroDataResponse result = new DistroDataResponse();
        DistroKey distroKey = distroData.getDistroKey();
        DistroData queryData = distroProtocol.onQuery(distroKey);
        result.setDistroData(queryData);
        return result;
    }
}
