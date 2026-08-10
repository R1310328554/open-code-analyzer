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
package io.netty.channel.oio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FileRegion;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.util.LeakPresenceDetector;
import io.netty.util.internal.StringUtil;

import java.io.IOException;

/**
 * Abstract base class for OIO which reads and writes bytes from/to a Socket
 * <p>OIO（阻塞 I/O）字节 channel 抽象基类；已废弃，请使用 NIO/EPOLL/KQUEUE。</p>
 *
 * @deprecated use NIO / EPOLL / KQUEUE transport.
 */
public abstract class AbstractOioByteChannel extends AbstractOioChannel {

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);
    private static final String EXPECTED_TYPES =
            " (expected: " + StringUtil.simpleClassName(ByteBuf.class) + ", " +
            StringUtil.simpleClassName(FileRegion.class) + ')';

    /**
     * @see AbstractOioByteChannel#AbstractOioByteChannel(Channel)
     * <p>指定父 channel 构造 OIO 字节 channel。</p>
     */
    protected AbstractOioByteChannel(Channel parent) {
        super(parent);
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    /**
     * Determine if the input side of this channel is shutdown.
     * <p>输入侧是否已 shutdown。</p>
     * @return {@code true} if the input side of this channel is shutdown.
     */
    protected abstract boolean isInputShutdown();

    /**
     * Shutdown the input side of this channel.
     * <p>关闭输入侧（半关闭读）。</p>
     * @return A channel future that will complete when the shutdown is complete.
     */
    protected abstract ChannelFuture shutdownInput();

    private void closeOnRead(ChannelPipeline pipeline) {
        if (isOpen()) {
            if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
                shutdownInput();
                pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
            } else {
                unsafe().close(unsafe().voidPromise());
            }
            pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
        }
    }

    private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close,
            RecvByteBufAllocator.Handle allocHandle) {
        if (byteBuf != null) {
            if (byteBuf.isReadable()) {
                readPending = false;
                pipeline.fireChannelRead(byteBuf);
            } else {
                byteBuf.release();
            }
        }
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        pipeline.fireExceptionCaught(cause);

        // OOM 等致命错误时关闭读
        // See https://github.com/netty/netty/issues/10434
        if (close ||
                cause instanceof OutOfMemoryError ||
                cause instanceof LeakPresenceDetector.AllocationProhibitedException ||
                cause instanceof IOException) {
            closeOnRead(pipeline);
        }
    }

    @Override
    protected void doRead() {
        final ChannelConfig config = config();
        if (isInputShutdown() || !readPending) {
            // 同一次 read 循环内 readPending 可能被置 false，须再检查
            return;
        }
        // OIO：即使本次未读到数据也清 readPending，以便 EventLoop 再次调度 read
        readPending = false;

        final ChannelPipeline pipeline = pipeline();
        final ByteBufAllocator allocator = config.getAllocator();
        final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        allocHandle.reset(config);

        ByteBuf byteBuf = null;
        boolean close = false;
        boolean readData = false;
        try {
            byteBuf = allocHandle.allocate(allocator);
            do {
                allocHandle.lastBytesRead(doReadBytes(byteBuf));
                if (allocHandle.lastBytesRead() <= 0) {
                    if (!byteBuf.isReadable()) { // nothing was read. release the buffer.
                        byteBuf.release();
                        byteBuf = null;
                        close = allocHandle.lastBytesRead() < 0;
                        if (close) {
                            // There is nothing left to read as we received an EOF.
                            readPending = false;
                        }
                    }
                    break;
                } else {
                    readData = true;
                }

                final int available = available();
                if (available <= 0) {
                    break;
                }

                // OIO：将连续读到的数据合并进同一 ByteBuf 再向上传播
                if (!byteBuf.isWritable()) {
                    final int capacity = byteBuf.capacity();
                    final int maxCapacity = byteBuf.maxCapacity();
                    if (capacity == maxCapacity) {
                        allocHandle.incMessagesRead(1);
                        readPending = false;
                        pipeline.fireChannelRead(byteBuf);
                        byteBuf = allocHandle.allocate(allocator);
                    } else {
                        final int writerIndex = byteBuf.writerIndex();
                        if (writerIndex + available > maxCapacity) {
                            byteBuf.capacity(maxCapacity);
                        } else {
                            byteBuf.ensureWritable(available);
                        }
                    }
                }
            } while (allocHandle.continueReading());

            if (byteBuf != null) {
                // It is possible we allocated a buffer because the previous one was not writable, but then didn't use
                // it because allocHandle.continueReading() returned false.
                if (byteBuf.isReadable()) {
                    readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                } else {
                    byteBuf.release();
                }
                byteBuf = null;
            }

            if (readData) {
                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();
            }

            if (close) {
                closeOnRead(pipeline);
            }
        } catch (Throwable t) {
            handleReadException(pipeline, byteBuf, t, close, allocHandle);
        } finally {
            if (readPending || config.isAutoRead() || !readData && isActive()) {
                // 读 0 字节可能是 SocketTimeout，仍应再次 read
                read();
            }
        }
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        for (;;) {
            Object msg = in.current();
            if (msg == null) {
                // nothing left to write
                break;
            }
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                int readableBytes = buf.readableBytes();
                while (readableBytes > 0) {
                    doWriteBytes(buf);
                    int newReadableBytes = buf.readableBytes();
                    in.progress(readableBytes - newReadableBytes);
                    readableBytes = newReadableBytes;
                }
                in.remove();
            } else if (msg instanceof FileRegion) {
                FileRegion region = (FileRegion) msg;
                long transferred = region.transferred();
                doWriteFileRegion(region);
                in.progress(region.transferred() - transferred);
                in.remove();
            } else {
                in.remove(new UnsupportedOperationException(
                        "unsupported message type: " + StringUtil.simpleClassName(msg)));
            }
        }
    }

    @Override
    protected final Object filterOutboundMessage(Object msg) throws Exception {
        if (msg instanceof ByteBuf || msg instanceof FileRegion) {
            return msg;
        }

        throw new UnsupportedOperationException(
                "unsupported message type: " + StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
    }

    /**
     * Return the number of bytes ready to read from the underlying Socket.
     * <p>底层 Socket 可读字节数（{@code available()}）。</p>
     */
    protected abstract int available();

    /**
     * Read bytes from the underlying Socket.
     * <p>从底层 Socket 读到 {@link ByteBuf}。</p>
     *
     * @param buf           the {@link ByteBuf} into which the read bytes will be written
     * @return amount       the number of bytes read. This may return a negative amount if the underlying
     *                      Socket was closed
     * @throws Exception    is thrown if an error occurred
     */
    protected abstract int doReadBytes(ByteBuf buf) throws Exception;

    /**
     * Write the data which is hold by the {@link ByteBuf} to the underlying Socket.
     * <p>将 {@link ByteBuf} 写入底层 Socket。</p>
     *
     * @param buf           the {@link ByteBuf} which holds the data to transfer
     * @throws Exception    is thrown if an error occurred
     */
    protected abstract void doWriteBytes(ByteBuf buf) throws Exception;

    /**
     * Write the data which is hold by the {@link FileRegion} to the underlying Socket.
     * <p>将 {@link FileRegion} 写入底层 Socket。</p>
     *
     * @param region        the {@link FileRegion} which holds the data to transfer
     * @throws Exception    is thrown if an error occurred
     */
    protected abstract void doWriteFileRegion(FileRegion region) throws Exception;
}
