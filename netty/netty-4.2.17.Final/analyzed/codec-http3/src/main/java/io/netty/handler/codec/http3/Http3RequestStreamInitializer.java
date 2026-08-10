/*
 * Copyright 2020 The Netty Project
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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.util.internal.StringUtil;

/**
 * Abstract base class that users can extend to init HTTP/3 request-streams. This initializer
 * will automatically add HTTP/3 codecs etc to the {@link ChannelPipeline} as well.
 * <p>客户端主动打开请求流时使用：从父连接 pipeline 取得 {@link Http3ConnectionHandler}，
 * 依次挂载编解码器、出入站帧顺序校验与协议校验 handler，再调用 {@link #initRequestStream} 添加业务逻辑。
 */
public abstract class Http3RequestStreamInitializer extends ChannelInitializer<QuicStreamChannel> {

    @Override
    protected final void initChannel(QuicStreamChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        Http3ConnectionHandler connectionHandler = ch.parent().pipeline().get(Http3ConnectionHandler.class);
        if (connectionHandler == null) {
            throw new IllegalStateException("Couldn't obtain the " +
                    StringUtil.simpleClassName(Http3ConnectionHandler.class) + " of the parent Channel");
        }

        Http3RequestStreamEncodeStateValidator encodeStateValidator = new Http3RequestStreamEncodeStateValidator();
        Http3RequestStreamDecodeStateValidator decodeStateValidator = new Http3RequestStreamDecodeStateValidator();

        // 编解码器：将 ByteBuf 与 Http3RequestStreamFrame 互转
        pipeline.addLast(connectionHandler.newCodec(encodeStateValidator, decodeStateValidator));
        // 出入站帧顺序与帧类型校验（HEADERS → DATA → trailers 状态机）
        pipeline.addLast(encodeStateValidator);
        pipeline.addLast(decodeStateValidator);
        pipeline.addLast(connectionHandler.newRequestStreamValidationHandler(ch, encodeStateValidator,
                decodeStateValidator));
        initRequestStream(ch);
    }

    /**
     * Init the {@link QuicStreamChannel} to handle {@link Http3RequestStreamFrame}s. At the point of calling this
     * method it is already valid to write {@link Http3RequestStreamFrame}s as the codec is already in the pipeline.
     * @param ch    the {QuicStreamChannel} for the request stream.
     */
    protected abstract void initRequestStream(QuicStreamChannel ch);
}
