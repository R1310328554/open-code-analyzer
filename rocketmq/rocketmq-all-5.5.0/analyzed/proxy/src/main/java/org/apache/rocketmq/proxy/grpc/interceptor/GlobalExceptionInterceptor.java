/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.grpc.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 全局异常拦截器：捕获 gRPC 调用链中的未处理异常并转换为标准 Status 响应。
 */
public class GlobalExceptionInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    /** 包装 Listener 并在各生命周期回调中统一捕获异常。 */
    @Override
    public <R, W> ServerCall.Listener<R> interceptCall(
        ServerCall<R, W> call,
        Metadata headers,
        ServerCallHandler<R, W> next
    ) {
        // 使用可幂等关闭的 ServerCall 防止重复 close
        final ServerCall<R, W> serverCall = new ClosableServerCall<>(call);
        ServerCall.Listener<R> delegate = next.startCall(serverCall, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<R>(delegate) {
            @Override
            public void onMessage(R message) {
                try {
                    super.onMessage(message);
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onReady() {
                try {
                    super.onReady();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            /** 将异常映射为 gRPC Status 并关闭调用。 */
            private void closeWithException(Throwable t) {
                Metadata trailers = new Metadata();
                Status status = Status.INTERNAL.withDescription(t.getMessage());
                boolean printLog = true;

                if (t instanceof StatusRuntimeException) {
                    trailers = ((StatusRuntimeException) t).getTrailers();
                    status = ((StatusRuntimeException) t).getStatus();
                    // 权限拒绝时不打印完整异常栈
                    if (status.getCode().value() == Status.PERMISSION_DENIED.getCode().value()) {
                        printLog = false;
                    }
                }

                if (printLog) {
                    log.error("grpc server has exception. errorMsg:{}, e:", t.getMessage(), t);
                }

                serverCall.close(status, trailers);
            }
        };
    }

    /** 保证 close 仅执行一次的 ServerCall 包装器。 */
    private static class ClosableServerCall<R, W> extends
        ForwardingServerCall.SimpleForwardingServerCall<R, W> {
        /** 标记是否已调用 close。 */
        private boolean closeCalled = false;

        ClosableServerCall(ServerCall<R, W> delegate) {
            super(delegate);
        }

        @Override
        public synchronized void close(final Status status, final Metadata trailers) {
            if (!closeCalled) {
                closeCalled = true;
                ClosableServerCall.super.close(status, trailers);
            }
        }
    }
}
