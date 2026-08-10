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
 * Factory for {@link IoHandler} instances.
 * <p>{@link IoHandler} 实例的工厂接口。</p>
 */
public interface IoHandlerFactory {

    /**
     * Creates a new {@link IoHandler} instance.
     * <p>为指定 {@link ThreadAwareExecutor} 创建新的 {@link IoHandler}。</p>
     *
     * @param ioExecutor        the {@link ThreadAwareExecutor} for the {@link IoHandler}.
     * @return                  a new {@link IoHandler} instance.
     */
    IoHandler newHandler(ThreadAwareExecutor ioExecutor);

    /**
     * Returns {@code true} if it's supported that the {@link ThreadAwareExecutor} might change its {@link Thread}
     * during the life-time of the {@link IoHandler} that can be created via {@link #newHandler(ThreadAwareExecutor)}.
     * That said even if changing the {@link Thread} is supported it must be guaranteed that the access rules specified
     * by {@link IoHandler} are not violated.
     * <p>若 {@link IoHandler} 生命周期内 {@link ThreadAwareExecutor} 可更换执行线程，则返回 {@code true}；
     * 即使支持换线程，仍须遵守 {@link IoHandler} 规定的线程访问约束。</p>
     *
     * @return {@code true} if changing is supported, {@code false} otherwise.
     */
    default boolean isChangingThreadSupported() {
        return false;
    }
}
