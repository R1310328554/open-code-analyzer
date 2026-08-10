/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel.kqueue;

import io.netty.channel.ChannelOption;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * If KQueue is available the JNI resources will be loaded when this class loads.
 * <p>KQueue 原生传输可用性探测：类加载时尝试创建 kqueue fd 并加载 JNI； 可通过 {@code -Dio.netty.transport.noNative=true} 禁用。</p>
 */
public final class KQueue {
    /** 不可用时的根因；{@code null} 表示 KQueue 可用 */
    private static final Throwable UNAVAILABILITY_CAUSE;

    static {
        Throwable cause = null;
        if (SystemPropertyUtil.getBoolean("io.netty.transport.noNative", false)) {
            cause = new UnsupportedOperationException(
                    "Native transport was explicit disabled with -Dio.netty.transport.noNative=true");
        } else {
            FileDescriptor kqueueFd = null;
            try {
                kqueueFd = Native.newKQueue();
            } catch (Throwable t) {
                cause = t;
            } finally {
                if (kqueueFd != null) {
                    try {
                        kqueueFd.close();
                    } catch (Exception ignore) {
                        // ignore
                    }
                }
            }
        }
        if (cause != null) {
            InternalLogger logger = InternalLoggerFactory.getInstance(KQueue.class);
            if (logger.isTraceEnabled()) {
                logger.debug("KQueue support is not available", cause);
            } else if (logger.isDebugEnabled()) {
                logger.debug("KQueue support is not available: {}", cause.getMessage());
            }
        }
        UNAVAILABILITY_CAUSE = cause;
    }

    /**
     * Returns {@code true} if and only if the <a href="https://netty.io/wiki/native-transports.html">{@code
     * netty-transport-native-kqueue}</a> is available.
     * <p>原生 kqueue 模块与 JNI 是否已成功加载。</p>
     */
    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    /**
     * Ensure that <a href="https://netty.io/wiki/native-transports.html">{@code netty-transport-native-kqueue}</a> is
     * available.
     * <p>强制校验可用性，不可用时抛出 {@link UnsatisfiedLinkError}。</p>
     *
     * @throws UnsatisfiedLinkError if unavailable
     */
    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error) new UnsatisfiedLinkError(
                    "failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    /**
     * Returns the cause of unavailability of <a href="https://netty.io/wiki/native-transports.html">{@code
     * netty-transport-native-kqueue}</a>.
     * <p>返回不可用原因；可用时为 {@code null}。</p>
     *
     * @return the cause if unavailable. {@code null} if available.
     */
    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    /**
     * Returns {@code true} if the kqueue native transport is both {@linkplain #isAvailable() available} and supports
     * {@linkplain ChannelOption#TCP_FASTOPEN_CONNECT client-side TCP FastOpen}.
     * <p>客户端 TCP FastOpen（connectx）是否可用。</p>
     *
     * @return {@code true} if it's possible to use client-side TCP FastOpen via kqueue, otherwise {@code false}.
     */
    public static boolean isTcpFastOpenClientSideAvailable() {
        return isAvailable() && Native.IS_SUPPORTING_TCP_FASTOPEN_CLIENT;
    }

    /**
     * Returns {@code true} if the kqueue native transport is both {@linkplain #isAvailable() available} and supports
     * {@linkplain ChannelOption#TCP_FASTOPEN server-side TCP FastOpen}.
     * <p>服务端 TCP FastOpen 是否可用。</p>
     *
     * @return {@code true} if it's possible to use server-side TCP FastOpen via kqueue, otherwise {@code false}.
     */
    public static boolean isTcpFastOpenServerSideAvailable() {
        return isAvailable() && Native.IS_SUPPORTING_TCP_FASTOPEN_SERVER;
    }

    private KQueue() {
    }
}
