/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

/**
 * SPDY 协议的 RST_STREAM 帧：异常或主动终止单条流。
 * <p>携带 {@link SpdyStreamStatus} 说明关闭原因；发送后该流对发送方即不可再写。
 */
public interface SpdyRstStreamFrame extends SpdyStreamFrame {

    /**
     * 返回流重置的状态码。
     */
    SpdyStreamStatus status();

    /**
     * 设置流重置的状态码。
     */
    SpdyRstStreamFrame setStatus(SpdyStreamStatus status);

    @Override
    SpdyRstStreamFrame setStreamId(int streamId);

    @Override
    SpdyRstStreamFrame setLast(boolean last);
}
