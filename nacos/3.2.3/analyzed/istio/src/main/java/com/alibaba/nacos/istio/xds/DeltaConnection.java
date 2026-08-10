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
 *
 */

package com.alibaba.nacos.istio.xds;

import com.alibaba.nacos.istio.common.AbstractConnection;
import com.alibaba.nacos.istio.common.WatchedStatus;
import com.alibaba.nacos.istio.misc.Loggers;
import io.envoyproxy.envoy.service.discovery.v3.DeltaDiscoveryResponse;
import io.grpc.stub.StreamObserver;

/**
 * xDS Delta 发现协议的 gRPC 连接实现。
 *
 * <p>向 Envoy 推送 {@link DeltaDiscoveryResponse} 并更新 {@link WatchedStatus} 版本/nonce。</p>
 *
 * @author RocketEngine26
 * @date 2022/8/20 下午10:46
 */
public class DeltaConnection extends AbstractConnection<DeltaDiscoveryResponse> {
    
    /**
     * @param streamObserver 客户端 Delta 响应流
     */
    public DeltaConnection(StreamObserver<DeltaDiscoveryResponse> streamObserver) {
        super(streamObserver);
    }
    
    /**
     * 推送 Delta 响应并同步 WatchedStatus 中的 version/nonce。
     *
     * @param response       Delta 发现响应
     * @param watchedStatus  对应资源类型的订阅状态
     */
    @Override
    public void push(DeltaDiscoveryResponse response, WatchedStatus watchedStatus) {
        if (Loggers.MAIN.isDebugEnabled()) {
            Loggers.MAIN.debug("DeltaDiscoveryResponse: {}", response.toString());
        }
        
        Loggers.MAIN.info("DeltaDiscoveryResponse: {}", response.toString());
        
        this.streamObserver.onNext(response);
        
        // 更新订阅状态的最新 version 与 nonce，供 ACK 比对
        watchedStatus.setLatestVersion(response.getSystemVersionInfo());
        watchedStatus.setLatestNonce(response.getNonce());
        
        Loggers.MAIN.info(
            "delta: push, type: {}, connection-id {}, version {}, nonce {}, resource size {}.",
            watchedStatus.getType(),
            getConnectionId(),
            response.getSystemVersionInfo(),
            response.getNonce(),
            response.getResourcesCount());
    }
}
