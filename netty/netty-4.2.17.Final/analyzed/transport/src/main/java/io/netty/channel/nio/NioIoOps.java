/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel.nio;

import io.netty.channel.IoOps;

import java.nio.channels.SelectionKey;

/**
 * Implementation of {@link IoOps} for
 * that is used by {@link NioIoHandler} and so for NIO based transports.
 * <p>{@link NioIoHandler} 使用的 {@link IoOps} 实现，对应 {@link SelectionKey} 的 interest/ready 位组合。</p>
 */
public final class NioIoOps implements IoOps {
    /**
     * Interested in NO IO events.
     * <p>不关注任何 I/O 事件。</p>
     */
    public static final NioIoOps NONE = new NioIoOps(0);

    /**
     * Interested in IO events that should be handled by accepting new connections
     * <p>关注 {@link SelectionKey#OP_ACCEPT}，用于接受新连接。</p>
     */
    public static final NioIoOps ACCEPT = new NioIoOps(SelectionKey.OP_ACCEPT);

    /**
     * Interested in IO events which should be handled by finish pending connect operations
     * <p>关注 {@link SelectionKey#OP_CONNECT}，用于完成挂起的 connect。</p>
     */
    public static final NioIoOps CONNECT = new NioIoOps(SelectionKey.OP_CONNECT);

    /**
     * Interested in IO events which tell that the underlying channel is writable again.
     * <p>关注 {@link SelectionKey#OP_WRITE}，表示底层 channel 可写。</p>
     */
    public static final NioIoOps WRITE = new NioIoOps(SelectionKey.OP_WRITE);

    /**
     * Interested in IO events which should be handled by reading data.
     * <p>关注 {@link SelectionKey#OP_READ}，用于读数据。</p>
     */
    public static final NioIoOps READ = new NioIoOps(SelectionKey.OP_READ);

    /**
     * Interested in IO events which should be either handled by reading or accepting.
     * <p>同时关注读与 accept（{@code OP_READ | OP_ACCEPT}）。</p>
     */
    public static final NioIoOps READ_AND_ACCEPT = new NioIoOps(SelectionKey.OP_READ | SelectionKey.OP_ACCEPT);

    /**
     * Interested in IO events which should be either handled by reading or writing.
     * <p>同时关注读与写（{@code OP_READ | OP_WRITE}）。</p>
     */
    public static final NioIoOps READ_AND_WRITE = new NioIoOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

    // 常用组合预缓存为 NioIoEvent 数组，避免重复分配
    private static final NioIoEvent[] EVENTS;

    static {
        NioIoOps all = new NioIoOps(
                NONE.value | ACCEPT.value | CONNECT.value | WRITE.value | READ.value);

        EVENTS = new NioIoEvent[all.value + 1];
        addToArray(EVENTS, NONE);
        addToArray(EVENTS, ACCEPT);
        addToArray(EVENTS, CONNECT);
        addToArray(EVENTS, WRITE);
        addToArray(EVENTS, READ);
        addToArray(EVENTS, READ_AND_ACCEPT);
        addToArray(EVENTS, READ_AND_WRITE);
        addToArray(EVENTS, all);
    }

    private static void addToArray(NioIoEvent[] array, NioIoOps opt) {
        array[opt.value] = new DefaultNioIoEvent(opt);
    }

    final int value;

    private NioIoOps(int value) {
        this.value = value;
    }

    /**
     * Returns {@code true} if this {@link NioIoOps} is a combination of the given {@link NioIoOps}.
     * <p>当前 ops 是否包含给定 {@link NioIoOps} 的全部位。</p>
     * @param ops   the ops.
     * @return      {@code true} if a combination of the given.
     */
    public boolean contains(NioIoOps ops) {
        return isIncludedIn(ops.value);
    }

    /**
     * Return a {@link NioIoOps} which is a combination of the current and the given {@link NioIoOps}.
     * <p>与给定 ops 按位或，得到新的 interest 组合。</p>
     *
     * @param ops   the {@link NioIoOps} that should be added to this one.
     * @return      a {@link NioIoOps}.
     */
    public NioIoOps with(NioIoOps ops) {
        if (contains(ops)) {
            return this;
        }
        return valueOf(value | ops.value());
    }

    /**
     * Return a {@link NioIoOps} which is not a combination of the current and the given {@link NioIoOps}.
     * <p>从当前 ops 中清除给定位的 interest。</p>
     *
     * @param ops   the {@link NioIoOps} that should be remove from this one.
     * @return      a {@link NioIoOps}.
     */
    public NioIoOps without(NioIoOps ops) {
        if (!contains(ops)) {
            return this;
        }
        return valueOf(value & ~ops.value());
    }

    /**
     * Returns the underlying ops value of the {@link NioIoOps}.
     * <p>返回底层 {@link SelectionKey} interest 整型值。</p>
     *
     * @return value.
     */
    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NioIoOps nioOps = (NioIoOps) o;
        return value == nioOps.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Returns a {@link NioIoOps} for the given value.
     * <p>由 interest 整型值构造或复用 {@link NioIoOps}。</p>
     *
     * @param   value the value
     * @return  the {@link NioIoOps}.
     */
    public static NioIoOps valueOf(int value) {
        return eventOf(value).ops();
    }

    /**
     * Returns {@code true} if this {@link NioIoOps} is <strong>included </strong> in the given {@code ops}.
     * <p>本 ops 的位是否全部包含在给定 {@code ops} 中。</p>
     *
     * @param ops   the ops to check.
     * @return      {@code true} if <strong>included</strong>, {@code false} otherwise.
     */
    public boolean isIncludedIn(int ops) {
        return (ops & value) != 0;
    }

    /**
     * Returns {@code true} if this {@link NioIoOps} is <strong>not included</strong> in the given {@code ops}.
     * <p>本 ops 的位是否均未包含在给定 {@code ops} 中。</p>
     *
     * @param ops   the ops to check.
     * @return      {@code true} if <strong>not included</strong>, {@code false} otherwise.
     */
    public boolean isNotIncludedIn(int ops) {
        return (ops & value) == 0;
    }

    static NioIoEvent eventOf(int value) {
        if (value > 0 && value < EVENTS.length) {
            NioIoEvent event = EVENTS[value];
            if (event != null) {
                return event;
            }
        }
        return new DefaultNioIoEvent(new NioIoOps(value));
    }

    private static final class DefaultNioIoEvent implements NioIoEvent {
        /** 就绪的 interest 组合 */
        private final NioIoOps ops;

        DefaultNioIoEvent(NioIoOps ops) {
            this.ops = ops;
        }

        @Override
        public NioIoOps ops() {
            return ops;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NioIoEvent event = (NioIoEvent) o;
            return event.ops().equals(ops());
        }

        @Override
        public int hashCode() {
            return ops().hashCode();
        }
    }
}
