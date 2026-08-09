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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;

/**
 * SPDY DATA 帧：在已建立的流上传输请求/响应体字节。
 * <p>继承 {@link SpdyStreamFrame} 的流 ID 与 FIN 语义，并通过 {@link ByteBufHolder}
 * 持有可变长 payload；单帧 payload 上限 16 MiB（24 位长度字段）。
 */
public interface SpdyDataFrame extends ByteBufHolder, SpdyStreamFrame {

    @Override
    SpdyDataFrame setStreamId(int streamID);

    @Override
    SpdyDataFrame setLast(boolean last);

    /**
     * 返回本帧数据负载；无数据时返回 {@link Unpooled#EMPTY_BUFFER}。
     *
     * The data payload cannot exceed 16777215 bytes.
     */
    @Override
    ByteBuf content();

    @Override
    SpdyDataFrame copy();

    @Override
    SpdyDataFrame duplicate();

    @Override
    SpdyDataFrame retainedDuplicate();

    @Override
    SpdyDataFrame replace(ByteBuf content);

    @Override
    SpdyDataFrame retain();

    @Override
    SpdyDataFrame retain(int increment);

    @Override
    SpdyDataFrame touch();

    @Override
    SpdyDataFrame touch(Object hint);
}
