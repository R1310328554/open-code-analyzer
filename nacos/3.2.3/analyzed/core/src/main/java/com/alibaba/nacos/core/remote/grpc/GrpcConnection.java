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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.grpc.auto.Payload;
import com.alibaba.nacos.api.remote.DefaultRequestFuture;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.RequestFuture;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.common.remote.client.grpc.GrpcUtils;
import com.alibaba.nacos.common.remote.exception.ConnectionAlreadyClosedException;
import com.alibaba.nacos.common.remote.exception.ConnectionBusyException;
import com.alibaba.nacos.core.remote.Connection;
import com.alibaba.nacos.core.remote.ConnectionMeta;
import com.alibaba.nacos.core.remote.RpcAckCallbackSynchronizer;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * gRPC 远程连接实现：通过双向流 StreamObserver 向客户端发送请求，
 * 支持同步/异步 RPC、推送队列背压检测与连接关闭。
 * grpc connection.
 *
 * @author liuzunfei
 * @version $Id: GrpcConnection.java, v 0.1 2020年07月13日 7:26 PM liuzunfei Exp $
 */
public class GrpcConnection extends Connection {
    
    /** 双向流响应观察者，用于向客户端推送 Payload。 */
    private StreamObserver streamObserver;
    
    /** 底层 Netty Channel。 */
    private Channel channel;
    
    /** TPS 控制器，用于记录推送队列阻塞。 */
    private static volatile TpsControlManager tpsControlManager;
    
    /** 构造 gRPC 连接。
     * @param metaInfo 连接元信息
     * @param streamObserver 双向流观察者
     * @param channel Netty 通道
     */
    public GrpcConnection(ConnectionMeta metaInfo, StreamObserver streamObserver, Channel channel) {
        super(metaInfo);
        this.streamObserver = streamObserver;
        this.channel = channel;
    }
    
    /**
     * 向客户端发送请求且不等待 ACK（单向推送）。
     * send request without ack.
     *
     * @param request request data.
     * @throws NacosException NacosException
     */
    public void sendRequestNoAck(Request request) throws NacosException {
        sendQueueBlockCheck();
        Future<Boolean> executeFuture = this.channel.eventLoop().submit(() -> {
            // StreamObserver#onNext 非线程安全，需同步避免直接内存泄漏
            synchronized (streamObserver) {
                try {
                    Payload payload = GrpcUtils.convert(request);
                    traceIfNecessary(payload);
                    streamObserver.onNext(payload);
                    return true;
                } catch (Throwable e) {
                    if (e instanceof StatusRuntimeException) {
                        throw new ConnectionAlreadyClosedException(e);
                    } else if (e instanceof IllegalStateException) {
                        throw new ConnectionAlreadyClosedException(e);
                    }
                    throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
                }
            }
        });
        try {
            executeFuture.get();
        } catch (Throwable throwable) {
            if (throwable instanceof ExecutionException && throwable.getCause() != null
                && throwable.getCause() instanceof NacosRuntimeException) {
                throw (NacosRuntimeException) throwable.getCause();
            }
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, throwable);
        }
    }
    
    private void sendQueueBlockCheck() {
        if (streamObserver instanceof ServerCallStreamObserver) {
            // 发送队列超过约 32KB 时 isReady 返回 false
            // 队列包括 gRPC 写队列与流控队列等
            // 32KB 阈值见 AbstractStream.TransportState.DEFAULT_ONREADY_THRESHOLD
            // see io.grpc.internal.AbstractStream.TransportState.DEFAULT_ONREADY_THRESHOLD
            boolean ready = ((ServerCallStreamObserver<?>) streamObserver).isReady();
            if (!ready) {
                if (tpsControlManager == null) {
                    synchronized (GrpcConnection.class) {
                        if (tpsControlManager == null) {
                            tpsControlManager =
                                ControlManagerCenter.getInstance().getTpsControlManager();
                            tpsControlManager.registerTpsPoint("SERVER_PUSH_BLOCK");
                        }
                    }
                }
                TpsCheckRequest tpsCheckRequest = new TpsCheckRequest("SERVER_PUSH_BLOCK",
                    this.getMetaInfo().getConnectionId(), this.getMetaInfo().getClientIp());
                // 仅记录阻塞 TPS 指标
                tpsControlManager.check(tpsCheckRequest);
                getMetaInfo().recordPushQueueBlockTimes();
                throw new ConnectionBusyException(
                    "too much bytes on sending queue of this stream.");
            } else {
                getMetaInfo().clearPushQueueBlockTimes();
            }
        }
    }
    
    private void traceIfNecessary(Payload payload) {
        String connectionId = null;
        if (this.isTraced()) {
            try {
                connectionId = getMetaInfo().getConnectionId();
                Loggers.REMOTE_DIGEST.info("[{}]Send request to client ,payload={}", connectionId,
                    payload.toByteString().toStringUtf8());
            } catch (Throwable throwable) {
                Loggers.REMOTE_DIGEST.warn("[{}]Send request to client trace error, ,error={}",
                    connectionId,
                    throwable);
            }
        }
    }
    
    private DefaultRequestFuture sendRequestInner(Request request, RequestCallBack callBack)
        throws NacosException {
        final String requestId = String.valueOf(PushAckIdGenerator.getNextId());
        request.setRequestId(requestId);
        
        DefaultRequestFuture defaultPushFuture =
            new DefaultRequestFuture(getMetaInfo().getConnectionId(), requestId,
                callBack, () -> RpcAckCallbackSynchronizer
                    .clearFuture(getMetaInfo().getConnectionId(), requestId));
        
        RpcAckCallbackSynchronizer.syncCallback(getMetaInfo().getConnectionId(), requestId,
            defaultPushFuture);
        try {
            sendRequestNoAck(request);
        } catch (NacosRuntimeException nacosRuntimeException) {
            defaultPushFuture.cancel(true);
            throw nacosRuntimeException;
        }
        return defaultPushFuture;
    }
    
    /** 同步发送请求并等待响应，超时抛出异常。 */
    @Override
    public Response request(Request request, long timeoutMills) throws NacosException {
        DefaultRequestFuture pushFuture = sendRequestInner(request, null);
        try {
            return pushFuture.get(timeoutMills);
        } catch (Exception e) {
            throw new NacosException(NacosException.SERVER_ERROR, e);
        } finally {
            RpcAckCallbackSynchronizer.clearFuture(getMetaInfo().getConnectionId(),
                pushFuture.getRequestId());
        }
    }
    
    /** 异步发送请求并返回 Future。 */
    @Override
    public RequestFuture requestFuture(Request request) throws NacosException {
        return sendRequestInner(request, null);
    }
    
    /** 异步发送请求并通过回调接收响应。 */
    @Override
    public void asyncRequest(Request request, RequestCallBack requestCallBack)
        throws NacosException {
        sendRequestInner(request, requestCallBack);
    }
    
    /** 关闭双向流与底层 Channel。 */
    @Override
    public void close() {
        String connectionId = null;
        
        try {
            connectionId = getMetaInfo().getConnectionId();
            
            if (isTraced()) {
                Loggers.REMOTE_DIGEST.warn("[{}] try to close connection ", connectionId);
            }
            
            try {
                closeBiStream();
            } catch (Throwable e) {
                Loggers.REMOTE_DIGEST.warn("[{}] connection  close bi stream exception  : {}",
                    connectionId, e);
            }
            channel.close();
            
        } catch (Exception e) {
            Loggers.REMOTE_DIGEST.warn("[{}] connection  close exception  : {}", connectionId, e);
        }
    }
    
    private void closeBiStream() {
        if (streamObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver serverCallStreamObserver =
                ((ServerCallStreamObserver) streamObserver);
            if (!serverCallStreamObserver.isCancelled()) {
                serverCallStreamObserver.onCompleted();
            }
        }
    }
    
    /** 判断 Netty Channel 是否仍处于活跃连接状态。 */
    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }
}
