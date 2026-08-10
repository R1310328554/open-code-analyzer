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


import io.netty.util.concurrent.Future;

/**
 * {@link EventLoop} that allows to register / deregister {@link IoHandle} instances.
 * <p>支持注册/注销 {@link IoHandle} 的 {@link EventLoop}。</p>
 */
public interface IoEventLoop extends EventLoop, IoEventLoopGroup {

    /** 单线程 EventLoop 始终返回自身。 */
    @Override
    default IoEventLoop next() {
        return this;
    }

    /**
     * Register the {@link IoHandle} to the {@link EventLoop} for I/O processing.
     * <p>将 {@link IoHandle} 注册到本 {@link EventLoop} 以进行 I/O 处理。</p>
     *
     * @param handle        the {@link IoHandle} to register.
     * @return              the {@link Future} that is notified once the operations completes.
     */
    Future<IoRegistration> register(IoHandle handle);

    // Force sub-classes to implement.
    // 强制子类实现兼容性检查
    @Override
    boolean isCompatible(Class<? extends IoHandle> handleType);

    // Force sub-classes to implement.
    // 强制子类实现 I/O 处理器类型检查
    @Override
    boolean isIoType(Class<? extends IoHandler> handlerType);
}
