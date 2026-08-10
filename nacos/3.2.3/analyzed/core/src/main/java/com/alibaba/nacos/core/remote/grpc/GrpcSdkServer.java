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

package com.alibaba.nacos.core.remote.grpc;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.core.remote.grpc.filter.NacosGrpcServerTransportFilter;
import com.alibaba.nacos.core.remote.grpc.filter.NacosGrpcServerTransportFilterServiceLoader;
import com.alibaba.nacos.core.remote.grpc.interceptor.NacosGrpcServerInterceptor;
import com.alibaba.nacos.core.remote.grpc.interceptor.NacosGrpcServerInterceptorServiceLoader;
import com.alibaba.nacos.core.remote.grpc.negotiator.SdkProtocolNegotiatorBuilderSingleton;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;
import io.grpc.ServerInterceptor;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.shaded.io.grpc.netty.InternalProtocolNegotiator;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SDK 客户端 gRPC RPC 服务端：使用 SDK 端口偏移、SDK 协议协商器
 * 及 SDK 专用拦截器/传输过滤器，调用来源为 SDK。
 * Grpc implementation as  a rpc server.
 *
 * @author liuzunfei
 * @version $Id: BaseGrpcServer.java, v 0.1 2020年07月13日 3:42 PM liuzunfei Exp $
 */
@Service
public class GrpcSdkServer extends BaseGrpcServer {
    
    /** 返回 SDK gRPC 端口相对主端口的偏移量。 */
    @Override
    public int rpcPortOffset() {
        return Constants.SDK_GRPC_PORT_DEFAULT_OFFSET;
    }
    
    /** 返回 SDK RPC 专用线程池。 */
    @Override
    public ThreadPoolExecutor getRpcExecutor() {
        return GlobalExecutor.sdkRpcExecutor;
    }
    
    /** 读取 SDK KeepAlive 配置。 */
    @Override
    protected long getKeepAliveTime() {
        Long property = EnvUtil
            .getProperty(GrpcServerConstants.GrpcConfig.SDK_KEEP_ALIVE_TIME_PROPERTY, Long.class);
        if (property != null) {
            return property;
        }
        return super.getKeepAliveTime();
    }
    
    /** 读取 SDK KeepAlive 超时配置。 */
    @Override
    protected long getKeepAliveTimeout() {
        Long property = EnvUtil.getProperty(
            GrpcServerConstants.GrpcConfig.SDK_KEEP_ALIVE_TIMEOUT_PROPERTY, Long.class);
        if (property != null) {
            return property;
        }
        
        return super.getKeepAliveTimeout();
    }
    
    /** 读取 SDK 入站消息大小上限。 */
    @Override
    protected int getMaxInboundMessageSize() {
        Integer property =
            EnvUtil.getProperty(GrpcServerConstants.GrpcConfig.SDK_MAX_INBOUND_MSG_SIZE_PROPERTY,
                Integer.class);
        if (property != null) {
            return property;
        }
        
        int size = super.getMaxInboundMessageSize();
        
        if (Loggers.REMOTE.isWarnEnabled()) {
            Loggers.REMOTE.warn(
                "Recommended use '{}' property instead '{}', now property value is {}",
                GrpcServerConstants.GrpcConfig.SDK_MAX_INBOUND_MSG_SIZE_PROPERTY,
                GrpcServerConstants.GrpcConfig.MAX_INBOUND_MSG_SIZE_PROPERTY, size);
        }
        
        return size;
    }
    
    /** 读取 SDK 允许 KeepAlive 最小间隔。 */
    @Override
    protected long getPermitKeepAliveTime() {
        Long property = EnvUtil
            .getProperty(GrpcServerConstants.GrpcConfig.SDK_PERMIT_KEEP_ALIVE_TIME, Long.class);
        if (property != null) {
            return property;
        }
        return super.getPermitKeepAliveTime();
    }
    
    /** 构建 SDK TLS/协议协商器。 */
    @Override
    protected Optional<InternalProtocolNegotiator.ProtocolNegotiator> newProtocolNegotiator() {
        protocolNegotiator = SdkProtocolNegotiatorBuilderSingleton.getSingleton().build();
        return Optional.ofNullable(protocolNegotiator);
    }
    
    /** 叠加 SDK 专用 gRPC 拦截器。 */
    @Override
    protected List<ServerInterceptor> getSeverInterceptors() {
        List<ServerInterceptor> result = new LinkedList<>();
        result.addAll(super.getSeverInterceptors());
        result.addAll(NacosGrpcServerInterceptorServiceLoader.loadServerInterceptors(
            NacosGrpcServerInterceptor.SDK_INTERCEPTOR));
        return result;
    }
    
    /** 叠加 SDK 专用传输过滤器。 */
    @Override
    protected List<ServerTransportFilter> getServerTransportFilters() {
        List<ServerTransportFilter> result = new LinkedList<>();
        result.addAll(super.getServerTransportFilters());
        result.addAll(NacosGrpcServerTransportFilterServiceLoader.loadServerTransportFilters(
            NacosGrpcServerTransportFilter.SDK_FILTER));
        return result;
    }
    
    /** 返回 SDK 调用来源标签。 */
    @Override
    protected String getSource() {
        return RemoteConstants.LABEL_SOURCE_SDK;
    }
}
