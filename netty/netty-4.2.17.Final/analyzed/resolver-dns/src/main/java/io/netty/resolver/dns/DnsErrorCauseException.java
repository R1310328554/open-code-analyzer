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

package io.netty.resolver.dns;

import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.util.internal.ThrowableUtil;

import java.net.UnknownHostException;

/**
 * 携带 {@link DnsResponseCode} 元数据的异常，作为 {@link UnknownHostException} 的 cause 传播 DNS 错误码。
 * <p>不填充堆栈，避免泄漏 ClassLoader；供上层识别 SERVFAIL、REFUSED 等响应码。</p>
 */
public final class DnsErrorCauseException extends RuntimeException {

    private static final long serialVersionUID = 7485145036717494533L;

    /** 导致解析失败的 DNS 响应码。 */
    private final DnsResponseCode code;

    private DnsErrorCauseException(String message, DnsResponseCode code, boolean shared) {
        super(message, null, false, true);
        this.code = code;
        assert shared;
    }

    // 不填充堆栈，避免 native 回溯与 ClassLoader 泄漏
    // Override fillInStackTrace() so we not populate the backtrace via a native call and so leak the
    // Classloader.
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    /**
     * 返回导致 {@link UnknownHostException} 的 DNS 错误码。
     *
     * @return the DNS error-code that caused the {@link UnknownHostException}.
     */
    public DnsResponseCode getCode() {
        return code;
    }

    static DnsErrorCauseException newStatic(String message, DnsResponseCode code, Class<?> clazz, String method) {
        final DnsErrorCauseException exception = new DnsErrorCauseException(message, code, true);
        return ThrowableUtil.unknownStackTrace(exception, clazz, method);
    }
}
