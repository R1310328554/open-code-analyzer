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
package org.apache.rocketmq.proxy.grpc.v2.common;

import apache.rocketmq.v2.Code;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.proxy.common.ProxyException;
import org.apache.rocketmq.proxy.common.ProxyExceptionCode;

/**
 * gRPC Proxy 运行时异常：携带 gRPC {@link Code} 或将 {@link ProxyException} 映射为 gRPC 错误码。
 */
public class GrpcProxyException extends RuntimeException {

    /** 底层 Proxy 业务异常（可选）。 */
    private ProxyException proxyException;
    /** 直接指定的 gRPC 错误码（可选）。 */
    private Code code;

    /** {@link ProxyExceptionCode} 到 gRPC {@link Code} 的映射表。 */
    protected static final Map<ProxyExceptionCode, Code> CODE_MAPPING = new ConcurrentHashMap<>();

    static {
        CODE_MAPPING.put(ProxyExceptionCode.INVALID_BROKER_NAME, Code.BAD_REQUEST);
        CODE_MAPPING.put(ProxyExceptionCode.INVALID_RECEIPT_HANDLE, Code.INVALID_RECEIPT_HANDLE);
        CODE_MAPPING.put(ProxyExceptionCode.FORBIDDEN, Code.FORBIDDEN);
        CODE_MAPPING.put(ProxyExceptionCode.INTERNAL_SERVER_ERROR, Code.INTERNAL_SERVER_ERROR);
        CODE_MAPPING.put(ProxyExceptionCode.MESSAGE_PROPERTY_CONFLICT_WITH_TYPE, Code.MESSAGE_PROPERTY_CONFLICT_WITH_TYPE);
    }

    /** 以 gRPC 错误码与消息构造异常。 */
    public GrpcProxyException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public GrpcProxyException(Code code, String message, Throwable t) {
        super(message, t);
        this.code = code;
    }

    /** 由 {@link ProxyException} 包装构造。 */
    public GrpcProxyException(ProxyException proxyException) {
        super(proxyException);
        this.proxyException = proxyException;
    }

    /** 解析并返回 gRPC 错误码（优先直接 code，其次映射 ProxyException）。 */
    public Code getCode() {
        if (this.code != null) {
            return this.code;
        }
        if (this.proxyException != null) {
            return CODE_MAPPING.getOrDefault(this.proxyException.getCode(), Code.INTERNAL_SERVER_ERROR);
        }
        return Code.INTERNAL_SERVER_ERROR;
    }

    public ProxyException getProxyException() {
        return proxyException;
    }
}
