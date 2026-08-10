/*
 * Copyright 2014 The Netty Project
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
package io.netty.channel.unix;

import io.netty.util.internal.ClassInitializerUtil;
import io.netty.util.internal.UnstableApi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tells if <a href="https://netty.io/wiki/native-transports.html">{@code netty-transport-native-unix}</a> is
 * supported.
 * <p>Unix 原生传输公共入口：预加载 JNI OnLoad 所需类以避免类加载器死锁； {@link #registerInternal} 由 epoll/kqueue 模块调用以完成注册。</p>
 */
public final class Unix {
    /** 是否已完成原生库注册（内部使用） */
    private static final AtomicBoolean registered = new AtomicBoolean();

    static {
        // 预加载 JNI OnLoad 将触达的类，避免类加载器死锁（netty#11209）

        // 须与 NETTY_JNI_UTIL_LOAD_CLASS / FIND_CLASS 加载的类一致
        ClassInitializerUtil.tryLoadClasses(Unix.class,
                // netty_unix_errors
                OutOfMemoryError.class, RuntimeException.class, ClosedChannelException.class,
                IOException.class, PortUnreachableException.class,

                // netty_unix_socket
                DatagramSocketAddress.class, DomainDatagramSocketAddress.class, InetSocketAddress.class
        );
    }

    /**
     * Internal method... Should never be called from the user.
     *
     * @param registerTask
     * <p>执行原生库注册任务并初始化 {@link Socket} IPv6 偏好。</p>
     */
    @UnstableApi
    public static synchronized void registerInternal(Runnable registerTask) {
        registerTask.run();
        Socket.initialize();
    }

    /**
     * Returns {@code true} if and only if the <a href="https://netty.io/wiki/native-transports.html">{@code
     * netty_transport_native_unix}</a> is available.
     * <p>已废弃：可用性由各 transport 模块自行检测。</p>
     */
    @Deprecated
    public static boolean isAvailable() {
        return false;
    }

    /**
     * Ensure that <a href="https://netty.io/wiki/native-transports.html">{@code netty_transport_native_unix}</a> is
     * available.
     *
     * @throws UnsatisfiedLinkError if unavailable
     * <p>已废弃：请使用具体 transport 的 ensureAvailability。</p>
     */
    @Deprecated
    public static void ensureAvailability() {
       throw new UnsupportedOperationException();
    }

    /**
     * Returns the cause of unavailability of <a href="https://netty.io/wiki/native-transports.html">
     * {@code netty_transport_native_unix}</a>.
     *
     * @return the cause if unavailable. {@code null} if available.
     * <p>已废弃：不可用原因由各 transport 模块提供。</p>
     */
    @Deprecated
    public static Throwable unavailabilityCause() {
        return new UnsupportedOperationException();
    }

    private Unix() {
    }
}
