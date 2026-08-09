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
 * SPDY SYN_REPLY 帧：服务端对已有流的响应头块。
 * <p>对应 HTTP 响应的状态行与头域，通过 {@link SpdyHeaders} 承载；
 * 常与后续 DATA 帧组成完整响应。方法返回 {@code this} 类型以支持链式修改。
 */
public interface SpdySynReplyFrame extends SpdyHeadersFrame {

    @Override
    SpdySynReplyFrame setStreamId(int streamID);

    @Override
    SpdySynReplyFrame setLast(boolean last);

    /** 标记帧已损坏或不应再被编解码器处理。 */
    @Override
    SpdySynReplyFrame setInvalid();
}
