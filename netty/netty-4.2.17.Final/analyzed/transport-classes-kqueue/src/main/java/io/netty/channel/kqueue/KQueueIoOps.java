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
package io.netty.channel.kqueue;

import io.netty.channel.IoOps;

/**
 * Implementation of {@link IoOps} for
 * that is used by {@link KQueueIoHandler} and so for kqueue based transports.
 * <p>封装一条 kevent changelist 项：filter、flags、fflags 与 data。</p>
 */
public final class KQueueIoOps implements IoOps {
    /** kevent 过滤器（如 EVFILT_READ/WRITE） */
    private final short filter;
    /** EV_ADD/EV_ENABLE/EV_DELETE 等通用 flags */
    private final short flags;
    /** 过滤器相关 fflags（如 NOTE_EOF） */
    private final int fflags;
    /** 过滤器相关 data 字段 */
    private final long data;

    /**
     * Creates a new {@link KQueueIoOps}.
     * <p>构造 kqueue I/O 操作描述，data 默认为 0。</p>
     *
     * @param filter    the filter for this event.
     * @param flags     the general flags.
     * @param fflags    filter-specific flags.
     * @return          {@link KQueueIoOps}.
     */
    public static KQueueIoOps newOps(short filter, short flags, int fflags) {
        return new KQueueIoOps(filter, flags, fflags, 0);
    }

    private KQueueIoOps(short filter, short flags, int fflags, long data) {
        this.filter = filter;
        this.flags = flags;
        this.fflags = fflags;
        this.data = data;
    }

    /**
     * Returns the filter for this event.
     * <p>返回 kevent 过滤器类型。</p>
     *
     * @return filter.
     */
    public short filter() {
        return filter;
    }

    /**
     * Returns the general flags.
     * <p>返回 kevent 通用 flags。</p>
     *
     * @return flags.
     */
    public short flags() {
        return flags;
    }

    /**
     * Returns filter-specific flags.
     * <p>返回过滤器相关 fflags。</p>
     *
     * @return fflags.
     */
    public int fflags() {
        return fflags;
    }

    /**
     * Returns filter-specific data.
     * <p>返回过滤器相关 data 字段。</p>
     *
     * @return data.
     */
    public long data() {
        return data;
    }

    @Override
    public String toString() {
        return "KQueueIoOps{" +
                "filter=" + filter +
                ", flags=" + flags +
                ", fflags=" + fflags +
                ", data=" + data +
                '}';
    }
}
