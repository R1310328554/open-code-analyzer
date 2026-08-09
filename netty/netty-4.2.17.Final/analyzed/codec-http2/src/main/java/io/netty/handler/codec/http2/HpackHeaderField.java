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

/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty.handler.codec.http2;

import static io.netty.handler.codec.http2.HpackUtil.equalsVariableTime;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * HPACK 动态/静态表中的单条头字段（name + value）。
 * <p>RFC 7541 §4.1：每条目占用 {@code name.length + value.length + HEADER_ENTRY_OVERHEAD} 字节，
 * 其中 {@code HEADER_ENTRY_OVERHEAD=32} 估算结构体开销。
 */
class HpackHeaderField {

    // Section 4.1. Calculating Table Size
    // 额外 32 字节估算条目在表结构中的存储开销。
    static final int HEADER_ENTRY_OVERHEAD = 32;

    static long sizeOf(CharSequence name, CharSequence value) {
        return name.length() + value.length() + HEADER_ENTRY_OVERHEAD;
    }

    /** 头名（小写 ASCII，HTTP/2 要求）。 */
    final CharSequence name;
    /** 头值（ISO-8859-1 字节序列的字符视图）。 */
    final CharSequence value;

    // 仅当 name/value 为 ISO-8859-1 编码时可调用此构造器。
    HpackHeaderField(CharSequence name, CharSequence value) {
        this.name = checkNotNull(name, "name");
        this.value = checkNotNull(value, "value");
    }

    final int size() {
        return name.length() + value.length() + HEADER_ENTRY_OVERHEAD;
    }

    public final boolean equalsForTest(HpackHeaderField other) {
        return equalsVariableTime(name, other.name) && equalsVariableTime(value, other.value);
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
