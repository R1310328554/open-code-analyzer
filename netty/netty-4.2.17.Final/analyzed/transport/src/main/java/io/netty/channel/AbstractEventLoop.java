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

package io.netty.channel;

import io.netty.util.concurrent.AbstractEventExecutor;

/**
 * Skeletal implementation of {@link EventLoop}.
 * <p>{@link EventLoop} 的骨架实现，继承 {@link io.netty.util.concurrent.AbstractEventExecutor}，
 * 将父执行器与 {@link #next()} 的返回类型收窄为通道相关的 {@link EventLoopGroup} 与 {@link EventLoop}。</p>
 */
public abstract class AbstractEventLoop extends AbstractEventExecutor implements EventLoop {

    /** 无参构造，供子类在无显式父组时使用。 */
    protected AbstractEventLoop() { }

    /**
     * 指定所属 {@link EventLoopGroup} 的构造器。
     *
     * @param parent 创建本 {@link EventLoop} 的父 {@link EventLoopGroup}，可为 {@code null}
     */
    protected AbstractEventLoop(EventLoopGroup parent) {
        super(parent);
    }

    /**
     * 返回创建本 {@link EventLoop} 的父 {@link EventLoopGroup}。
     */
    @Override
    public EventLoopGroup parent() {
        return (EventLoopGroup) super.parent();
    }

    /**
     * 返回下一个 {@link EventLoop}；在单线程 {@link EventLoop} 实现中通常返回自身。
     */
    @Override
    public EventLoop next() {
        return (EventLoop) super.next();
    }
}
