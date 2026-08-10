/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.jgroups.header;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jgroups.Header;
import org.jgroups.util.Util;

/**
 * 在 JGroups 消息中携带 OpenTelemetry {@link io.opentelemetry.api.trace.Span} 追踪上下文的 Header。
 * <p>
 * 以键值对形式存储 W3C Trace Context 传播字段，供 {@link org.keycloak.jgroups.protocol.OPEN_TELEMETRY} 协议注入/提取。
 *
 * @author Bela Ban
 * @since 1.0.0
 */
public class TracerHeader extends Header {
    /** JGroups Header 魔数 ID。 */
    public static final short ID = 1050;
    /** 追踪上下文键值对（如 traceparent、tracestate）。 */
    protected final Map<String, String> ctx = new HashMap<>();

    public TracerHeader() {
    }

    public short getMagicId() {
        return ID;
    }

    public Supplier<? extends Header> create() {
        return TracerHeader::new;
    }

    /** 写入追踪上下文键值。 */
    public void put(String key, String value) {
        ctx.put(key, value);
    }

    /** 读取指定键的追踪上下文值。 */
    public String get(String key) {
        return ctx.get(key);
    }

    public Set<String> keys() {
        return ctx.keySet();
    }

    /** 计算序列化后的字节大小（用于 JGroups 缓冲区预分配）。 */
    public int serializedSize() {
        int size = Integer.BYTES;
        int num_attrs = ctx.size();
        if (num_attrs > 0) {
            for (Map.Entry<String, String> entry : ctx.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                size += Util.size(key) + Util.size(val);
            }
        }
        return size;
    }

    public void writeTo(DataOutput out) throws IOException {
        out.writeInt(ctx.size());
        if (!ctx.isEmpty()) {
            for (Map.Entry<String, String> e : ctx.entrySet()) {
                Util.writeString(e.getKey(), out);
                Util.writeString(e.getValue(), out);
            }
        }
    }

    public void readFrom(DataInput in) throws IOException {
        int size = in.readInt();
        if (size > 0) {
            for (int i = 0; i < size; i++)
                ctx.put(Util.readString(in), Util.readString(in));
        }
    }

    public String toString() {
        return ctx.toString();
    }
}
