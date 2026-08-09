/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.command.codec;

import java.nio.charset.Charset;

/**
 * 命令响应体编码器：将类型 {@code R} 的对象编码为字节数组。
 * 通过 {@link #canEncode} 判断是否支持源类型。
 *
 * @param <R> 源类型
 * @author Eric Zhao
 */
public interface Encoder<R> {

    /**
     * 判断是否支持编码指定类型的对象。
     *
     * @param clazz 源类型
     * @return 支持时 true，否则 false
     */
    boolean canEncode(Class<?> clazz);

    /**
     * 使用指定字符集将对象编码为字节数组。
     *
     * @param r 待编码对象
     * @param charset 字符集
     * @return 编码后的字节
     * @throws Exception 编码失败时抛出
     */
    byte[] encode(R r, Charset charset) throws Exception;

    /**
     * Encode the given object into a byte array with the default charset.
     *
     * @param r the object to encode
     * @return the encoded byte buffer, which is already flipped.
     * @throws Exception error occurs when encoding the object (e.g. IO fails)
     */
    byte[] encode(R r) throws Exception;
}
