/*
 * Copyright 2013 The Netty Project
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

import io.netty.util.concurrent.AbstractEventExecutorGroup;

/**
 * Skeletal implementation of {@link EventLoopGroup}.
 * <p>{@link EventLoopGroup} 的骨架实现，继承 {@link io.netty.util.concurrent.AbstractEventExecutorGroup}，
 * 子类只需实现 {@link #next()} 以按组内策略选取 {@link EventLoop}。</p>
 */
public abstract class AbstractEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup {
    /**
     * 按组内负载均衡策略返回下一个 {@link EventLoop}，供注册 {@link Channel} 时使用。
     */
    @Override
    public abstract EventLoop next();
}
