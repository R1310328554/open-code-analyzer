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

import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

/**
 * JZlib 编解码器内部工具：异常封装、封装类型转换与开销估算。
 */
final class ZlibUtil {

    /** 根据 JZlib 解压结果码抛出 {@link DecompressionException}。 */
    static void fail(Inflater z, String message, int resultCode) {
        throw inflaterException(z, message, resultCode);
    }

    /** 根据 JZlib 压缩结果码抛出 {@link CompressionException}。 */
    static void fail(Deflater z, String message, int resultCode) {
        throw deflaterException(z, message, resultCode);
    }

    /** 构造带 JZlib 错误码与消息的解压异常。 */
    static DecompressionException inflaterException(Inflater z, String message, int resultCode) {
        return new DecompressionException(message + " (" + resultCode + ')' + (z.msg != null? ": " + z.msg : ""));
    }

    /** 构造带 JZlib 错误码与消息的压缩异常。 */
    static CompressionException deflaterException(Deflater z, String message, int resultCode) {
        return new CompressionException(message + " (" + resultCode + ')' + (z.msg != null? ": " + z.msg : ""));
    }

    /** 将 Netty {@link ZlibWrapper} 映射为 JZlib 封装类型。 */
    static JZlib.WrapperType convertWrapperType(ZlibWrapper wrapper) {
        switch (wrapper) {
        case NONE:
            return JZlib.W_NONE;
        case ZLIB:
            return JZlib.W_ZLIB;
        case GZIP:
            return JZlib.W_GZIP;
        case ZLIB_OR_NONE:
            return JZlib.W_ANY;
        default:
            throw new Error("Unexpected wrapper type: " + wrapper);
        }
    }

    /** 估算各封装格式的头部/尾部额外字节开销。 */
    static int wrapperOverhead(ZlibWrapper wrapper) {
        switch (wrapper) {
        case NONE:
            return 0;
        case ZLIB:
        case ZLIB_OR_NONE:
            return 2;
        case GZIP:
            return 10;
        default:
            throw new Error("Unexpected wrapper type: " + wrapper);
        }
    }

    private ZlibUtil() {
    }
}
