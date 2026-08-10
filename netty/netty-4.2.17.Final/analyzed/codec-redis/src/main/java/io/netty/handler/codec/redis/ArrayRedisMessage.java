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

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.UnstableApi;

import java.util.Collections;
import java.util.List;

/**
 * Arrays of <a href="https://redis.io/topics/protocol">RESP</a>.
 * <p>聚合后的 RESP 数组，持有已解码的子 {@link RedisMessage} 列表。实现引用计数：
 * {@link #release()} 时会递归释放所有子元素。多数 Redis 命令/回复以数组形式组织（如
 * {@code *2\r\n$3\r\nSET\r\n$3\r\nfoo\r\n}）。</p>
 */
@UnstableApi
public class ArrayRedisMessage extends AbstractReferenceCounted implements RedisMessage {

    /** 数组子元素；构造时不额外 retain，因各子消息在创建时已持有引用。 */
    private final List<RedisMessage> children;

    private ArrayRedisMessage() {
        children = Collections.emptyList();
    }

    /**
     * Creates a {@link ArrayRedisMessage} for the given {@code content}.
     *
     * @param children the children.
     */
    public ArrayRedisMessage(List<RedisMessage> children) {
        // do not retain here. children are already retained when created.
        this.children = ObjectUtil.checkNotNull(children, "children");
    }

    /**
     * Get children of this Arrays. It can be null or empty.
     *
     * @return list of {@link RedisMessage}s.
     */
    public final List<RedisMessage> children() {
        return children;
    }

    /**
     * Returns whether the content of this message is {@code null}.
     *
     * @return indicates whether the content of this message is {@code null}.
     */
    public boolean isNull() {
        return false;
    }

    @Override
    protected void deallocate() {
        for (RedisMessage msg : children) {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public ArrayRedisMessage touch(Object hint) {
        for (RedisMessage msg : children) {
            ReferenceCountUtil.touch(msg);
        }
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
                .append('[')
                .append("children=")
                .append(children.size())
                .append(']').toString();
    }

    /**
     * A predefined null array instance for {@link ArrayRedisMessage}.
     * <p>对应 RESP {@code *-1\r\n}，单例且 retain/release 均为空操作。</p>
     */
    public static final ArrayRedisMessage NULL_INSTANCE = new ArrayRedisMessage() {
        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public ArrayRedisMessage retain() {
            return this;
        }

        @Override
        public ArrayRedisMessage retain(int increment) {
            return this;
        }

        @Override
        public ArrayRedisMessage touch() {
            return this;
        }

        @Override
        public ArrayRedisMessage touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return false;
        }

        @Override
        public boolean release(int decrement) {
            return false;
        }

        @Override
        public String toString() {
            return "NullArrayRedisMessage";
        }
    };

    /**
     * A predefined empty array instance for {@link ArrayRedisMessage}.
     * <p>对应 RESP {@code *0\r\n}，元素列表为空且不可变。</p>
     */
    public static final ArrayRedisMessage EMPTY_INSTANCE = new ArrayRedisMessage() {

        @Override
        public ArrayRedisMessage retain() {
            return this;
        }

        @Override
        public ArrayRedisMessage retain(int increment) {
            return this;
        }

        @Override
        public ArrayRedisMessage touch() {
            return this;
        }

        @Override
        public ArrayRedisMessage touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return false;
        }

        @Override
        public boolean release(int decrement) {
            return false;
        }

        @Override
        public String toString() {
            return "EmptyArrayRedisMessage";
        }
    };

}
