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
package com.alibaba.csp.sentinel.adapter.grpc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Sentinel 集成的 gRPC 客户端拦截器，目前仅支持 unary 方法。</p>
 * <p>
 * Example code:
 * <pre>
 * public class ServiceClient {
 *
 *     private final ManagedChannel channel;
 *
 *     ServiceClient(String host, int port) {
 *         this.channel = ManagedChannelBuilder.forAddress(host, port)
 *             .intercept(new SentinelGrpcClientInterceptor()) // Add the client interceptor.
 *             .build();
 *         // Init your stub here.
 *     }
 *
 * }
 * </pre>
 * <p>
 * 服务端拦截器见 {@link SentinelGrpcServerInterceptor}。
 *
 * @author Eric Zhao
 */
public class SentinelGrpcClientInterceptor implements ClientInterceptor {
    private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE.withDescription(
            "Flow control limit exceeded (client side)");

    /** 以 gRPC 全方法名作为资源名进行 asyncEntry，在 onClose/cancel 时 exit。 */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {
        String fullMethodName = methodDescriptor.getFullMethodName();
        Entry entry = null;
        try {
            entry = SphU.asyncEntry(fullMethodName, EntryType.OUT);
            final AtomicReference<Entry> atomicReferenceEntry = new AtomicReference<>(entry);
            // 通过流控检查，转发调用。
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                    channel.newCall(methodDescriptor, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onClose(Status status, Metadata trailers) {
                            Entry entry = atomicReferenceEntry.get();
                            if (entry != null) {
                                // 记录异常指标。
                                if (!status.isOk()) {
                                    Tracer.traceEntry(status.asRuntimeException(), entry);
                                }
                                entry.exit();
                                atomicReferenceEntry.set(null);
                            }
                            super.onClose(status, trailers);
                        }
                    }, headers);
                }

                /**
                 * 部分异常只会触发 cancel 而不会调用 onClose。
                 */
                @Override
                public void cancel(@Nullable String message, @Nullable Throwable cause) {
                    Entry entry = atomicReferenceEntry.get();
                    // 部分异常会同时触发 onClose 与 cancel。
                    if (entry != null) {
                        // 记录异常指标。
                        Tracer.traceEntry(cause, entry);
                        entry.exit();
                        atomicReferenceEntry.set(null);
                    }
                    super.cancel(message, cause);
                }
            };
        } catch (BlockException e) {
            // 超过流控阈值，阻断调用。
            return new ClientCall<ReqT, RespT>() {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    responseListener.onClose(FLOW_CONTROL_BLOCK, new Metadata());
                }

                @Override
                public void request(int numMessages) {
                }

                @Override
                public void cancel(@Nullable String message, @Nullable Throwable cause) {
                }

                @Override
                public void halfClose() {
                }

                @Override
                public void sendMessage(ReqT message) {
                }
            };
        } catch (RuntimeException e) {
            // 捕获 newCall 抛出的 RuntimeException，确保 entry 退出。
            if (entry != null) {
                Tracer.traceEntry(e, entry);
                entry.exit();
            }
            throw e;
        }
    }
}
