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

package com.alibaba.nacos.istio.common;

import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * gRPC 长连接的抽象基类，维护连接标识、订阅资源状态及向客户端推送消息的生命周期。
 *
 * <p>子类实现 {@link #push} 将 Istio 资源响应写入 {@link StreamObserver}。</p>
 *
 * @author special.fy
 */
public abstract class AbstractConnection<MessageT> {
    
    /** 全局连接序号生成器，用于构造唯一 connectionId。 */
    private static AtomicLong connectIdGenerator = new AtomicLong(0);
    
    /** 客户端 ID 与序号组合而成的连接标识。 */
    private String connectionId;
    
    /** 向客户端写入响应的 gRPC 流观察者。 */
    protected StreamObserver<MessageT> streamObserver;
    
    /** 按资源类型索引的订阅/ACK 状态表。 */
    private final Map<String, WatchedStatus> watchedResources;
    
    public AbstractConnection(StreamObserver<MessageT> streamObserver) {
        this.streamObserver = streamObserver;
        this.watchedResources = new HashMap<>(1 << 4);
    }
    
    /** 基于客户端 ID 与自增序号生成并设置 connectionId。 */
    public void setConnectionId(String clientId) {
        long id = connectIdGenerator.getAndIncrement();
        this.connectionId = clientId + "-" + id;
    }
    
    public String getConnectionId() {
        return connectionId;
    }
    
    /** 注册某资源类型的订阅状态。 */
    public void addWatchedResource(String resourceType, WatchedStatus watchedStatus) {
        watchedResources.put(resourceType, watchedStatus);
    }
    
    /** 按资源类型查询订阅状态。 */
    public WatchedStatus getWatchedStatusByType(String resourceType) {
        return watchedResources.get(resourceType);
    }
    
    /**
     * 向 gRPC 连接推送资源响应并更新订阅状态。
     *
     * @param message 待推送的响应消息
     * @param watchedStatus 该资源类型的订阅/ACK 状态
     */
    public abstract void push(MessageT message, WatchedStatus watchedStatus);
}
