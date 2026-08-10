/*
 * Copyright 2020 The Netty Project
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
package io.netty.channel;

import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.UnstableApi;

import java.nio.channels.ClosedChannelException;

/**
 * Cheap {@link ClosedChannelException} that does not fill in the stacktrace.
 * <p>不填充堆栈跟踪的轻量 {@link ClosedChannelException}，用于高频关闭路径以降低开销。</p>
 */
@UnstableApi
public final class StacklessClosedChannelException extends ClosedChannelException {

    private static final long serialVersionUID = -2214806025529435136L;

    /** 私有构造，通过 {@link #newInstance(Class, String)} 创建 */
    private StacklessClosedChannelException() { }

    /** 跳过堆栈填充，直接返回自身以节省分配与 walk 成本 */
    @Override
    public Throwable fillInStackTrace() {
        // Suppress a warning since this method doesn't need synchronization
        return this;
    }

    /**
     * Creates a new {@link StacklessClosedChannelException} which has the origin of the given {@link Class} and method.
     * <p>创建无堆栈实例，并通过 {@link ThrowableUtil} 将异常来源绑定到指定类与方法。</p>
     *
     * @param clazz  异常来源类
     * @param method 异常来源方法名
     */
    public static StacklessClosedChannelException newInstance(Class<?> clazz, String method) {
        return ThrowableUtil.unknownStackTrace(new StacklessClosedChannelException(), clazz, method);
    }
}
