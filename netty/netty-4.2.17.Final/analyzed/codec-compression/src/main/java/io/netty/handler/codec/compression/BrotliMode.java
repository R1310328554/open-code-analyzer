/*
 * Copyright 2024 The Netty Project
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

import com.aayushatharva.brotli4j.encoder.Encoder;

import static com.aayushatharva.brotli4j.encoder.Encoder.Mode;

/**
 * Brotli 压缩模式：通用、文本或字体数据，映射至 brotli4j {@link Encoder.Mode}。
 */
public enum BrotliMode {

    /**
     * 不对输入数据做假设，适用于各类二进制与混合内容（默认模式）。
     */
    GENERIC,

    /**
     * 针对 UTF-8 文本优化，适合 HTML/JSON 等。
     */
    TEXT,

    /**
     * 专为 WOFF 2.0 等字体数据设计。
     */
    FONT;

    /**
     * 转换为 brotli4j {@link Encoder.Mode}。
     *
     * @return a new {@link Encoder.Mode}
     */
    Mode adapt() {
        switch (this) {
            case GENERIC:
                return Mode.GENERIC;
            case TEXT:
                return Mode.TEXT;
            case FONT:
                return Mode.FONT;
            default:
                throw new IllegalStateException("Unsupported enum value: " + this);
        }
    }
}
