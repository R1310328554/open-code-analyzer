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
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Sentinel 集成的 gRPC 服务端拦截器，目前仅支持 unary 方法。</p>
 * <p>
 * Example code:
 * <pre>
 * Server server = ServerBuilder.forPort(port)
 *      .addService(new MyServiceImpl()) // Add your service.
 *      .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.
 *      .build();
 * </pre>
 * <p>
 * 客户端拦截器见 {@link SentinelGrpcClientInterceptor}。
 *
 * @author Eric Zhao
 */
public class SentinelGrpcServerInterceptor implements ServerInterceptor {
    private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE.withDescription(
            "Flow control limit exceeded (server side)");
    private static final StatusRuntimeException STATUS_RUNTIME_EXCEPTION = new StatusRuntimeException(Status.CANCELLED);

    /** 以 gRPC 全方法名作为资源名进行 asyncEntry，在 close/onCancel 时 exit。 */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        // 远程地址：serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        Entry entry = null;
        try {
            entry = SphU.asyncEntry(fullMethodName, EntryType.IN);
            final AtomicReference<Entry> atomicReferenceEntry = new AtomicReference<>(entry);
            // 通过流控检查，转发调用。
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(
                            new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                                @Override
                                public void close(Status status, Metadata trailers) {
                                    Entry entry = atomicReferenceEntry.get();
                                    if (entry != null) {
                                        // 记录异常指标。
                                        if (!status.isOk()) {
                                            Tracer.traceEntry(status.asRuntimeException(), entry);
                                        }
                                        // 调用关闭时 exit entry
                                        entry.exit();
                                    }
                                    super.close(status, trailers);
                                }
                            }, headers)) {
                /**
                 * 调用被取消时会触发 onCancel 而非 close，服务端应在此中止处理以节省资源。
                 * @see ServerCall.Listener#onCancel()
                 */
                @Override
                public void onCancel() {
                    Entry entry = atomicReferenceEntry.get();
                    if (entry != null) {
                        Tracer.traceEntry(STATUS_RUNTIME_EXCEPTION, entry);
                        entry.exit();
                        atomicReferenceEntry.set(null);
                    }
                    super.onCancel();
                }
            };
        } catch (BlockException e) {
            call.close(FLOW_CONTROL_BLOCK, new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        } catch (RuntimeException e) {
            // 捕获 startCall 抛出的 RuntimeException，确保 entry 退出。
            if (entry != null) {
                Tracer.traceEntry(e, entry);
                entry.exit();
            }
            throw e;
        }
    }
}
