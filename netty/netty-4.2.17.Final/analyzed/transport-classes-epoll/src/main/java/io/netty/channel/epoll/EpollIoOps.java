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
package io.netty.channel.epoll;

import io.netty.channel.IoOps;

/**
 * Implementation of {@link IoOps} that is used by {@link EpollIoHandler} and so for epoll based transports.
 * <p>epoll 传输使用的 {@link IoOps} 实现，封装 Linux epoll 事件位掩码。</p>
 */
public final class EpollIoOps implements IoOps {

    static {
        // 构造常量前须先加载 native 库
        Epoll.ensureAvailability();
    }

    /**
     * Interested in IO events which tell that the underlying channel is writable again or a connection
     * attempt can be continued.
     * <p>关注可写或连接可继续的 epoll 事件（EPOLLOUT）。</p>
     */
    public static final EpollIoOps EPOLLOUT = new EpollIoOps(Native.EPOLLOUT);

    /**
     * Interested in IO events which should be handled by finish pending connect operations
     * <p>关注可读事件（EPOLLIN），用于完成挂起连接或读取数据。</p>
     */
    public static final EpollIoOps EPOLLIN = new EpollIoOps(Native.EPOLLIN);

    /**
     * Error condition happened on the associated file descriptor.
     * <p>关联 fd 发生错误（EPOLLERR）。</p>
     */
    public static final EpollIoOps EPOLLERR = new EpollIoOps(Native.EPOLLERR);

    /**
     * Interested in IO events which should be handled by reading data.
     * <p>对端关闭或半关闭（EPOLLRDHUP），需继续读至 EOF。</p>
     */
    public static final EpollIoOps EPOLLRDHUP = new EpollIoOps(Native.EPOLLRDHUP);

    public static final EpollIoOps EPOLLET = new EpollIoOps(Native.EPOLLET);

    /**
     * Special {@link EpollIoOps} which basically means we are not interested in any event and so should remove the
     * fd from underlying epoll fd.
     * <p>值为 0 表示取消监听，从 epoll 集移除 fd。</p>
     */
    public static final EpollIoOps NONE = new EpollIoOps(0);

    static final int EPOLL_ERR_OUT_MASK = EpollIoOps.EPOLLERR.value | EpollIoOps.EPOLLOUT.value;
    static final int EPOLL_ERR_IN_MASK = EpollIoOps.EPOLLERR.value | EpollIoOps.EPOLLIN.value;
    static final int EPOLL_RDHUP_MASK = EpollIoOps.EPOLLRDHUP.value;

    // 用数组缓存常用 EpollIoEvent，避免重复分配
    private static final EpollIoEvent[] EVENTS;

    static {
        EpollIoOps all = new EpollIoOps(EPOLLOUT.value | EPOLLIN.value | EPOLLERR.value | EPOLLRDHUP.value);
        EVENTS = new EpollIoEvent[all.value + 2];
        addToArray(EVENTS, NONE);
        addToArray(EVENTS, EPOLLOUT);
        addToArray(EVENTS, EPOLLIN);
        addToArray(EVENTS, EPOLLERR);
        addToArray(EVENTS, EPOLLRDHUP);
        addToArray(EVENTS, all);
    }

    private static void addToArray(EpollIoEvent[] array, EpollIoOps ops) {
        array[ops.value] = new DefaultEpollIoEvent(ops);
    }

    final int value;

    private EpollIoOps(int value) {
        this.value = value;
    }

    /**
     * Returns {@code true} if this {@link EpollIoOps} is a combination of the given {@link EpollIoOps}.
     * <p>判断当前掩码是否包含给定 ops 的位。</p>
     * @param ops   the ops.
     * @return      {@code true} if a combination of the given.
     */
    public boolean contains(EpollIoOps ops) {
        return (value & ops.value) != 0;
    }

    boolean contains(int value) {
        return (this.value & value) != 0;
    }

    /**
     * Return a {@link EpollIoOps} which is a combination of the current and the given {@link EpollIoOps}.
     * <p>与给定 ops 做按位或，合并监听兴趣集。</p>
     *
     * @param ops   the {@link EpollIoOps} that should be added to this one.
     * @return      a {@link EpollIoOps}.
     */
    public EpollIoOps with(EpollIoOps ops) {
        if (contains(ops)) {
            return this;
        }
        return valueOf(value | ops.value());
    }

    /**
     * Return a {@link EpollIoOps} which is not a combination of the current and the given {@link EpollIoOps}.
     * <p>清除给定 ops 位，得到新的掩码。</p>
     *
     * @param ops   the {@link EpollIoOps} that should be remove from this one.
     * @return      a {@link EpollIoOps}.
     */
    public EpollIoOps without(EpollIoOps ops) {
        if (!contains(ops)) {
            return this;
        }
        return valueOf(value & ~ops.value());
    }

    /**
     * Returns the underlying value of the {@link EpollIoOps}.
     * <p>返回原生 epoll 事件整型掩码。</p>
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
        EpollIoOps nioOps = (EpollIoOps) o;
        return value == nioOps.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Returns a {@link EpollIoOps} for the given value.
     * <p>由整型掩码构造或查表得到 {@link EpollIoOps}。</p>
     *
     * @param   value the value
     * @return  the {@link EpollIoOps}.
     */
    public static EpollIoOps valueOf(int value) {
        return eventOf(value).ops();
    }

    @Override
    public String toString() {
        return "EpollIoOps{" +
                "value=" + value +
                '}';
    }

    static EpollIoEvent eventOf(int value) {
        if (value > 0 && value < EVENTS.length) {
            EpollIoEvent event = EVENTS[value];
            if (event != null) {
                return event;
            }
        }
        return new DefaultEpollIoEvent(new EpollIoOps(value));
    }

    private static final class DefaultEpollIoEvent implements EpollIoEvent {
        private final EpollIoOps ops;

        DefaultEpollIoEvent(EpollIoOps ops) {
            this.ops = ops;
        }

        @Override
        public EpollIoOps ops() {
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
            EpollIoEvent event = (EpollIoEvent) o;
            return event.ops().equals(ops());
        }

        @Override
        public int hashCode() {
            return ops().hashCode();
        }

        @Override
        public String toString() {
            return "DefaultEpollIoEvent{" +
                    "ops=" + ops +
                    '}';
        }
    }
}
