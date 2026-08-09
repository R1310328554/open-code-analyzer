/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 入站 {@link WebSocketFrame} UTF-8 校验器：验证文本帧及分片消息的字节序列合法。
 * <p>违规时抛出 {@link CorruptedWebSocketFrameException}，可选自动回复 {@link CloseWebSocketFrame} 并关闭连接。
 */
public class Utf8FrameValidator extends ChannelInboundHandlerAdapter {

    private final boolean closeOnProtocolViolation;

    private int fragmentedFramesCount;
    private Utf8Validator utf8Validator;

    /** 默认在协议违规时发送关闭帧并断开连接 */
    public Utf8FrameValidator() {
        this(true);
    }

    /** @param closeOnProtocolViolation 为 true 时校验失败则写关闭帧并 close 通道 */
    public Utf8FrameValidator(boolean closeOnProtocolViolation) {
        this.closeOnProtocolViolation = closeOnProtocolViolation;
    }

    /** 判断是否为控制帧（Close/Ping/Pong），分片序列中允许穿插 */
    // See https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.
    private static boolean isControlFrame(WebSocketFrame frame) {
        return frame instanceof CloseWebSocketFrame ||
                frame instanceof PingWebSocketFrame ||
                frame instanceof PongWebSocketFrame;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;

            try {
                // 处理文本/二进制分片消息的 UTF-8 校验
                if (frame.isFinalFragment()) {
                    // Control frames are allowed between fragments
                    // See https://datatracker.ietf.org/doc/html/rfc6455#section-5.5.
                    if (!isControlFrame(frame)) {

                        // 分片序列的最后一帧
                        fragmentedFramesCount = 0;

                        // 校验本帧及整段消息的 UTF-8 完整性
                        if (frame instanceof TextWebSocketFrame ||
                                (utf8Validator != null && utf8Validator.isChecking())) {
                            // 校验当前载荷字节序列
                            checkUTF8String(frame.content());

                            // 再次确认整段分片消息 UTF-8 完整闭合
                            utf8Validator.finish();
                        }
                    }
                } else {
                    // 非 FIN 帧：继续累积分片
                    if (fragmentedFramesCount == 0) {
                        // 分片首帧：文本则启动校验
                        if (frame instanceof TextWebSocketFrame) {
                            checkUTF8String(frame.content());
                        }
                    } else {
                        // 后续帧：仅当首帧为文本时继续校验
                        if (utf8Validator != null && utf8Validator.isChecking()) {
                            checkUTF8String(frame.content());
                        }
                    }

                    // 分片计数加一
                    fragmentedFramesCount++;
                }
            } catch (CorruptedWebSocketFrameException e) {
                protocolViolation(ctx, frame, e);
            }
        }

        super.channelRead(ctx, msg);
    }

    private void checkUTF8String(ByteBuf buffer) {
        if (utf8Validator == null) {
            utf8Validator = new Utf8Validator();
        }
        utf8Validator.check(buffer);
    }

    /** 释放违规帧，按需写关闭帧并抛出异常终止 pipeline */
    private void protocolViolation(ChannelHandlerContext ctx, WebSocketFrame frame,
                                   CorruptedWebSocketFrameException ex) {
        frame.release();
        if (closeOnProtocolViolation && ctx.channel().isOpen()) {
            WebSocketCloseStatus closeStatus = ex.closeStatus();
            String reasonText = ex.getMessage();
            if (reasonText == null) {
                reasonText = closeStatus.reasonText();
            }

            CloseWebSocketFrame closeFrame = new CloseWebSocketFrame(closeStatus.code(), reasonText);
            ctx.writeAndFlush(closeFrame).addListener(ChannelFutureListener.CLOSE);
        }

        throw ex;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
