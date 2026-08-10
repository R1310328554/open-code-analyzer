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

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.RecvByteBufAllocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for OIO which reads and writes objects from/to a Socket
 * <p>OIO 消息型 channel 抽象基类：从 Socket 读/写完整消息对象；已废弃。</p>
 *
 * @deprecated use NIO / EPOLL / KQUEUE transport.
 */
@Deprecated
public abstract class AbstractOioMessageChannel extends AbstractOioChannel {

    /** 单次 read 循环内累积的消息，再批量 fireChannelRead */
    private final List<Object> readBuf = new ArrayList<Object>();

    /** 指定父 channel 构造消息型 OIO channel */
    protected AbstractOioMessageChannel(Channel parent) {
        super(parent);
    }

    @Override
    protected void doRead() {
        if (!readPending) {
            // 同一次 read 循环内 readPending 可能被置 false，须再检查
            // We have to check readPending here because the Runnable to read could have been scheduled and later
            // during the same read loop readPending was set to false.
            return;
        }
        // OIO：即使本次未读到数据也清 readPending，以便 EventLoop 再次调度 read
        // In OIO we should set readPending to false even if the read was not successful so we can schedule
        // another read on the event loop if no reads are done.
        readPending = false;

        final ChannelConfig config = config();
        final ChannelPipeline pipeline = pipeline();
        final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        allocHandle.reset(config);

        boolean closed = false;
        Throwable exception = null;
        try {
            do {
                // 执行一次消息读
                // Perform a read.
                int localRead = doReadMessages(readBuf);
                if (localRead == 0) {
                    break;
                }
                if (localRead < 0) {
                    closed = true;
                    break;
                }

                allocHandle.incMessagesRead(localRead);
            } while (allocHandle.continueReading());
        } catch (Throwable t) {
            exception = t;
        }

        boolean readData = false;
        int size = readBuf.size();
        if (size > 0) {
            readData = true;
            for (int i = 0; i < size; i++) {
                readPending = false;
                pipeline.fireChannelRead(readBuf.get(i));
            }
            readBuf.clear();
            allocHandle.readComplete();
            pipeline.fireChannelReadComplete();
        }

        if (exception != null) {
            if (exception instanceof IOException) {
                closed = true;
            }

            pipeline.fireExceptionCaught(exception);
        }

        if (closed) {
            if (isOpen()) {
                unsafe().close(unsafe().voidPromise());
            }
        } else if (readPending || config.isAutoRead() || !readData && isActive()) {
            // 读 0 条消息可能是 SocketTimeout，仍应再次 read
            // Reading 0 bytes could mean there is a SocketTimeout and no data was actually read, so we
            // should execute read() again because no data may have been read.
            read();
        }
    }

    /**
     * Read messages into the given array and return the amount which was read.
     * <p>从底层读消息到列表；返回读到的条数，&lt;0 表示 EOF。</p>
     */
    protected abstract int doReadMessages(List<Object> msgs) throws Exception;
}
