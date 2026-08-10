/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.LeakPresenceDetector;
import io.netty.util.internal.UnstableApi;

import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/**
 * Encoder for SMTP requests.
 * <p>将 {@link SmtpRequest} 编码为 {@code COMMAND [params]\r\n} 行，将 {@link SmtpContent} 序列
 * 编码为 DATA 正文块；{@link LastSmtpContent} 之后自动追加 {@code .\r\n} 结束符。
 * 内部 {@code contentExpected} 状态机保证 DATA 与正文帧的严格顺序，仅 {@code RSET} 可在正文阶段打断。</p>
 */
@UnstableApi
public final class SmtpRequestEncoder extends MessageToMessageEncoder<Object> {
    /** 大端 short 形式的 {@code \r\n}，用于命令行结尾。 */
    private static final int CRLF_SHORT = ('\r' << 8) | '\n';
    private static final byte SP = ' ';
    /** 共享只读 {@code .\r\n} 缓冲区，最后一帧正文后 retainedDuplicate 输出。 */
    private static final ByteBuf DOT_CRLF_BUFFER = LeakPresenceDetector.staticInitializer(() ->
            Unpooled.unreleasableBuffer(Unpooled.directBuffer(3)
                    .writeByte('.').writeByte('\r').writeByte('\n')).asReadOnly());

    /** 为 true 表示已发送 DATA，下一出站消息必须是 {@link SmtpContent}（或 RSET 取消）。 */
    private boolean contentExpected;

    public SmtpRequestEncoder() {
        super(Object.class);
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return msg instanceof SmtpRequest || msg instanceof SmtpContent;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof SmtpRequest) {
            final SmtpRequest req = (SmtpRequest) msg;
            if (contentExpected) {
                // DATA 正文传输中仅允许 RSET 打断，其它命令视为协议错误
                if (req.command().equals(SmtpCommand.RSET)) {
                    contentExpected = false;
                } else {
                    throw new IllegalStateException("SmtpContent expected");
                }
            }
            boolean release = true;
            final ByteBuf buffer = ctx.alloc().buffer();
            try {
                req.command().encode(buffer);
                boolean notEmpty = req.command() != SmtpCommand.EMPTY;
                writeParameters(req.parameters(), buffer, notEmpty);
                ByteBufUtil.writeShortBE(buffer, CRLF_SHORT);
                out.add(buffer);
                release = false;
                if (req.command().isContentExpected()) {
                    contentExpected = true;
                }
            } finally {
                if (release) {
                    buffer.release();
                }
            }
        }

        if (msg instanceof SmtpContent) {
            if (!contentExpected) {
                throw new IllegalStateException("No SmtpContent expected");
            }
            final ByteBuf content = ((SmtpContent) msg).content();
            out.add(content.retain());
            if (msg instanceof LastSmtpContent) {
                // RFC 2821：单独一行的点加 CRLF 标记 DATA 结束
                out.add(DOT_CRLF_BUFFER.retainedDuplicate());
                contentExpected = false;
            }
        }
    }

    /**
     * 参数以空格分隔写入；{@code EMPTY} 命令无命令名时首个参数前不加空格。
     */
    private static void writeParameters(List<CharSequence> parameters, ByteBuf out, boolean commandNotEmpty) {
        if (parameters.isEmpty()) {
            return;
        }
        if (commandNotEmpty) {
            out.writeByte(SP);
        }
        if (parameters instanceof RandomAccess) {
            final int sizeMinusOne = parameters.size() - 1;
            for (int i = 0; i < sizeMinusOne; i++) {
                ByteBufUtil.writeAscii(out, parameters.get(i));
                out.writeByte(SP);
            }
            ByteBufUtil.writeAscii(out, parameters.get(sizeMinusOne));
        } else {
            final Iterator<CharSequence> params = parameters.iterator();
            for (;;) {
                ByteBufUtil.writeAscii(out, params.next());
                if (params.hasNext()) {
                    out.writeByte(SP);
                } else {
                    break;
                }
            }
        }
    }
}
