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

import io.netty.channel.ChannelHandlerMask.Skip;
import io.netty.util.internal.InternalThreadLocalMap;

import java.util.Map;

/**
 * Skeleton implementation of a {@link ChannelHandler}.
 * <p>{@link ChannelHandler} 的骨架实现，提供 {@link Sharable} 检测、默认空实现的
 * {@link #handlerAdded}/{@link #handlerRemoved}，以及将 {@link #exceptionCaught} 转发至管道的默认行为。</p>
 */
public abstract class ChannelHandlerAdapter implements ChannelHandler {

    // Not using volatile because it's used only for a sanity check.
    /** 标记处理器是否已加入管道，用于 {@link #ensureNotSharable()} 一致性检查（非 volatile，仅作断言）。 */
    boolean added;

    /**
     * Throws {@link IllegalStateException} if {@link ChannelHandlerAdapter#isSharable()} returns {@code true}
     * <p>若本处理器标注为 {@link Sharable} 却持有每连接状态，则抛出 {@link IllegalStateException}。</p>
     */
    protected void ensureNotSharable() {
        if (isSharable()) {
            throw new IllegalStateException("ChannelHandler " + getClass().getName() + " is not allowed to be shared");
        }
    }

    /**
     * Return {@code true} if the implementation is {@link Sharable} and so can be added
     * to different {@link ChannelPipeline}s.
     * <p>检测类是否标注 {@link Sharable}；结果按线程缓存以避免重复反射与 volatile 读写。</p>
     */
    public boolean isSharable() {
        /**
         * Cache the result of {@link Sharable} annotation detection to workaround a condition. We use a
         * {@link ThreadLocal} and {@link WeakHashMap} to eliminate the volatile write/reads. Using different
         * {@link WeakHashMap} instances per {@link Thread} is good enough for us and the number of
         * {@link Thread}s are quite limited anyway.
         *
         * See <a href="https://github.com/netty/netty/issues/2289">#2289</a>.
         */
        Class<?> clazz = getClass();
        Map<Class<?>, Boolean> cache = InternalThreadLocalMap.get().handlerSharableCache();
        Boolean sharable = cache.get(clazz);
        if (sharable == null) {
            sharable = clazz.isAnnotationPresent(Sharable.class);
            cache.put(clazz, sharable);
        }
        return sharable;
    }

    /**
     * Do nothing by default, sub-classes may override this method.
     * <p>默认无操作；子类可在处理器加入管道后初始化资源。</p>
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * Do nothing by default, sub-classes may override this method.
     * <p>默认无操作；子类可在处理器从管道移除后释放资源。</p>
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward
     * to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     * <p>将未捕获异常沿管道向下游传播；子类可覆盖以自定义处理逻辑。</p>
     *
     * @deprecated is part of {@link ChannelInboundHandler}
     */
    @Skip
    @Override
    @Deprecated
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
