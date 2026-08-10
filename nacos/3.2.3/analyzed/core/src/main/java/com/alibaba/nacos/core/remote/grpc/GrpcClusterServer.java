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
import com.alibaba.nacos.core.remote.grpc.negotiator.ClusterProtocolNegotiatorBuilderSingleton;
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
 * 集群节点间 gRPC RPC 服务端：使用集群端口偏移、集群协议协商器
 * 及集群专用拦截器/传输过滤器，调用来源为 CLUSTER。
 * Grpc implementation as  a rpc server.
 *
 * @author liuzunfei
 * @version $Id: BaseGrpcServer.java, v 0.1 2020年07月13日 3:42 PM liuzunfei Exp $
 */
@Service
public class GrpcClusterServer extends BaseGrpcServer {
    
    /** 返回集群 gRPC 端口相对主端口的偏移量。 */
    @Override
    public int rpcPortOffset() {
        return Constants.CLUSTER_GRPC_PORT_DEFAULT_OFFSET;
    }
    
    /** 返回集群 RPC 专用线程池。 */
    @Override
    public ThreadPoolExecutor getRpcExecutor() {
        if (!GlobalExecutor.clusterRpcExecutor.allowsCoreThreadTimeOut()) {
            GlobalExecutor.clusterRpcExecutor.allowCoreThreadTimeOut(true);
        }
        return GlobalExecutor.clusterRpcExecutor;
    }
    
    /** 读取集群 KeepAlive 配置，缺省使用父类默认值。 */
    @Override
    protected long getKeepAliveTime() {
        Long property =
            EnvUtil.getProperty(GrpcServerConstants.GrpcConfig.CLUSTER_KEEP_ALIVE_TIME_PROPERTY,
                Long.class);
        if (property != null) {
            return property;
        }
        return super.getKeepAliveTime();
    }
    
    /** 读取集群 KeepAlive 超时配置。 */
    @Override
    protected long getKeepAliveTimeout() {
        Long property =
            EnvUtil.getProperty(GrpcServerConstants.GrpcConfig.CLUSTER_KEEP_ALIVE_TIMEOUT_PROPERTY,
                Long.class);
        if (property != null) {
            return property;
        }
        return super.getKeepAliveTimeout();
    }
    
    /** 构建集群 TLS/协议协商器。 */
    @Override
    protected Optional<InternalProtocolNegotiator.ProtocolNegotiator> newProtocolNegotiator() {
        protocolNegotiator = ClusterProtocolNegotiatorBuilderSingleton.getSingleton().build();
        return Optional.ofNullable(protocolNegotiator);
    }
    
    /** 读取集群允许 KeepAlive 最小间隔。 */
    @Override
    protected long getPermitKeepAliveTime() {
        Long property = EnvUtil
            .getProperty(GrpcServerConstants.GrpcConfig.CLUSTER_PERMIT_KEEP_ALIVE_TIME, Long.class);
        if (property != null) {
            return property;
        }
        return super.getPermitKeepAliveTime();
    }
    
    /** 读取集群入站消息大小上限。 */
    @Override
    protected int getMaxInboundMessageSize() {
        Integer property = EnvUtil.getProperty(
            GrpcServerConstants.GrpcConfig.CLUSTER_MAX_INBOUND_MSG_SIZE_PROPERTY,
            Integer.class);
        if (property != null) {
            return property;
        }
        
        int size = super.getMaxInboundMessageSize();
        if (Loggers.REMOTE.isWarnEnabled()) {
            Loggers.REMOTE.warn(
                "Recommended use '{}' property instead '{}', now property value is {}",
                GrpcServerConstants.GrpcConfig.CLUSTER_MAX_INBOUND_MSG_SIZE_PROPERTY,
                GrpcServerConstants.GrpcConfig.MAX_INBOUND_MSG_SIZE_PROPERTY, size);
        }
        return size;
    }
    
    /** 叠加集群专用 gRPC 拦截器。 */
    @Override
    protected List<ServerInterceptor> getSeverInterceptors() {
        List<ServerInterceptor> result = new LinkedList<>();
        result.addAll(super.getSeverInterceptors());
        result.addAll(NacosGrpcServerInterceptorServiceLoader.loadServerInterceptors(
            NacosGrpcServerInterceptor.CLUSTER_INTERCEPTOR));
        return result;
    }
    
    /** 叠加集群专用传输过滤器。 */
    @Override
    protected List<ServerTransportFilter> getServerTransportFilters() {
        List<ServerTransportFilter> result = new LinkedList<>();
        result.addAll(super.getServerTransportFilters());
        result.addAll(NacosGrpcServerTransportFilterServiceLoader.loadServerTransportFilters(
            NacosGrpcServerTransportFilter.CLUSTER_FILTER));
        return result;
    }
    
    /** 返回 CLUSTER 调用来源标签。 */
    @Override
    protected String getSource() {
        return RemoteConstants.LABEL_SOURCE_CLUSTER;
    }
}
