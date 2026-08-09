/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.grpcweb.proxy;

import io.grpc.*;
import io.grpc.ClientCall.Listener;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;

import java.util.concurrent.CountDownLatch;

/**
 * gRPC 客户端拦截器：将后端 RPC 调用的响应头/尾帧桥接到 gRPC-Web HTTP 响应。
 *
 * <p>通过 {@link SendGrpcWebResponse} 把 {@link Metadata} 与 {@link Status} 写入 chunked HTTP；
 * 调用结束时对 {@link CountDownLatch} 计数，供 {@link GrpcWebRequestHandler} 同步等待。</p>
 */
class GrpcWebClientInterceptor implements ClientInterceptor {

    /** 用于通知主线程 RPC 流已结束（含 trailer 已写出） */
    private final CountDownLatch latch;
    /** 将 gRPC 语义映射为 gRPC-Web HTTP chunk 的写出器 */
    private final SendGrpcWebResponse sendResponse;

    GrpcWebClientInterceptor(CountDownLatch latch, SendGrpcWebResponse send) {
        this.latch = latch;
        sendResponse = send;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel channel) {
        return new SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // 用自定义 Listener 截获 headers/trailer，再转发给业务 StreamObserver
                super.start(new MetadataResponseListener<RespT>(responseListener), headers);
            }
        };
    }

    /**
     * 监听 gRPC 响应生命周期，在合适时机写出 gRPC-Web HTTP 头与 trailer。
     */
    class MetadataResponseListener<T> extends SimpleForwardingClientCallListener<T> {
        /** 是否已通过 onHeaders 写出 HTTP 响应头 */
        private boolean headersSent = false;

        MetadataResponseListener(Listener<T> responseListener) {
            super(responseListener);
        }

        @Override
        public void onHeaders(Metadata h) {
            sendResponse.writeHeaders(h);
            headersSent = true;
        }

        @Override
        public void onClose(Status s, Metadata t) {
            // 注意：onClose 可能在 onCompleted 之前触发，顺序与常规 StreamObserver 略有不同
            if (!headersSent) {
                // 部分错误路径下 onHeaders 未被调用；交由 ClientListener.onError 处理，此处可忽略
                // TODO: 若 onError 也未触发，trailer 可能丢失，需进一步确认
            } else {
                sendResponse.writeTrailer(s, t);
                latch.countDown();
            }
            super.onClose(s, t);
        }
    }
}
