/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.remote.ConnectionManager;
import com.alibaba.nacos.core.utils.Loggers;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;

import java.net.InetSocketAddress;

import static com.alibaba.nacos.core.remote.grpc.GrpcServerConstants.ATTR_TRANS_KEY_CONN_ID;
import static com.alibaba.nacos.core.remote.grpc.GrpcServerConstants.ATTR_TRANS_KEY_LOCAL_PORT;
import static com.alibaba.nacos.core.remote.grpc.GrpcServerConstants.ATTR_TRANS_KEY_REMOTE_IP;
import static com.alibaba.nacos.core.remote.grpc.GrpcServerConstants.ATTR_TRANS_KEY_REMOTE_PORT;

/**
 * gRPC 传输层过滤器：在连接就绪时解析远端/本地地址并生成 connectionId，
 * 连接终止时注销 {@link ConnectionManager} 中的连接。
 * AddressTransportFilter process remote address, local address and connection id attributes.
 *
 * @author Weizhan▪Yun
 * @date 2023/1/5 15:45
 */
public class AddressTransportFilter extends ServerTransportFilter {
    
    /** 连接管理器，用于注销已终止连接。 */
    private final ConnectionManager connectionManager;
    
    /** 构造传输过滤器。
     * @param connectionManager 连接管理器
     */
    public AddressTransportFilter(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /** 传输就绪：写入 connectionId、远端/本地 IP 与端口属性。 */
    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        InetSocketAddress remoteAddress = (InetSocketAddress) transportAttrs
            .get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        InetSocketAddress localAddress = (InetSocketAddress) transportAttrs
            .get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        int remotePort = remoteAddress.getPort();
        int localPort = localAddress.getPort();
        String remoteIp = remoteAddress.getAddress().getHostAddress();
        Attributes attrWrapper = transportAttrs.toBuilder()
            .set(ATTR_TRANS_KEY_CONN_ID,
                System.currentTimeMillis() + "_" + remoteIp + "_" + remotePort)
            .set(ATTR_TRANS_KEY_REMOTE_IP, remoteIp).set(ATTR_TRANS_KEY_REMOTE_PORT, remotePort)
            .set(ATTR_TRANS_KEY_LOCAL_PORT, localPort).build();
        String connectionId = attrWrapper.get(ATTR_TRANS_KEY_CONN_ID);
        Loggers.REMOTE_DIGEST.info("Connection transportReady,connectionId = {} ", connectionId);
        return attrWrapper;
        
    }
    
    /** 传输终止：从连接管理器注销对应 connectionId。 */
    @Override
    public void transportTerminated(Attributes transportAttrs) {
        String connectionId = null;
        try {
            connectionId = transportAttrs.get(ATTR_TRANS_KEY_CONN_ID);
        } catch (Exception e) {
            // 忽略属性缺失异常
        }
        if (StringUtils.isNotBlank(connectionId)) {
            Loggers.REMOTE_DIGEST
                .info("Connection transportTerminated,connectionId = {} ", connectionId);
            connectionManager.unregister(connectionId);
        }
    }
}
