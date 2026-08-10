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

import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * 仅处理指定类型入站消息的 {@link ChannelInboundHandlerAdapter} 便捷基类。
 *
 * <p>示例：只处理 {@link String} 消息：</p>
 *
 * <pre>
 *     public class StringHandler extends
 *             {@link SimpleChannelInboundHandler}&lt;{@link String}&gt; {
 *
 *         {@code @Override}
 *         protected void channelRead0({@link ChannelHandlerContext} ctx, {@link String} message)
 *                 throws {@link Exception} {
 *             System.out.println(message);
 *         }
 *     }
 * </pre>
 *
 * <p>
 * 根据构造参数，已处理的消息可能通过 {@link ReferenceCountUtil#release(Object)} 自动释放。
 * 若需将消息传给 Pipeline 中下一 Handler，可能需要 {@link ReferenceCountUtil#retain(Object)}。
 * </p>
 */
public abstract class SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter {

    /** 入站消息类型匹配器 */
    private final TypeParameterMatcher matcher;
    /** 是否在处理后自动 release 消息 */
    private final boolean autoRelease;

    /**
     * 等价于 {@link #SimpleChannelInboundHandler(boolean)}，{@code autoRelease} 为 {@code true}。
     */
    protected SimpleChannelInboundHandler() {
        this(true);
    }

    /**
     * 从子类泛型参数自动推断待匹配的消息类型。
     *
     * @param autoRelease   为 {@code true} 时，已处理消息会通过
     *                      {@link ReferenceCountUtil#release(Object)} 自动释放
     */
    protected SimpleChannelInboundHandler(boolean autoRelease) {
        matcher = TypeParameterMatcher.find(this, SimpleChannelInboundHandler.class, "I");
        this.autoRelease = autoRelease;
    }

    /**
     * 等价于 {@link #SimpleChannelInboundHandler(Class, boolean)}，{@code autoRelease} 为 {@code true}。
     */
    protected SimpleChannelInboundHandler(Class<? extends I> inboundMessageType) {
        this(inboundMessageType, true);
    }

    /**
     * 显式指定要匹配的入站消息类型。
     *
     * @param inboundMessageType    要处理的消息类型
     * @param autoRelease           为 {@code true} 时自动释放已处理消息
     */
    protected SimpleChannelInboundHandler(Class<? extends I> inboundMessageType, boolean autoRelease) {
        matcher = TypeParameterMatcher.get(inboundMessageType);
        this.autoRelease = autoRelease;
    }

    /**
     * 判断给定消息是否应由本 Handler 处理。
     * <p>
     * 返回 {@code false} 时消息会传递给 {@link ChannelPipeline} 中的下一个
     * {@link ChannelInboundHandler}。
     * </p>
     */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I imsg = (I) msg;
                channelRead0(ctx, imsg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (autoRelease && release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    /**
     * 对类型为 {@link I} 的每条入站消息调用。
     *
     * @param ctx           本 Handler 所属的 {@link ChannelHandlerContext}
     * @param msg           待处理消息
     * @throws Exception    处理出错时抛出
     */
    protected abstract void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception;
}
