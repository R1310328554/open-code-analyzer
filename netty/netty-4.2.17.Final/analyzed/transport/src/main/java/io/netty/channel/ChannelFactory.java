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

/**
 * Creates a new {@link Channel}.
 * <p>用于创建 {@link Channel} 实例的工厂接口，与 {@link io.netty.bootstrap.ChannelFactory} 等价，
 * 供 {@link io.netty.bootstrap.ServerBootstrap}、{@link io.netty.bootstrap.Bootstrap} 等引导类配置通道类型。</p>
 */
@SuppressWarnings({ "ClassNameSameAsAncestorName", "deprecation" })
public interface ChannelFactory<T extends Channel> extends io.netty.bootstrap.ChannelFactory<T> {
    /**
     * Creates a new channel.
     * <p>创建并返回一个新的 {@link Channel} 实例；每次调用应产生独立对象（除非实现另有约定）。</p>
     */
    @Override
    T newChannel();
}
