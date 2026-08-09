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

import java.util.EnumMap;

/**
 * 压缩器工厂：按 {@link CompressionType} 注册并返回 LZ4/ZSTD/ZLIB 实现。
 */
public class CompressorFactory {
    /** 压缩类型到具体压缩器实例的映射表。 */
    private static final EnumMap<CompressionType, Compressor> COMPRESSORS;

    static {
        COMPRESSORS = new EnumMap<>(CompressionType.class);
        COMPRESSORS.put(CompressionType.LZ4, new Lz4Compressor());
        COMPRESSORS.put(CompressionType.ZSTD, new ZstdCompressor());
        COMPRESSORS.put(CompressionType.ZLIB, new ZlibCompressor());
    }

    /** 根据压缩类型获取对应压缩器，未注册类型返回 null。 */
    public static Compressor getCompressor(CompressionType type) {
        return COMPRESSORS.get(type);
    }

}
