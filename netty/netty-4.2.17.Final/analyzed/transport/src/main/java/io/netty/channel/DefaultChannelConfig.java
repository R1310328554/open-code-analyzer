/*
 * Copyright 2012 The Netty Project
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

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.internal.ObjectUtil;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static io.netty.channel.ChannelOption.ALLOCATOR;
import static io.netty.channel.ChannelOption.AUTO_CLOSE;
import static io.netty.channel.ChannelOption.AUTO_READ;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static io.netty.channel.ChannelOption.MAX_MESSAGES_PER_READ;
import static io.netty.channel.ChannelOption.MAX_MESSAGES_PER_WRITE;
import static io.netty.channel.ChannelOption.MESSAGE_SIZE_ESTIMATOR;
import static io.netty.channel.ChannelOption.RECVBUF_ALLOCATOR;
import static io.netty.channel.ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_LOW_WATER_MARK;
import static io.netty.channel.ChannelOption.WRITE_BUFFER_WATER_MARK;
import static io.netty.channel.ChannelOption.WRITE_SPIN_COUNT;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositive;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * The default {@link ChannelConfig} implementation.
 * <p>{@link ChannelConfig} 的默认实现：管理连接超时、自动读/关、写缓冲水位、
 * {@link ByteBufAllocator}、接收缓冲分配器等通道级配置项。</p>
 */
public class DefaultChannelConfig implements ChannelConfig {
    private static final MessageSizeEstimator DEFAULT_MSG_SIZE_ESTIMATOR = DefaultMessageSizeEstimator.DEFAULT;

    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;

    private static final AtomicIntegerFieldUpdater<DefaultChannelConfig> AUTOREAD_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(DefaultChannelConfig.class, "autoRead");
    private static final AtomicReferenceFieldUpdater<DefaultChannelConfig, WriteBufferWaterMark> WATERMARK_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(
                    DefaultChannelConfig.class, WriteBufferWaterMark.class, "writeBufferWaterMark");

    /** 所属 {@link Channel} */
    protected final Channel channel;

    /** 出站 {@link ByteBuf} 分配器 */
    private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
    /** 入站接收缓冲分配策略 */
    private volatile RecvByteBufAllocator rcvBufAllocator;
    /** 消息大小估算器，用于写缓冲水位计算 */
    private volatile MessageSizeEstimator msgSizeEstimator = DEFAULT_MSG_SIZE_ESTIMATOR;

    /** 连接超时（毫秒），默认 30000 */
    private volatile int connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT;
    /** 单次写自旋次数上限 */
    private volatile int writeSpinCount = 16;
    /** 单次 EventLoop 迭代最多写入的消息数 */
    private volatile int maxMessagesPerWrite = Integer.MAX_VALUE;

    @SuppressWarnings("FieldMayBeFinal")
    /** 是否自动读：1 表示开启 */
    private volatile int autoRead = 1;
    /** 通道不可读/不可写时是否自动关闭 */
    private volatile boolean autoClose = true;
    /** 写缓冲高低水位标记 */
    private volatile WriteBufferWaterMark writeBufferWaterMark = WriteBufferWaterMark.DEFAULT;
    /** 是否将同一 EventExecutorGroup 固定绑定到单个子 Executor */
    private volatile boolean pinEventExecutor = true;

    /** 使用默认 {@link AdaptiveRecvByteBufAllocator} 创建配置。 */
    public DefaultChannelConfig(Channel channel) {
        this(channel, new AdaptiveRecvByteBufAllocator());
    }

    /** 指定接收缓冲分配器创建配置。 */
    protected DefaultChannelConfig(Channel channel, RecvByteBufAllocator allocator) {
        setRecvByteBufAllocator(allocator, channel.metadata());
        this.channel = channel;
    }

    /** 返回所有已支持的 {@link ChannelOption} 及其当前值。 */
    @Override
    @SuppressWarnings("deprecation")
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(
                null,
                CONNECT_TIMEOUT_MILLIS, MAX_MESSAGES_PER_READ, WRITE_SPIN_COUNT,
                ALLOCATOR, AUTO_READ, AUTO_CLOSE, RECVBUF_ALLOCATOR, WRITE_BUFFER_HIGH_WATER_MARK,
                WRITE_BUFFER_LOW_WATER_MARK, WRITE_BUFFER_WATER_MARK, MESSAGE_SIZE_ESTIMATOR,
                SINGLE_EVENTEXECUTOR_PER_GROUP, MAX_MESSAGES_PER_WRITE);
    }

    /** 从给定选项数组填充结果 Map；子类可扩展选项集合。 */
    protected Map<ChannelOption<?>, Object> getOptions(
            Map<ChannelOption<?>, Object> result, ChannelOption<?>... options) {
        if (result == null) {
            result = new IdentityHashMap<ChannelOption<?>, Object>();
        }
        for (ChannelOption<?> o: options) {
            result.put(o, getOption(o));
        }
        return result;
    }

    /** 批量设置配置项；未知选项返回 {@code false} 且不影响其他项。 */
    @SuppressWarnings("unchecked")
    @Override
    public boolean setOptions(Map<ChannelOption<?>, ?> options) {
        ObjectUtil.checkNotNull(options, "options");

        boolean setAllOptions = true;
        for (Entry<ChannelOption<?>, ?> e: options.entrySet()) {
            if (!setOption((ChannelOption<Object>) e.getKey(), e.getValue())) {
                setAllOptions = false;
            }
        }

        return setAllOptions;
    }

    /** 读取单个 {@link ChannelOption} 的当前值。 */
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    public <T> T getOption(ChannelOption<T> option) {
        ObjectUtil.checkNotNull(option, "option");

        if (option == CONNECT_TIMEOUT_MILLIS) {
            return (T) Integer.valueOf(getConnectTimeoutMillis());
        }
        if (option == MAX_MESSAGES_PER_READ) {
            return (T) Integer.valueOf(getMaxMessagesPerRead());
        }
        if (option == WRITE_SPIN_COUNT) {
            return (T) Integer.valueOf(getWriteSpinCount());
        }
        if (option == ALLOCATOR) {
            return (T) getAllocator();
        }
        if (option == RECVBUF_ALLOCATOR) {
            return (T) getRecvByteBufAllocator();
        }
        if (option == AUTO_READ) {
            return (T) Boolean.valueOf(isAutoRead());
        }
        if (option == AUTO_CLOSE) {
            return (T) Boolean.valueOf(isAutoClose());
        }
        if (option == WRITE_BUFFER_HIGH_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferHighWaterMark());
        }
        if (option == WRITE_BUFFER_LOW_WATER_MARK) {
            return (T) Integer.valueOf(getWriteBufferLowWaterMark());
        }
        if (option == WRITE_BUFFER_WATER_MARK) {
            return (T) getWriteBufferWaterMark();
        }
        if (option == MESSAGE_SIZE_ESTIMATOR) {
            return (T) getMessageSizeEstimator();
        }
        if (option == SINGLE_EVENTEXECUTOR_PER_GROUP) {
            return (T) Boolean.valueOf(getPinEventExecutorPerGroup());
        }
        if (option == MAX_MESSAGES_PER_WRITE) {
            return (T) Integer.valueOf(getMaxMessagesPerWrite());
        }
        return null;
    }

    /** 设置单个 {@link ChannelOption}；不支持的选项返回 {@code false}。 */
    @Override
    @SuppressWarnings("deprecation")
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == CONNECT_TIMEOUT_MILLIS) {
            setConnectTimeoutMillis((Integer) value);
        } else if (option == MAX_MESSAGES_PER_READ) {
            setMaxMessagesPerRead((Integer) value);
        } else if (option == WRITE_SPIN_COUNT) {
            setWriteSpinCount((Integer) value);
        } else if (option == ALLOCATOR) {
            setAllocator((ByteBufAllocator) value);
        } else if (option == RECVBUF_ALLOCATOR) {
            setRecvByteBufAllocator((RecvByteBufAllocator) value);
        } else if (option == AUTO_READ) {
            setAutoRead((Boolean) value);
        } else if (option == AUTO_CLOSE) {
            setAutoClose((Boolean) value);
        } else if (option == WRITE_BUFFER_HIGH_WATER_MARK) {
            setWriteBufferHighWaterMark((Integer) value);
        } else if (option == WRITE_BUFFER_LOW_WATER_MARK) {
            setWriteBufferLowWaterMark((Integer) value);
        } else if (option == WRITE_BUFFER_WATER_MARK) {
            setWriteBufferWaterMark((WriteBufferWaterMark) value);
        } else if (option == MESSAGE_SIZE_ESTIMATOR) {
            setMessageSizeEstimator((MessageSizeEstimator) value);
        } else if (option == SINGLE_EVENTEXECUTOR_PER_GROUP) {
            setPinEventExecutorPerGroup((Boolean) value);
        } else if (option == MAX_MESSAGES_PER_WRITE) {
            setMaxMessagesPerWrite((Integer) value);
        } else {
            return false;
        }

        return true;
    }

    /** 校验选项名与值的合法性。 */
    protected <T> void validate(ChannelOption<T> option, T value) {
        ObjectUtil.checkNotNull(option, "option").validate(value);
    }

    /** 返回连接超时（毫秒）。 */
    @Override
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    /** 设置连接超时（毫秒），须 {@code >= 0}。 */
    @Override
    public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        checkPositiveOrZero(connectTimeoutMillis, "connectTimeoutMillis");
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * @throws IllegalStateException if {@link #getRecvByteBufAllocator()} does not return an object of type
     * {@link MaxMessagesRecvByteBufAllocator}.
     */
    @Override
    @Deprecated
    public int getMaxMessagesPerRead() {
        try {
            MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
            return allocator.maxMessagesPerRead();
        } catch (ClassCastException e) {
            throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type " +
                    "MaxMessagesRecvByteBufAllocator", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * @throws IllegalStateException if {@link #getRecvByteBufAllocator()} does not return an object of type
     * {@link MaxMessagesRecvByteBufAllocator}.
     */
    @Override
    @Deprecated
    public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        try {
            MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
            allocator.maxMessagesPerRead(maxMessagesPerRead);
            return this;
        } catch (ClassCastException e) {
            throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type " +
                    "MaxMessagesRecvByteBufAllocator", e);
        }
    }

    /**
     * Get the maximum number of message to write per eventloop run. Once this limit is
     * reached we will continue to process other events before trying to write the remaining messages.
     * <p>单次 EventLoop 运行中最多写入的消息数，达到上限后先处理其他事件再继续写。</p>
     */
    public int getMaxMessagesPerWrite() {
        return maxMessagesPerWrite;
    }

     /**
     * Set the maximum number of message to write per eventloop run. Once this limit is
     * reached we will continue to process other events before trying to write the remaining messages.
     * <p>设置单次 EventLoop 运行中的最大写入消息数。</p>
     */
    public ChannelConfig setMaxMessagesPerWrite(int maxMessagesPerWrite) {
        this.maxMessagesPerWrite = ObjectUtil.checkPositive(maxMessagesPerWrite, "maxMessagesPerWrite");
        return this;
    }

    /** 返回写自旋次数。 */
    @Override
    public int getWriteSpinCount() {
        return writeSpinCount;
    }

    /** 设置写自旋次数；{@link Integer#MAX_VALUE} 会被减 1 以避免与内部特殊值冲突。 */
    @Override
    public ChannelConfig setWriteSpinCount(int writeSpinCount) {
        checkPositive(writeSpinCount, "writeSpinCount");
        // Integer.MAX_VALUE 在通道实现中用作特殊标记，表示不可再写；此处减 1 避免与内部逻辑冲突
        if (writeSpinCount == Integer.MAX_VALUE) {
            --writeSpinCount;
        }
        this.writeSpinCount = writeSpinCount;
        return this;
    }

    /** 返回出站 {@link ByteBufAllocator}。 */
    @Override
    public ByteBufAllocator getAllocator() {
        return allocator;
    }

    /** 设置出站 {@link ByteBufAllocator}。 */
    @Override
    public ChannelConfig setAllocator(ByteBufAllocator allocator) {
        this.allocator = ObjectUtil.checkNotNull(allocator, "allocator");
        return this;
    }

    /** 返回入站接收缓冲分配器。 */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator() {
        return (T) rcvBufAllocator;
    }

    /** 设置入站接收缓冲分配器。 */
    @Override
    public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        rcvBufAllocator = checkNotNull(allocator, "allocator");
        return this;
    }

    /**
     * Set the {@link RecvByteBufAllocator} which is used for the channel to allocate receive buffers.
     * <p>设置接收缓冲分配器；若为 {@link MaxMessagesRecvByteBufAllocator} 则同步默认单次最大读消息数。</p>
     * @param allocator the allocator to set.
     * @param metadata Used to set the {@link ChannelMetadata#defaultMaxMessagesPerRead()} if {@code allocator}
     * is of type {@link MaxMessagesRecvByteBufAllocator}.
     */
    private void setRecvByteBufAllocator(RecvByteBufAllocator allocator, ChannelMetadata metadata) {
        checkNotNull(allocator, "allocator");
        checkNotNull(metadata, "metadata");
        if (allocator instanceof MaxMessagesRecvByteBufAllocator) {
            ((MaxMessagesRecvByteBufAllocator) allocator).maxMessagesPerRead(metadata.defaultMaxMessagesPerRead());
        }
        setRecvByteBufAllocator(allocator);
    }

    /** 是否开启自动读。 */
    @Override
    public boolean isAutoRead() {
        return autoRead == 1;
    }

    /** 设置自动读；从关到开时会触发 {@link Channel#read()}。 */
    @Override
    public ChannelConfig setAutoRead(boolean autoRead) {
        boolean oldAutoRead = AUTOREAD_UPDATER.getAndSet(this, autoRead ? 1 : 0) == 1;
        if (autoRead && !oldAutoRead) {
            channel.read();
        } else if (!autoRead && oldAutoRead) {
            autoReadCleared();
        }
        return this;
    }

    /**
     * Is called once {@link #setAutoRead(boolean)} is called with {@code false} and {@link #isAutoRead()} was
     * {@code true} before.
     * <p>自动读从开启变为关闭时的钩子，子类可覆写以执行额外逻辑。</p>
     */
    protected void autoReadCleared() { }

    /** 是否在不可读/不可写时自动关闭通道。 */
    @Override
    public boolean isAutoClose() {
        return autoClose;
    }

    /** 设置是否在不可读/不可写时自动关闭。 */
    @Override
    public ChannelConfig setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    /** 返回写缓冲高水位（字节）。 */
    @Override
    public int getWriteBufferHighWaterMark() {
        return writeBufferWaterMark.high();
    }

    /** 设置写缓冲高水位，须不低于当前低水位。 */
    @Override
    public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        checkPositiveOrZero(writeBufferHighWaterMark, "writeBufferHighWaterMark");
        for (;;) {
            WriteBufferWaterMark waterMark = writeBufferWaterMark;
            if (writeBufferHighWaterMark < waterMark.low()) {
                throw new IllegalArgumentException(
                        "writeBufferHighWaterMark cannot be less than " +
                                "writeBufferLowWaterMark (" + waterMark.low() + "): " +
                                writeBufferHighWaterMark);
            }
            if (WATERMARK_UPDATER.compareAndSet(this, waterMark,
                    new WriteBufferWaterMark(waterMark.low(), writeBufferHighWaterMark, false))) {
                return this;
            }
        }
    }

    /** 返回写缓冲低水位（字节）。 */
    @Override
    public int getWriteBufferLowWaterMark() {
        return writeBufferWaterMark.low();
    }

    /** 设置写缓冲低水位，须不高于当前高水位。 */
    @Override
    public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        checkPositiveOrZero(writeBufferLowWaterMark, "writeBufferLowWaterMark");
        for (;;) {
            WriteBufferWaterMark waterMark = writeBufferWaterMark;
            if (writeBufferLowWaterMark > waterMark.high()) {
                throw new IllegalArgumentException(
                        "writeBufferLowWaterMark cannot be greater than " +
                                "writeBufferHighWaterMark (" + waterMark.high() + "): " +
                                writeBufferLowWaterMark);
            }
            if (WATERMARK_UPDATER.compareAndSet(this, waterMark,
                    new WriteBufferWaterMark(writeBufferLowWaterMark, waterMark.high(), false))) {
                return this;
            }
        }
    }

    /** 同时设置写缓冲高低水位。 */
    @Override
    public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        this.writeBufferWaterMark = checkNotNull(writeBufferWaterMark, "writeBufferWaterMark");
        return this;
    }

    /** 返回写缓冲水位标记对象。 */
    @Override
    public WriteBufferWaterMark getWriteBufferWaterMark() {
        return writeBufferWaterMark;
    }

    /** 返回消息大小估算器。 */
    @Override
    public MessageSizeEstimator getMessageSizeEstimator() {
        return msgSizeEstimator;
    }

    /** 设置消息大小估算器。 */
    @Override
    public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        this.msgSizeEstimator = ObjectUtil.checkNotNull(estimator, "estimator");
        return this;
    }

    private ChannelConfig setPinEventExecutorPerGroup(boolean pinEventExecutor) {
        this.pinEventExecutor = pinEventExecutor;
        return this;
    }

    private boolean getPinEventExecutorPerGroup() {
        return pinEventExecutor;
    }

}
