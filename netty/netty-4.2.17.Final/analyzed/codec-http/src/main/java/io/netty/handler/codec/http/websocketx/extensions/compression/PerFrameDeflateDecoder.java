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
package io.netty.handler.codec.http.websocketx.extensions.compression;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

/**
 * perframe-deflate 解压器：每个带 RSV1 的数据帧独立解压。
 * <p>每帧解压时追加 {@link DeflateDecoder#FRAME_TAIL}；解压后清除 RSV1 位。
 */
class PerFrameDeflateDecoder extends DeflateDecoder {

    /**
     * Constructor
     *
     * @param noContext 是否禁用上下文接管
     * @param maxAllocation 解压缓冲上限，0 表示不限
     */
    PerFrameDeflateDecoder(boolean noContext, int maxAllocation) {
        super(noContext, WebSocketExtensionFilter.NEVER_SKIP, maxAllocation);
    }

    /**
     * Constructor
     *
     * @param noContext true to disable context takeover.
     * @param extensionDecoderFilter perframe 解码扩展过滤器
     * @param maxAllocation
     *            maximum size of the decompression buffer. Must be &gt;= 0. If zero, maximum size is not limited.
     */
    PerFrameDeflateDecoder(boolean noContext, WebSocketExtensionFilter extensionDecoderFilter, int maxAllocation) {
        super(noContext, extensionDecoderFilter, maxAllocation);
    }

    @Override
    /** 仅处理 Text/Binary/Continuation 且 RSV1 置位且未被 filter 跳过的帧 */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!super.acceptInboundMessage(msg)) {
            return false;
        }

        WebSocketFrame wsFrame = (WebSocketFrame) msg;
        if (extensionDecoderFilter().mustSkip(wsFrame)) {
            return false;
        }

        return (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame ||
                msg instanceof ContinuationWebSocketFrame) &&
               (wsFrame.rsv() & WebSocketExtension.RSV1) > 0;
    }

    @Override
    /** 清除 RSV1 压缩标志位 */
    protected int newRsv(WebSocketFrame msg) {
        return msg.rsv() ^ WebSocketExtension.RSV1;
    }

    @Override
    /** perframe 模式每帧解压均需追加 FRAME_TAIL */
    protected boolean appendFrameTail(WebSocketFrame msg) {
        return true;
    }

}
