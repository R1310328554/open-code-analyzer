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

import io.netty.util.concurrent.EventExecutor;

/**
 * {@link DefaultChannelPipeline} 使用的 {@link ChannelHandlerContext} 实现，
 * 持有对 {@link ChannelHandler} 的直接引用并在 {@link #handler()} 中返回。
 */
final class DefaultChannelHandlerContext extends AbstractChannelHandlerContext {

    /** 本 Context 绑定的处理器实例 */
    private final ChannelHandler handler;

    /** 创建 Pipeline 中的 handler 上下文节点。 */
    DefaultChannelHandlerContext(
            DefaultChannelPipeline pipeline, EventExecutor executor, String name, ChannelHandler handler) {
        super(pipeline, executor, name, handler.getClass());
        this.handler = handler;
    }

    /** 返回绑定的 {@link ChannelHandler}。 */
    @Override
    public ChannelHandler handler() {
        return handler;
    }
}
