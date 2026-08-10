/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.pcap;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 将 PCAP 数据包写入 {@link OutputStream} 的底层写入器，由 {@link PcapWriteHandler} 持有并调用。
 *
 * <p>负责 PCAP 全局头（非共享流时）、逐包记录头与帧体的序列化；共享 {@link OutputStream} 时用该流对象作互斥锁。</p>
 */
final class PcapWriter implements Closeable {

    /**
     * Logger
     *
     * <p>调试日志记录器。</p>
     */
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PcapWriter.class);

    /** 关联的 PCAP 写入处理器，提供状态与配置。 */
    private final PcapWriteHandler pcapWriteHandler;

    /**
     * Reference declared so that we can use this as mutex in clean way.
     *
     * <p>底层输出流；共享模式下作为同步锁对象。</p>
     */
    private final OutputStream outputStream;

    /**
     * This uses {@link OutputStream} for writing Pcap data.
     *
     * @throws IOException If {@link OutputStream#write(byte[])} throws an exception
     *
     * <p>构造时若非共享流且需写全局头，则先写入 PCAP 文件头。</p>
     */
    PcapWriter(PcapWriteHandler pcapWriteHandler) throws IOException {
        this.pcapWriteHandler = pcapWriteHandler;
        outputStream = pcapWriteHandler.outputStream();

        // If OutputStream is not shared then we have to write Global Header.
        // 非共享 OutputStream 时必须先写入 PCAP 全局文件头
        if (pcapWriteHandler.writePcapGlobalHeader() && !pcapWriteHandler.sharedOutputStream()) {
            PcapHeaders.writeGlobalHeader(pcapWriteHandler.outputStream());
        }
    }

    /**
     * Write Packet in Pcap OutputStream.
     *
     * @param packetHeaderBuf Packer Header {@link ByteBuf}
     * @param packet          Packet
     * @throws IOException If {@link OutputStream#write(byte[])} throws an exception
     *
     * <p>按当前毫秒时间戳写 PCAP 记录头，再写以太网/IP/TCP(UDP) 帧体；共享流时加锁写入。</p>
     */
    void writePacket(ByteBuf packetHeaderBuf, ByteBuf packet) throws IOException {
        if (pcapWriteHandler.state() == State.CLOSED) {
            logger.debug("Pcap Write attempted on closed PcapWriter");
        }

        long timestamp = System.currentTimeMillis();

        // 秒 + 微秒组成 PCAP 包时间戳
        PcapHeaders.writePacketHeader(
                packetHeaderBuf,
                (int) (timestamp / 1000L),
                (int) (timestamp % 1000L * 1000L),
                packet.readableBytes(),
                packet.readableBytes()
        );

        if (pcapWriteHandler.sharedOutputStream()) {
            synchronized (outputStream) {
                packetHeaderBuf.readBytes(outputStream, packetHeaderBuf.readableBytes());
                packet.readBytes(outputStream, packet.readableBytes());
            }
        } else {
            packetHeaderBuf.readBytes(outputStream, packetHeaderBuf.readableBytes());
            packet.readBytes(outputStream, packet.readableBytes());
        }
    }

    @Override
    public String toString() {
        return "PcapWriter{" +
                "outputStream=" + outputStream +
                '}';
    }

    /**
     * 关闭写入器：共享流仅 flush，独占流 flush 并 close，并标记 handler 为 {@link State#CLOSED}。
     */
    @Override
    public void close() throws IOException {
        if (pcapWriteHandler.state() == State.CLOSED) {
            logger.debug("PcapWriter is already closed");
        } else {
            if (pcapWriteHandler.sharedOutputStream()) {
                synchronized (outputStream) {
                    outputStream.flush();
                }
            } else {
                outputStream.flush();
                outputStream.close();
            }
            pcapWriteHandler.markClosed();
            logger.debug("PcapWriter is now closed");
        }
    }
}
