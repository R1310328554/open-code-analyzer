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
package io.netty.handler.logging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;

/**
 * Used to control the format and verbosity of logging for {@link ByteBuf}s and {@link ByteBufHolder}s.
 *
 * @see LoggingHandler
 *
 * <p>控制 {@link LoggingHandler} 记录 {@link ByteBuf} / {@link ByteBufHolder} 时的格式与详细程度。</p>
 */
public enum ByteBufFormat {
    /**
     * {@link ByteBuf}s will be logged in a simple format, with no hex dump included.
     *
     * <p>仅记录可读字节数等简要信息，不包含十六进制转储。</p>
     */
    SIMPLE,
    /**
     * {@link ByteBuf}s will be logged using {@link ByteBufUtil#appendPrettyHexDump(StringBuilder, ByteBuf)}.
     *
     * <p>附加 {@link ByteBufUtil#appendPrettyHexDump} 生成的可读十六进制转储。</p>
     */
    HEX_DUMP
}
