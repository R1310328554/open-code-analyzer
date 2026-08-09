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

import com.alibaba.csp.sentinel.config.SentinelConfig;

/**
 * 字符串解码器：将请求体字节按 {@link SentinelConfig#charset()} 或指定 Charset 转为 String。
 *
 * @author Eric Zhao
 */
public class StringDecoder implements Decoder<String> {

    @Override
    /** 仅支持 {@link String} 及其子类。 */
    public boolean canDecode(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    @Override
    public String decode(byte[] bytes) throws Exception {
        return decode(bytes, Charset.forName(SentinelConfig.charset()));
    }

    @Override
    public String decode(byte[] bytes, Charset charset) {
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalArgumentException("无效的字节数组");
        }
        return new String(bytes, charset);
    }
}
