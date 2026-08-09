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
package io.netty.handler.codec.http2;

import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.UnsupportedValueConverter;
import io.netty.handler.codec.ValueConverter;

import static io.netty.util.AsciiString.CASE_INSENSITIVE_HASHER;
import static io.netty.util.AsciiString.CASE_SENSITIVE_HASHER;

/**
 * HTTP/2 内部使用的 {@link CharSequence} 键头表，基于 {@link DefaultHeaders}。
 * <p>键为 {@link io.netty.util.AsciiString} 或普通 {@link CharSequence}，
 * 可选大小写敏感/不敏感哈希；值类型由 {@link ValueConverter} 决定。
 * Internal use only!
 */
public final class CharSequenceMap<V> extends DefaultHeaders<CharSequence, V, CharSequenceMap<V>> {

    /** 默认大小写不敏感（HTTP 头名惯例）。 */
    public CharSequenceMap() {
        this(true);
    }

    /**
     * @param caseSensitive {@code true} 使用大小写敏感哈希；{@code false} 忽略大小写
     */
    public CharSequenceMap(boolean caseSensitive) {
        this(caseSensitive, UnsupportedValueConverter.<V>instance());
    }

    public CharSequenceMap(boolean caseSensitive, ValueConverter<V> valueConverter) {
        super(caseSensitive ? CASE_SENSITIVE_HASHER : CASE_INSENSITIVE_HASHER, valueConverter);
    }

    @SuppressWarnings("unchecked")
    public CharSequenceMap(boolean caseSensitive, ValueConverter<V> valueConverter, int arraySizeHint) {
        super(caseSensitive ? CASE_SENSITIVE_HASHER : CASE_INSENSITIVE_HASHER, valueConverter,
                NameValidator.NOT_NULL, arraySizeHint);
    }
}
