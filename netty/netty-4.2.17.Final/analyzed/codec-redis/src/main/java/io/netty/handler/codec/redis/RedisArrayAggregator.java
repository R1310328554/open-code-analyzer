/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.redis;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Aggregates {@link RedisMessage} parts into {@link ArrayRedisMessage}. This decoder
 * should be used together with {@link RedisDecoder}.
 * <p>将 {@link RedisDecoder} 逐条输出的 {@link ArrayHeaderRedisMessage} 及其子元素
 * 聚合成完整的 {@link ArrayRedisMessage}。RESP 数组可能嵌套，解码器用栈式
 * {@link AggregateState} 跟踪各层期望长度；须与 {@link RedisDecoder} 串联使用。</p>
 */
@UnstableApi
public final class RedisArrayAggregator extends MessageToMessageDecoder<RedisMessage> {

    private static final int DEFAULT_MAX_ARRAY_LENGTH = RedisConstants.REDIS_MAX_ARRAY_LENGTH;
    /** 允许嵌套数组的最大深度，防止恶意或异常协议导致栈溢出。 */
    private final int maxNestedArrayDepth;
    /** 当前正在组装的嵌套数组栈，栈顶为最内层。 */
    private final Deque<AggregateState> depths = new ArrayDeque<AggregateState>(4);
    /** 单个数组可聚合的最大元素个数（Java List 为 int 索引）。 */
    private final int maxElements;

    /**
     * Create a new instance that will aggregate an {@link ArrayHeaderRedisMessage}
     * and its subsequent elements into an {@link ArrayRedisMessage}.
     * <p>
     * This constructor specifies a maximum number of elements of 1.000.000,
     * but this default can be increased with the {@value RedisConstants#PROP_REDIS_MAX_ARRAY_LENGTH} system property.
     * <p>默认最多聚合 100 万元素、嵌套深度 1024；可通过系统属性
     * {@value RedisConstants#PROP_REDIS_MAX_ARRAY_LENGTH} 调整数组长度上限。</p>
     *
     * @deprecated Use {@link #RedisArrayAggregator(int, int)} instead to define a max size of the array to aggregate.
     */
    @Deprecated
    public RedisArrayAggregator() {
        // Let's impose some limit at least by default.
        this(DEFAULT_MAX_ARRAY_LENGTH, 1024);
    }

    /**
     * Create a new instance that will aggregate an {@link ArrayHeaderRedisMessage}
     * and its subsequent elements into an {@link ArrayRedisMessage}.
     * <p>
     * A {@link CodecException} will be thrown if the array header specify a length greater than
     * the given number of max elements.
     * @param maxElements The maximum number of elements to aggregate in a single message.
     * @param maxNestedArrayDepth   the maximum depth of the nested array before an exception will be thrown
     */
    public RedisArrayAggregator(int maxElements, int maxNestedArrayDepth) {
        super(RedisMessage.class);
        this.maxElements = ObjectUtil.checkPositive(maxElements, "maxElements");
        this.maxNestedArrayDepth = ObjectUtil.checkPositive(maxNestedArrayDepth, "maxNestedArrayDepth");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, RedisMessage msg, List<Object> out) throws Exception {
        if (msg instanceof ArrayHeaderRedisMessage) {
            msg = decodeRedisArrayHeader((ArrayHeaderRedisMessage) msg);
            if (msg == null) {
                return;
            }
        } else {
            ReferenceCountUtil.retain(msg);
        }

        while (!depths.isEmpty()) {
            AggregateState current = depths.peek();
            current.children.add(msg);

            // if current aggregation completed, go to parent aggregation.
            // 当前层元素收齐则弹出并向上层继续组装（支持嵌套数组）。
            if (current.children.size() == current.length) {
                msg = new ArrayRedisMessage(current.children);
                depths.pop();
            } else {
                // not aggregated yet. try next time.
                return;
            }
        }

        out.add(msg);
    }

    private CodecException clearAndCreateException(String msg) {
        releaseAndClearDepths();
        return new CodecException(msg);
    }

    private RedisMessage decodeRedisArrayHeader(ArrayHeaderRedisMessage header) {
        if (header.isNull()) {
            return ArrayRedisMessage.NULL_INSTANCE;
        } else if (header.length() == 0L) {
            return ArrayRedisMessage.EMPTY_INSTANCE;
        } else if (header.length() > 0L) {
            // Currently, this codec doesn't support `long` length for arrays because Java's List.size() is int.
            if (header.length() > maxElements) {
                throw clearAndCreateException("this codec doesn't support longer length than " + maxElements);
            }

            if (depths.size() >= maxNestedArrayDepth) {
                throw clearAndCreateException("max nested array depth exceeded: "  + maxNestedArrayDepth);
            }
            // start aggregating array
            depths.push(new AggregateState((int) header.length()));
            return null;
        } else {
            throw clearAndCreateException("bad length: " + header.length());
        }
    }

    /** 单层数组聚合状态：期望子元素个数与已收集列表。 */
    private static final class AggregateState {
        private final int length;
        private final List<RedisMessage> children;
        AggregateState(int length) {
            this.length = length;
            this.children = new ArrayList<RedisMessage>(length);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        releaseAndClearDepths();
    }

    /** Handler 移除或异常时释放栈内未完成的子消息引用。 */
    private void releaseAndClearDepths() {
        for (AggregateState state : depths) {
            for (RedisMessage message : state.children) {
                ReferenceCountUtil.safeRelease(message);
            }
        }
        depths.clear();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (!depths.isEmpty()) {
            ctx.fireExceptionCaught(new PrematureChannelClosureException(
                    "channel gone inactive with " + depths.size() +
                            " messages still incomplete"));
        }
    }
}
