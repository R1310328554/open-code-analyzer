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
package io.netty.handler.codec.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v4.Socks4ServerDecoder.State;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks4CommandRequest} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove this decoder later.  On failed decode, this decoder will discard the
 * received data, so that other handler closes the connection later.
 *
 * <p>SOCKS4 服务端解码器：多阶段状态机解析客户端命令请求。
 * 先读固定头（VN/CD/PORT/IP），再读 NUL 结尾 USERID；若 DSTIP 为 0.0.0.x 占位则进入 SOCKS4a
 * 分支读取域名。成功后透传后续隧道字节；失败时产出带 {@link DecoderResult} 的占位请求。</p>
 */
public class Socks4ServerDecoder extends ReplayingDecoder<State> {

    /** SOCKS4 可变长字符串字段（USERID、域名）最大长度。 */
    private static final int MAX_FIELD_LENGTH = 255;

    /** 解码阶段：固定头 → USERID → 可选域名 → 成功透传 / 失败丢弃。 */
    @UnstableApi
    public enum State {
        START,
        READ_USERID,
        READ_DOMAIN,
        SUCCESS,
        FAILURE
    }

    private Socks4CommandType type;
    private String dstAddr;
    private int dstPort;
    private String userId;

    public Socks4ServerDecoder() {
        super(State.START);
        setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (state()) {
            case START: {
                final int version = in.readUnsignedByte();
                if (version != SocksVersion.SOCKS4a.byteValue()) {
                    throw new DecoderException("unsupported protocol version: " + version);
                }

                type = Socks4CommandType.valueOf(in.readByte());
                dstPort = ByteBufUtil.readUnsignedShortBE(in);
                dstAddr = NetUtil.intToIpAddress(ByteBufUtil.readIntBE(in));
                checkpoint(State.READ_USERID);
            }
            case READ_USERID: {
                userId = readString("userid", in);
                checkpoint(State.READ_DOMAIN);
            }
            case READ_DOMAIN: {
                // Check for Socks4a protocol marker 0.0.0.x
                // 0.0.0.0 为 BIND 合法地址；0.0.0.1~254 表示 USERID 后还有域名
                if (!"0.0.0.0".equals(dstAddr) && dstAddr.startsWith("0.0.0.")) {
                    dstAddr = readString("dstAddr", in);
                }
                out.add(new DefaultSocks4CommandRequest(type, dstAddr, dstPort, userId));
                checkpoint(State.SUCCESS);
            }
            case SUCCESS: {
                int readableBytes = actualReadableBytes();
                if (readableBytes > 0) {
                    out.add(in.readRetainedSlice(readableBytes));
                }
                break;
            }
            case FAILURE: {
                in.skipBytes(actualReadableBytes());
                break;
            }
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    /** 用已解析的部分字段构造失败占位请求，便于上层统一处理 {@link DecoderResult}。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        Socks4CommandRequest m = new DefaultSocks4CommandRequest(
                type != null? type : Socks4CommandType.CONNECT,
                dstAddr != null? dstAddr : "",
                dstPort != 0? dstPort : 65535,
                userId != null? userId : "");

        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);

        checkpoint(State.FAILURE);
    }

    /**
     * Reads a variable-length NUL-terminated string as defined in SOCKS4.
     *
     * <p>读取 SOCKS4 NUL 终止字符串；超过 {@link #MAX_FIELD_LENGTH} 未遇 NUL 则抛错。</p>
     */
    private static String readString(String fieldName, ByteBuf in) {
        int length = in.bytesBefore(MAX_FIELD_LENGTH + 1, (byte) 0);
        if (length < 0) {
            throw new DecoderException("field '" + fieldName + "' longer than " + MAX_FIELD_LENGTH + " chars");
        }

        String value = in.readSlice(length).toString(CharsetUtil.US_ASCII);
        in.skipBytes(1); // Skip the NUL.

        return value;
    }
}
