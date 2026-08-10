/*
 * Copyright 2023 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ssl;

import io.netty.util.internal.ThrowableUtil;

import javax.net.ssl.SSLHandshakeException;

/**
 * 不填充完整堆栈的 {@link SSLHandshakeException}。
 * <p>高频握手失败路径上省略 {@link #fillInStackTrace()} 以降低分配与栈遍历开销；
 * 仍可通过 {@link #newInstance} 记录抛出点的类与方法。</p>
 */
final class StacklessSSLHandshakeException extends SSLHandshakeException {

    private static final long serialVersionUID = -1244781947804415549L;

    private StacklessSSLHandshakeException(String reason) {
        super(reason);
    }

    @Override
    public Throwable fillInStackTrace() {
        // This is a performance optimization to not fill in the
        // stack trace as this is a stackless exception.
        // 性能优化：无栈异常不采集完整堆栈
        return this;
    }

    /**
     * Creates a new {@link StacklessSSLHandshakeException} which has the origin of the given {@link Class} and method.
     */
    static StacklessSSLHandshakeException newInstance(String reason, Class<?> clazz, String method) {
        return ThrowableUtil.unknownStackTrace(new StacklessSSLHandshakeException(reason), clazz, method);
    }
}
