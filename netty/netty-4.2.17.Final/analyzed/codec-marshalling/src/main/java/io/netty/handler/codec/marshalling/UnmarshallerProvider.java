/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.marshalling;

import io.netty.channel.ChannelHandlerContext;

import org.jboss.marshalling.Unmarshaller;

/**
 * This provider is responsible to get an {@link Unmarshaller} for a {@link ChannelHandlerContext}
 *
 * <p>JBoss Marshalling 反序列化器的供应策略：按 {@link ChannelHandlerContext} 决定如何创建或复用
 * {@link Unmarshaller}，便于 per-channel 配置 ClassLoader、流工厂等上下文相关参数。
 */
public interface UnmarshallerProvider {

    /**
     * Get the {@link Unmarshaller} for the given {@link ChannelHandlerContext}
     *
     * <p>解码器在每次 inbound 字节到达时调用；实现可返回新实例或从池中借出，须保证与 ctx 生命周期一致。
     */
    Unmarshaller getUnmarshaller(ChannelHandlerContext ctx) throws Exception;
}
