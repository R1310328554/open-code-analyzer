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
package io.netty.handler.codec.compression;

/**
 * DEFLATE 压缩流的外层封装格式枚举。
 */
public enum ZlibWrapper {
    /** RFC 1950 定义的 ZLIB 封装（含 Adler-32 校验）。 */
    ZLIB,
    /** RFC 1952 定义的 GZIP 封装（含 CRC32 与长度尾）。 */
    GZIP,
    /** 裸 DEFLATE 流，无头尾封装。 */
    NONE,
    /** 解压时先尝试 {@link #ZLIB}，失败再试 {@link #NONE}；仅用于解码。 */
    ZLIB_OR_NONE
}
