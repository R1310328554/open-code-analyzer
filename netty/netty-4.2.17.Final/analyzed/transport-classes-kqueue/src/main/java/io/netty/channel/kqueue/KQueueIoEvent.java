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

import io.netty.channel.IoEvent;

/**
 * {@link IoEvent} to use with {@link KQueueIoHandler}.
 * <p>{@code kevent(2)} 完成事件的 Java 表示：ident、filter、flags、fflags、data 与 udata。</p>
 */
public final class KQueueIoEvent implements IoEvent {
    /** 事件标识（通常为文件描述符） */
    private int ident;
    /** kevent 过滤器（如 EVFILT_READ/EVFILT_WRITE） */
    private short filter;
    private short flags;
    private int fflags;
    private long data;
    private long udata;

    /**
     * Creates a new {@link KQueueIoEvent}.
     *
     * @param ident     the identifier for this event.
     * @param filter    the filter for this event.
     * @param flags     the general flags.
     * @param fflags    filter-specific flags.
     * @return          {@link KQueueIoEvent}.
     * @deprecated use {@link #newEvent(int, short, short, int, long, long)}
      * <p>Netty KQueue 传输 API；详见上方英文说明。</p>
     */
    @Deprecated
    public static KQueueIoEvent newEvent(int ident, short filter, short flags, int fflags) {
        return new KQueueIoEvent(ident, filter, flags, fflags, 0, 0);
    }

    /**
     * Creates a new {@link KQueueIoEvent}.
     *
     * @param ident     the identifier for this event.
     * @param filter    the filter for this event.
     * @param flags     the general flags.
     * @param fflags    filter-specific flags.
     * @param data      the data
     * @param udata     the user defined data that is passed through.
     * @return          {@link KQueueIoEvent}.
      * <p>Netty KQueue 传输 API；详见上方英文说明。</p>
     */
    public static KQueueIoEvent newEvent(int ident, short filter, short flags, int fflags, long data, long udata) {
        return new KQueueIoEvent(ident, filter, flags, fflags, data, udata);
    }

    private KQueueIoEvent(int ident, short filter, short flags, int fflags, long data, long udata) {
        this.ident = ident;
        this.filter = filter;
        this.flags = flags;
        this.fflags = fflags;
        this.data = data;
        this.udata = udata;
    }

    KQueueIoEvent() {
        this(0, (short) 0, (short) 0, 0, 0, 0);
    }

    // 内部复用同一实例，减少 kevent 处理时的对象分配
    void update(int ident, short filter, short flags, int fflags, long data, long udata) {
        this.ident = ident;
        this.filter = filter;
        this.flags = flags;
        this.fflags = fflags;
        this.data = data;
        this.udata = udata;
    }

    /**
     * Returns the identifier for this event.
     * <p>返回 kevent.ident（通常为 fd）。</p>
     *
     * @return  ident.
     */
    public int ident() {
        return ident;
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
     * <p>过滤器相关 data 字段（如可读字节数）。</p>
     *
     * @return data.
     */
    public long data() {
        return data;
    }

    /**
     * Returns user specified data.
     * <p>提交 changelist 时附带的 udata（关联 IoRegistration）。</p>
     *
     * @return udata.
     */
    public long udata() {
        return udata;
    }

    @Override
    public String toString() {
        return "KQueueIoEvent{" +
                "ident=" + ident +
                ", filter=" + filter +
                ", flags=" + flags +
                ", fflags=" + fflags +
                ", data=" + data +
                ", udata=" + udata +
                '}';
    }
}
