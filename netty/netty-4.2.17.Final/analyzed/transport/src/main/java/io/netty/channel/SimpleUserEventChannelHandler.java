/*
 * Copyright 2018 The Netty Project
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
 * 仅处理指定类型用户事件的 {@link ChannelInboundHandlerAdapter} 便捷基类。
 *
 * <p>示例：只处理 {@link String} 用户事件：</p>
 *
 * <pre>
 *     public class StringEventHandler extends
 *             {@link SimpleUserEventChannelHandler}&lt;{@link String}&gt; {
 *
 *         {@code @Override}
 *         protected void eventReceived({@link ChannelHandlerContext} ctx, {@link String} evt)
 *                 throws {@link Exception} {
 *             System.out.println(evt);
 *         }
 *     }
 * </pre>
 *
 * <p>
 * 根据构造参数，已处理的事件可能通过 {@link ReferenceCountUtil#release(Object)} 自动释放。
 * 若需将事件传给 Pipeline 中下一 Handler，可能需要 {@link ReferenceCountUtil#retain(Object)}。
 * </p>
 */
public abstract class SimpleUserEventChannelHandler<I> extends ChannelInboundHandlerAdapter {

    /** 用户事件类型匹配器 */
    private final TypeParameterMatcher matcher;
    /** 是否在处理后自动 release 事件 */
    private final boolean autoRelease;

    /**
     * 等价于 {@link #SimpleUserEventChannelHandler(boolean)}，{@code autoRelease} 为 {@code true}。
     */
    protected SimpleUserEventChannelHandler() {
        this(true);
    }

    /**
     * 从子类泛型参数自动推断待匹配的事件类型。
     *
     * @param autoRelease   为 {@code true} 时，已处理事件会通过
     *                      {@link ReferenceCountUtil#release(Object)} 自动释放
     */
    protected SimpleUserEventChannelHandler(boolean autoRelease) {
        matcher = TypeParameterMatcher.find(this, SimpleUserEventChannelHandler.class, "I");
        this.autoRelease = autoRelease;
    }

    /**
     * 等价于 {@link #SimpleUserEventChannelHandler(Class, boolean)}，{@code autoRelease} 为 {@code true}。
     */
    protected SimpleUserEventChannelHandler(Class<? extends I> eventType) {
        this(eventType, true);
    }

    /**
     * 显式指定要匹配的用户事件类型。
     *
     * @param eventType      要处理的事件类型
     * @param autoRelease    为 {@code true} 时自动释放已处理事件
     */
    protected SimpleUserEventChannelHandler(Class<? extends I> eventType, boolean autoRelease) {
        matcher = TypeParameterMatcher.get(eventType);
        this.autoRelease = autoRelease;
    }

    /**
     * 判断给定用户事件是否应由本 Handler 处理。
     * <p>
     * 返回 {@code false} 时事件会传递给 {@link ChannelPipeline} 中的下一个
     * {@link ChannelInboundHandler}。
     * </p>
     */
    protected boolean acceptEvent(Object evt) throws Exception {
        return matcher.match(evt);
    }

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean release = true;
        try {
            if (acceptEvent(evt)) {
                @SuppressWarnings("unchecked")
                I ievt = (I) evt;
                eventReceived(ctx, ievt);
            } else {
                release = false;
                ctx.fireUserEventTriggered(evt);
            }
        } finally {
            if (autoRelease && release) {
                ReferenceCountUtil.release(evt);
            }
        }
    }

    /**
     * 对类型为 {@link I} 的每个用户事件调用。
     *
     * @param ctx 本 Handler 所属的 {@link ChannelHandlerContext}
     * @param evt 待处理的用户事件
     *
     * @throws Exception 处理出错时抛出
     */
    protected abstract void eventReceived(ChannelHandlerContext ctx, I evt) throws Exception;
}
