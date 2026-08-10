/*
 * Copyright 2024 The Netty Project
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

import io.netty.util.concurrent.ThreadAwareExecutor;
/**
 * A handle that can be registered to an {@link IoHandler}.
 * All methods must be called from the {@link ThreadAwareExecutor} thread (which means
 * {@link ThreadAwareExecutor#isExecutorThread(Thread)} must return {@code true}).
 *<p>
 * All the methods are expected to be called from the {@link IoHandler} on which this {@link IoHandle}
 * was registered via {@link IoHandler#register(IoHandle)}.
 * <p>可注册到 {@link IoHandler} 的 I/O 句柄。所有方法须在 {@link ThreadAwareExecutor} 线程上调用
 * （{@link ThreadAwareExecutor#isExecutorThread(Thread)} 为 {@code true}），
 * 且通常由注册本句柄的 {@link IoHandler} 回调。</p>
 */
public interface IoHandle extends AutoCloseable {

    /**
     * Be called once there is something to handle.
     * <p>有待处理的 I/O 事件时回调；{@link IoEvent} 仅在方法执行期间有效，不得逃逸。</p>
     *
     * @param registration  the {@link IoRegistration} for this {@link IoHandle}.
     * @param ioEvent       the {@link IoEvent} that must be handled. The {@link IoEvent} is only valid
     *                      while this method is executed and so must not escape it.
     */
    void handle(IoRegistration registration, IoEvent ioEvent);

    /**
     * Called once this {@link IoHandle} was registered and so will start to receive events
     * via {@link #handle(IoRegistration, IoEvent)}.
     * <p>注册完成后调用，此后可通过 {@link #handle(IoRegistration, IoEvent)} 接收事件。</p>
     */
    default void registered() {
        // Noop by default.
    }

    /**
     * Called once this {@link IoHandle} was unregistered and so will not receive any more events
     * via {@link #handle(IoRegistration, IoEvent)}.
     * <p>注销完成后调用，此后不再接收 {@link IoEvent}。</p>
     */
    default void unregistered() {
        // Noop by default.
    }

    /**
     * Called once the {@link IoHandle} should be closed. Even once this method is called this handle might
     * still receive events via {@link #handle(IoRegistration, IoEvent)} (if it was previous be registered and so its
     * {@link #registered()} method was called) until the {@link #unregistered()} method is called.
     * <p>请求关闭句柄时调用。在 {@link #unregistered()} 之前，若此前已注册，仍可能收到
     * {@link #handle(IoRegistration, IoEvent)} 事件。</p>
     */
    void close() throws Exception;
}
