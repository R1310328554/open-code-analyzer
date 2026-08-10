/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.sctp;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;

import java.io.IOException;
import java.util.Map;

import static io.netty.channel.ChannelOption.SO_RCVBUF;
import static io.netty.channel.ChannelOption.SO_SNDBUF;
import static io.netty.channel.sctp.SctpChannelOption.SCTP_INIT_MAXSTREAMS;
import static io.netty.channel.sctp.SctpChannelOption.SCTP_NODELAY;

/**
 * The default {@link SctpChannelConfig} implementation for SCTP.
 * <p>SCTP 客户端通道默认配置：通过 JDK {@link com.sun.nio.sctp.SctpChannel}  读写 {@code SCTP_NODELAY}、缓冲区与 INIT 流数量； 在支持平台上默认启用 SCTP_NODELAY。</p>
 */
public class DefaultSctpChannelConfig extends DefaultChannelConfig implements SctpChannelConfig {

    /** 底层 JDK SCTP 通道，选项读写均委托给它 */
    private final SctpChannel javaChannel;

    /**
     * @param channel Netty {@link io.netty.channel.sctp.SctpChannel} 包装
     * @param javaChannel JDK {@link SctpChannel}
     * <p>若平台允许则尝试默认开启 SCTP_NODELAY。</p>
     */
    public DefaultSctpChannelConfig(io.netty.channel.sctp.SctpChannel channel, SctpChannel javaChannel) {
        super(channel);
        this.javaChannel = ObjectUtil.checkNotNull(javaChannel, "javaChannel");

        // 平台允许时默认开启 SCTP_NODELAY（注释沿用 Netty 历史命名）
        if (PlatformDependent.canEnableTcpNoDelayByDefault()) {
            try {
                setSctpNoDelay(true);
            } catch (Exception e) {
                // 忽略：部分平台不支持该选项
            }
        }
    }

    @Override
    /** 返回 SCTP 相关选项与父类选项的合并映射 */
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(
                super.getOptions(),
                SO_RCVBUF, SO_SNDBUF, SCTP_NODELAY, SCTP_INIT_MAXSTREAMS);
    }

    @SuppressWarnings("unchecked")
    @Override
    /** 按 {@link ChannelOption} 读取 SCTP/通用配置 */
    public <T> T getOption(ChannelOption<T> option) {
        if (option == SO_RCVBUF) {
            return (T) Integer.valueOf(getReceiveBufferSize());
        }
        if (option == SO_SNDBUF) {
            return (T) Integer.valueOf(getSendBufferSize());
        }
        if (option == SCTP_NODELAY) {
            return (T) Boolean.valueOf(isSctpNoDelay());
        }
        if (option == SCTP_INIT_MAXSTREAMS) {
            return (T) getInitMaxStreams();
        }
        return super.getOption(option);
    }

    @Override
    /** 设置选项；SCTP 专有项在此处理，其余委托父类 */
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == SO_RCVBUF) {
            setReceiveBufferSize((Integer) value);
        } else if (option == SO_SNDBUF) {
            setSendBufferSize((Integer) value);
        } else if (option == SCTP_NODELAY) {
            setSctpNoDelay((Boolean) value);
        } else if (option == SCTP_INIT_MAXSTREAMS) {
            setInitMaxStreams((SctpStandardSocketOptions.InitMaxStreams) value);
        } else {
            return super.setOption(option, value);
        }

        return true;
    }

    @Override
    /** 读取 {@code SCTP_NODELAY}（禁用 Nagle 类延迟） */
    public boolean isSctpNoDelay() {
        try {
            return javaChannel.getOption(SctpStandardSocketOptions.SCTP_NODELAY);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    /** 设置 {@code SCTP_NODELAY} 并返回 {@code this} */
    public SctpChannelConfig setSctpNoDelay(boolean sctpNoDelay) {
        try {
            javaChannel.setOption(SctpStandardSocketOptions.SCTP_NODELAY, sctpNoDelay);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
        return this;
    }

    @Override
    /** 读取 {@code SO_SNDBUF} 发送缓冲区大小 */
    public int getSendBufferSize() {
        try {
            return javaChannel.getOption(SctpStandardSocketOptions.SO_SNDBUF);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    /** 设置 {@code SO_SNDBUF} */
    public SctpChannelConfig setSendBufferSize(int sendBufferSize) {
        try {
            javaChannel.setOption(SctpStandardSocketOptions.SO_SNDBUF, sendBufferSize);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
        return this;
    }

    @Override
    /** 读取 {@code SO_RCVBUF} 接收缓冲区大小 */
    public int getReceiveBufferSize() {
        try {
            return javaChannel.getOption(SctpStandardSocketOptions.SO_RCVBUF);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    /** 设置 {@code SO_RCVBUF} */
    public SctpChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        try {
            javaChannel.setOption(SctpStandardSocketOptions.SO_RCVBUF, receiveBufferSize);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
        return this;
    }

    @Override
    /** 读取 INIT 阶段协商的最大入/出站流数 */
    public SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams() {
        try {
            return javaChannel.getOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    @Override
    /** 设置 {@code SCTP_INIT_MAXSTREAMS}（仅未建立关联前有效） */
    public SctpChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams initMaxStreams) {
        try {
            javaChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS, initMaxStreams);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
        return this;
    }

    @Override
    public SctpChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    @Deprecated
    public SctpChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public SctpChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public SctpChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public SctpChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public SctpChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public SctpChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public SctpChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public SctpChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public SctpChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public SctpChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
