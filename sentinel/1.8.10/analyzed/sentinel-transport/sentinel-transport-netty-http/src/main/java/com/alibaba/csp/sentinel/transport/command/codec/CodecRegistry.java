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

import java.util.ArrayList;
import java.util.List;

/**
 * 命令编解码器注册表：维护 {@link Encoder} 与 {@link Decoder} 列表，构造时注册默认字符串编解码器。
 *
 * @author Eric Zhao
 */
public final class CodecRegistry {

    private final List<Encoder<?>> encoderList = new ArrayList<Encoder<?>>();
    private final List<Decoder<?>> decoderList = new ArrayList<Decoder<?>>();

    public CodecRegistry() {
        // 注册默认字符串编解码器
        registerEncoder(DefaultCodecs.STRING_ENCODER);

        registerDecoder(DefaultCodecs.STRING_DECODER);
    }

    /** 注册响应体编码器。 */
    public void registerEncoder(Encoder<?> encoder) {
        encoderList.add(encoder);
    }

    /** 注册请求体解码器。 */
    public void registerDecoder(Decoder<?> decoder) {
        decoderList.add(decoder);
    }

    public List<Encoder<?>> getEncoderList() {
        return encoderList;
    }

    public List<Decoder<?>> getDecoderList() {
        return decoderList;
    }

    /** 清空已注册的编解码器列表。 */
    public void reset() {
        encoderList.clear();
        decoderList.clear();
    }
}
