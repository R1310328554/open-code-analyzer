/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;

/**
 * Different modes of reading from a {@link DomainSocketChannel}.
 * <p>Unix 域流式通道的读取模式：普通字节 I/O 或通过 ancillary data 接收对端传递的文件描述符。</p>
 */
public enum DomainSocketReadMode {

    /**
     * Read {@link ByteBuf}s from the {@link DomainSocketChannel}.
     * <p>默认模式：从通道读取字节并封装为 {@link ByteBuf} 向下游传递。</p>
     */
    BYTES,

    /**
     * Read {@link FileDescriptor}s from the {@link DomainSocketChannel}.
     * <p>FD 传递模式：经 {@code SCM_RIGHTS} 接收对端发来的文件描述符。</p>
     */
    FILE_DESCRIPTORS
}
