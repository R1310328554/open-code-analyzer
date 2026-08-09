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
package io.netty.handler.codec.quic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.ObjectUtil;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 在多个 {@link Channel} 通过
 * <a href="https://man7.org/linux/man-pages/man7/socket.7.html">SO_REUSEPORT</a>
 * 绑定同一 {@link java.net.InetSocketAddress} 时，用于 QUIC 报文分发的 {@link io.netty.channel.ChannelHandler}。
 * <p>
 * 将内部 {@link ChannelHandlerContext} 列表索引编码进目的连接 ID，
 * 收到报文后解码索引并转发到对应 codec，保证连接 ID 映射正确。
 * 子类可覆盖 {@link #decodeIndex(ByteBuf)} 与 {@link #newIdGenerator(int)} 自定义编解码策略。
 * <p>
 * 同一地址上所有复用端口的 {@link Channel} 必须共享同一 {@link QuicCodecDispatcher} 实例。
 * <p>
 * 也可在 eBPF 程序中实现路由，配合自定义 {@link QuicConnectionIdGenerator} 生成可路由的连接 ID。
 */
public abstract class QuicCodecDispatcher extends ChannelInboundHandlerAdapter {
    // RFC 9000 规定本地连接 ID 最大长度为 20
    // 参见 https://datatracker.ietf.org/doc/html/rfc9000#section-17.2
    private static final int MAX_LOCAL_CONNECTION_ID_LENGTH = 20;

    // 仅在启动/拆除阶段修改列表，使用 CopyOnWriteArrayList 保证读路径无锁
    private final List<ChannelHandlerContextDispatcher> contextList = new CopyOnWriteArrayList<>();
    private final int localConnectionIdLength;

    /** 使用默认连接 ID 长度（20）创建分发器。 */
    protected QuicCodecDispatcher() {
        this(MAX_LOCAL_CONNECTION_ID_LENGTH);
    }

    /**
     * 指定本地连接 ID 长度创建分发器，须在 10 到 20 之间（预留 2 字节编码索引）。
     *
     * @param localConnectionIdLength   the local connection id length. This must be between 10 and 20.
     */
    protected QuicCodecDispatcher(int localConnectionIdLength) {
        // 最小长度 10：2 字节存索引，其余字节保留随机性
        this.localConnectionIdLength = ObjectUtil.checkInRange(localConnectionIdLength,
                10, MAX_LOCAL_CONNECTION_ID_LENGTH, "localConnectionIdLength");
    }

    @Override
    public final boolean isSharable() {
        return true;
    }

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        ChannelHandlerContextDispatcher ctxDispatcher = new ChannelHandlerContextDispatcher(ctx);
        contextList.add(ctxDispatcher);
        int idx = contextList.indexOf(ctxDispatcher);
        try {
            QuicConnectionIdGenerator idGenerator = newIdGenerator((short) idx);
            initChannel(ctx.channel(), localConnectionIdLength, idGenerator);
        } catch (Exception e) {
            // 异常时将槽位置 null 但不删除，以保持索引稳定
            contextList.set(idx, null);
            throw e;
        }
    }

    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        for (int idx = 0; idx < contextList.size(); idx++) {
            ChannelHandlerContextDispatcher ctxDispatcher = contextList.get(idx);
            if (ctxDispatcher != null && ctxDispatcher.ctx.equals(ctx)) {
                // 置 null 以便 GC 回收已移除的 ChannelHandlerContext
                contextList.set(idx, null);
                break;
            }
        }
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf connectionId = getDestinationConnectionId(packet.content(), localConnectionIdLength);
        if (connectionId != null) {
            int idx = decodeIndex(connectionId);
            if (contextList.size() > idx) {
                ChannelHandlerContextDispatcher selectedCtx = contextList.get(idx);
                if (selectedCtx != null) {
                    selectedCtx.fireChannelRead(msg);
                    return;
                }
            }
        }
        // 无法分发到具体 codec 时向上游传递，由 Quic*Codec 自行处理
        ctx.fireChannelRead(msg);
    }

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) {
        // 遍历所有 dispatcher，必要时触发 fireChannelReadComplete；
        // CopyOnWriteArrayList 支持 RandomAccess，用索引 for 循环减少迭代器分配
        boolean dispatchForOwnContextAlready = false;
        for (int i = 0; i < contextList.size(); i++) {
            ChannelHandlerContextDispatcher ctxDispatcher = contextList.get(i);
            if (ctxDispatcher != null) {
                boolean fired = ctxDispatcher.fireChannelReadCompleteIfNeeded();
                if (fired && !dispatchForOwnContextAlready) {
                    // 记录是否已向当前 ctx 分发，以便末尾补发 readComplete
                    dispatchForOwnContextAlready = ctx.equals(ctxDispatcher.ctx);
                }
            }
        }
        if (!dispatchForOwnContextAlready) {
            ctx.fireChannelReadComplete();
        }
    }

    /**
     * 初始化 {@link Channel} pipeline，使用给定连接 ID 长度与 {@link QuicConnectionIdGenerator}
     * 通过 {@link QuicCodecBuilder} 子类构建 QUIC 编解码器并加入所需 handler。
     *
     * @param channel                   the {@link Channel} to init.
     * @param localConnectionIdLength   the local connection id length that must be used with the
     *                                  {@link QuicCodecBuilder}.
     * @param idGenerator               the {@link QuicConnectionIdGenerator} that must be used with the
     *                                  {@link QuicCodecBuilder}.
     * @throws Exception                thrown on error.
     */
    protected abstract void initChannel(Channel channel, int localConnectionIdLength,
                                        QuicConnectionIdGenerator idGenerator) throws Exception;

    /**
     * 从目的连接 ID 解码先前嵌入的 codec 索引；失败返回 {@code -1}。
     * <p>
     * 子类可覆盖；同时应覆盖 {@link #newIdGenerator(int)} 以保持编解码一致。
     *
     * @param connectionId  the destination connection id of the {@code QUIC} connection.
     * @return              the index or -1.
     */
    protected int decodeIndex(ByteBuf connectionId) {
        return decodeIdx(connectionId);
    }

    /**
     * 从 UDP 载荷解析目的连接 ID；无法解析时返回 {@code null}。
     *
     * @param buffer    the buffer
     * @return          the id or {@code null}.
     */
    /** 包内/测试可见：从报文缓冲区切片目的连接 ID。 */
    @Nullable
    static ByteBuf getDestinationConnectionId(ByteBuf buffer, int localConnectionIdLength) throws QuicException {
        if (buffer.readableBytes() > Byte.BYTES) {
            int offset = buffer.readerIndex();
            boolean shortHeader = hasShortHeader(buffer);
            offset += Byte.BYTES;
            // 仅处理短头报文（握手完成后客户端使用的连接 ID）
            if (shortHeader) {
                // 参见 RFC 9000 §17.3 短头 1-RTT 报文格式
                // 1-RTT Packet {
                //  Header Form (1) = 0,
                //  Fixed Bit (1) = 1,
                //  Spin Bit (1),
                //  Reserved Bits (2),
                //  Key Phase (1),
                //  Packet Number Length (2),
                //  Destination Connection ID (0..160),
                //  Packet Number (8..32),
                //  Packet Payload (8..),
                //}
                return QuicHeaderParser.sliceCid(buffer, offset, localConnectionIdLength);
            }
        }
        return null;
    }

    /** 包内/测试可见：判断是否为 QUIC 短头报文。 */
        return QuicHeaderParser.hasShortHeader(buffer.getByte(buffer.readerIndex()));
    }

    /** 包内/测试可见：从连接 ID 前 2 字节解码无符号 short 索引。 */
        if (connectionId.readableBytes() >= 2) {
            return connectionId.getUnsignedShort(connectionId.readerIndex());
        }
        return -1;
    }

    /** 包内/测试可见：在连接 ID 前 prepend 2 字节索引。 */
        // 分配新缓冲区并在首部写入索引
        ByteBuffer b = ByteBuffer.allocate(buffer.capacity() + Short.BYTES);
        // 以无符号 short 编码索引
        b.putShort((short) idx).put(buffer).flip();
        return b;
    }

    /**
     * 返回在生成的每个连接 ID 中嵌入指定索引的 {@link QuicConnectionIdGenerator}。
     * <p>
     * 子类可覆盖；解码侧应同步覆盖 {@link #decodeIndex(ByteBuf)}。
     *
     * @param idx       the index to encode into each id.
     * @return          the {@link QuicConnectionIdGenerator}.
     */
    protected QuicConnectionIdGenerator newIdGenerator(int idx) {
        return new IndexAwareQuicConnectionIdGenerator(idx, SecureRandomQuicConnectionIdGenerator.INSTANCE);
    }

    private static final class IndexAwareQuicConnectionIdGenerator implements QuicConnectionIdGenerator {
        private final int idx;
        private final QuicConnectionIdGenerator idGenerator;

        IndexAwareQuicConnectionIdGenerator(int idx, QuicConnectionIdGenerator idGenerator) {
            this.idx = idx;
            this.idGenerator = idGenerator;
        }

        @Override
        public ByteBuffer newId(int length) {
            if (length > Short.BYTES) {
                return encodeIdx(idGenerator.newId(length - Short.BYTES), idx);
            }
            return idGenerator.newId(length);
        }

        @Override
        public ByteBuffer newId(ByteBuffer input, int length) {
            if (length > Short.BYTES) {
                return encodeIdx(idGenerator.newId(input, length - Short.BYTES), idx);
            }
            return idGenerator.newId(input, length);
        }

        @Override
        public ByteBuffer newId(ByteBuffer scid, ByteBuffer dcid, int length) {
            if (length > Short.BYTES) {
                return encodeIdx(idGenerator.newId(scid, dcid, length - Short.BYTES), idx);
            }
            return idGenerator.newId(scid, dcid, length);
        }

        @Override
        public int maxConnectionIdLength() {
            return idGenerator.maxConnectionIdLength();
        }

        @Override
        public boolean isIdempotent() {
            // 嵌入索引后相同输入可能产生不同 ID，故非幂等
            return false;
        }
    }

    private static final class ChannelHandlerContextDispatcher extends AtomicBoolean {

        private final ChannelHandlerContext ctx;

        ChannelHandlerContextDispatcher(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        void fireChannelRead(Object msg) {
            ctx.fireChannelRead(msg);
            set(true);
        }

        boolean fireChannelReadCompleteIfNeeded() {
            if (getAndSet(false)) {
                // 此前已 fireChannelRead，补发 readComplete 通知读循环可能结束
                ctx.fireChannelReadComplete();
                return true;
            }
            return false;
        }
    }
}
