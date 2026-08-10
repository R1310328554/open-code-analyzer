/*
 * Copyright 2021 The Netty Project
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

package io.netty.handler.codec.http3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

import java.util.function.BooleanSupplier;

import static io.netty.handler.codec.http.HttpUtil.normalizeAndGetContentLength;
import static io.netty.handler.codec.http3.Http3ErrorCode.H3_MESSAGE_ERROR;
import static io.netty.handler.codec.http3.Http3FrameValidationUtils.frameTypeUnexpected;

/**
 * 请求流读写侧协议校验的静态工具方法：Content-Length 一致性、HTTP/3 禁用头、
 * 客户端 GOAWAY 后写帧限制，以及流提前关闭时的 QPACK {@code streamAbandoned} 通知。
 */
final class Http3RequestStreamValidationUtils {
    /** 当前 HEADERS 帧未携带或未修改 Content-Length。 */
    static final long CONTENT_LENGTH_NOT_MODIFIED = -1;
    /** 帧校验失败，调用方应中止传播并关闭/报错。 */
    static final long INVALID_FRAME_READ = -2;

    private Http3RequestStreamValidationUtils() {
        // No instances
    }

    /**
     * Validate write of the passed {@link Http3RequestStreamFrame} for a client and takes appropriate error handling
     * for invalid frames.
     *
     * @param frame                  to validate.
     * @param promise                for the write.
     * @param ctx                    for the handler.
     * @param goAwayReceivedSupplier for the channel.
     * @param encodeState            for the stream.
     * @return {@code true} if the frame is valid.
     */
    static boolean validateClientWrite(Http3RequestStreamFrame frame, ChannelPromise promise, ChannelHandlerContext ctx,
                                       BooleanSupplier goAwayReceivedSupplier,
                                       Http3RequestStreamCodecState encodeState) {
        if (goAwayReceivedSupplier.getAsBoolean() && !encodeState.started()) {
            // 已收 GOAWAY 且本流尚未写出任何帧：RFC 要求不得再开新请求
            String type = StringUtil.simpleClassName(frame);
            ReferenceCountUtil.release(frame);
            promise.setFailure(new Http3Exception(Http3ErrorCode.H3_FRAME_UNEXPECTED,
                    "Frame of type " + type + " unexpected as we received a GOAWAY already."));
            ctx.close();
            return false;
        }
        if (frame instanceof Http3PushPromiseFrame) {
            // Only supported on the server.
            // See https://tools.ietf.org/html/draft-ietf-quic-http-32#section-4.1
            frameTypeUnexpected(promise, frame);
            return false;
        }
        return true;
    }

    static long validateHeaderFrameRead(Http3HeadersFrame headersFrame, ChannelHandlerContext ctx,
                                        Http3RequestStreamCodecState decodeState) {
        if (headersFrame.headers().contains(HttpHeaderNames.CONNECTION)) {
            headerUnexpected(ctx, headersFrame, "connection header included");
            return INVALID_FRAME_READ;
        }
        CharSequence value = headersFrame.headers().get(HttpHeaderNames.TE);
        if (value != null && !HttpHeaderValues.TRAILERS.equals(value)) {
            headerUnexpected(ctx, headersFrame, "te header field included with invalid value: " + value);
            return INVALID_FRAME_READ;
        }
        if (decodeState.receivedFinalHeaders()) {
            // trailers 帧：解析并规范化 Content-Length（若有）
            long length = normalizeAndGetContentLength(
                    headersFrame.headers().getAll(HttpHeaderNames.CONTENT_LENGTH), false, true);
            if (length != CONTENT_LENGTH_NOT_MODIFIED) {
                headersFrame.headers().setLong(HttpHeaderNames.CONTENT_LENGTH, length);
            }
            return length;
        }
        return CONTENT_LENGTH_NOT_MODIFIED;
    }

    static long validateDataFrameRead(Http3DataFrame dataFrame, ChannelHandlerContext ctx,
                                      long expectedLength, long seenLength, boolean clientHeadRequest) {
        try {
            return verifyContentLength(dataFrame.content().readableBytes(), expectedLength, seenLength, false,
                    clientHeadRequest);
        } catch (Http3Exception e) {
            ReferenceCountUtil.release(dataFrame);
            failStream(ctx, e);
            return INVALID_FRAME_READ;
        }
    }

    static boolean validateOnStreamClosure(ChannelHandlerContext ctx, long expectedLength, long seenLength,
                                           boolean clientHeadRequest) {
        try {
            // 流关闭时以 length=0 做最终长度核对（HEAD 请求允许零体）
            verifyContentLength(0, expectedLength, seenLength, true, clientHeadRequest);
            return true;
        } catch (Http3Exception e) {
            ctx.fireExceptionCaught(e);
            Http3CodecUtils.streamError(ctx, e.errorCode());
            return false;
        }
    }

    static void sendStreamAbandonedIfRequired(ChannelHandlerContext ctx, QpackAttributes qpackAttributes,
                                              QpackDecoder qpackDecoder, Http3RequestStreamCodecState decodeState) {
        if (!qpackAttributes.dynamicTableDisabled() && !decodeState.terminated()) {
            // 流在未收到完整 trailers 前关闭：通知 QPACK 解码器放弃该流上的头部块引用
            final long streamId = ((QuicStreamChannel) ctx.channel()).streamId();
            if (qpackAttributes.decoderStreamAvailable()) {
                qpackDecoder.streamAbandoned(qpackAttributes.decoderStream(), streamId);
            } else {
                qpackAttributes.whenDecoderStreamAvailable(future -> {
                    if (future.isSuccess()) {
                        qpackDecoder.streamAbandoned(qpackAttributes.decoderStream(), streamId);
                    }
                });
            }
        }
    }

    private static void headerUnexpected(ChannelHandlerContext ctx, Http3RequestStreamFrame frame, String msg) {
        // We should close the stream.
        // See https://quicwg.org/base-drafts/draft-ietf-quic-http.html#section-4.1.1
        ReferenceCountUtil.release(frame);
        failStream(ctx, new Http3Exception(H3_MESSAGE_ERROR, msg));
    }

    private static void failStream(ChannelHandlerContext ctx, Http3Exception cause) {
        ctx.fireExceptionCaught(cause);
        Http3CodecUtils.streamError(ctx, cause.errorCode());
    }

    // See https://tools.ietf.org/html/draft-ietf-quic-http-34#section-4.1.3
    private static long verifyContentLength(int length, long expectedLength, long seenLength, boolean end,
                                            boolean clientHeadRequest) throws Http3Exception {
        seenLength += length;
        if (expectedLength != -1 && (seenLength > expectedLength ||
                (!clientHeadRequest && end && seenLength != expectedLength))) {
            throw new Http3Exception(
                    H3_MESSAGE_ERROR, "Expected content-length " + expectedLength +
                    " != " + seenLength + ".");
        }
        return seenLength;
    }
}
