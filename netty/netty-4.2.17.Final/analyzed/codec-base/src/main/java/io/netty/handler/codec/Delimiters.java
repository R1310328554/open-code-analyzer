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
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 供 {@link DelimiterBasedFrameDecoder} 使用的常用分隔符集合。
 */
public final class Delimiters {

    /**
     * 返回 {@code NUL (0x00)} 分隔符，适用于 Flash XML socket 等协议。
     */
    public static ByteBuf[] nulDelimiter() {
        return new ByteBuf[] {
                Unpooled.wrappedBuffer(new byte[] { 0 }) };
    }

    /**
     * 返回 {@code CR+LF} 与单独 {@code LF} 分隔符，适用于文本行协议。
     */
    public static ByteBuf[] lineDelimiter() {
        return new ByteBuf[] {
                Unpooled.wrappedBuffer(new byte[] { '\r', '\n' }),
                Unpooled.wrappedBuffer(new byte[] { '\n' }),
        };
    }

    private Delimiters() {
        // 工具类，禁止实例化
    }
}
