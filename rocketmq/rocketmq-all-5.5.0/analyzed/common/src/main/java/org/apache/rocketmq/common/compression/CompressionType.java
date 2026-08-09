/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.compression;

import org.apache.rocketmq.common.sysflag.MessageSysFlag;

/**
 * 消息体压缩算法类型，编码值写入 {@link MessageSysFlag}。
 */
public enum CompressionType {

    /**
     * 压缩类型编号最多可扩展至 7 种（见 {@link MessageSysFlag}）。
     *
     * 基准测试参考 https://github.com/facebook/zstd
     *
     *    |   Compressor   |  Ratio  | Compression | Decompress |
     *    |----------------|---------|-------------|------------|
     *    |   zstd 1.5.1   |  2.887  |   530 MB/s  |  1700 MB/s |
     *    |  zlib 1.2.11   |  2.743  |    95 MB/s  |   400 MB/s |
     *    |    lz4 1.9.3   |  2.101  |   740 MB/s  |  4500 MB/s |
     */

    /** LZ4 压缩（值 1）。 */
    LZ4(1),
    /** Zstandard 压缩（值 2）。 */
    ZSTD(2),
    /** Zlib 压缩（值 3）。 */
    ZLIB(3);

    /** 协议/存储中使用的整型编码。 */
    private final int value;

    CompressionType(int value) {
        this.value = value;
    }

    /** 返回整型编码值。 */
    public int getValue() {
        return value;
    }

    /**
     * 按名称解析压缩类型（忽略大小写与首尾空白）。
     *
     * @param name 算法名称（LZ4/ZSTD/ZLIB）
     * @return 对应 {@link CompressionType}
     */
    public static CompressionType of(String name) {
        switch (name.trim().toUpperCase()) {
            case "LZ4":
                return CompressionType.LZ4;
            case "ZSTD":
                return CompressionType.ZSTD;
            case "ZLIB":
                return CompressionType.ZLIB;
            default:
                throw new RuntimeException("Unsupported compress type name: " + name);
        }
    }

    /**
     * 按整型编码查找压缩类型。
     *
     * @param value 编码值（0 兼容旧版无类型，视为 ZLIB）
     * @return 对应 {@link CompressionType}
     */
    public static CompressionType findByValue(int value) {
        switch (value) {
            case 1:
                return LZ4;
            case 2:
                return ZSTD;
            case 0: // 兼容旧版未携带压缩类型的消息
            case 3:
                return ZLIB;
            default:
                throw new RuntimeException("Unknown compress type value: " + value);
        }
    }

    /** 返回写入 {@link MessageSysFlag} 的压缩类型标志位。 */
    public int getCompressionFlag() {
        switch (value) {
            case 1:
                return MessageSysFlag.COMPRESSION_LZ4_TYPE;
            case 2:
                return MessageSysFlag.COMPRESSION_ZSTD_TYPE;
            case 3:
                return MessageSysFlag.COMPRESSION_ZLIB_TYPE;
            default:
                throw new RuntimeException("Unsupported compress type flag: " + value);
        }
    }
}
