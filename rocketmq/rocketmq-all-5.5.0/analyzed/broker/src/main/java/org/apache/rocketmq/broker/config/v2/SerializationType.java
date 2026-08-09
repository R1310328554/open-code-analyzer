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
package org.apache.rocketmq.broker.config.v2;

/**
 * 配置值序列化格式：写入 RocksDB 时在值首部携带的类型标识。
 */
public enum SerializationType {
    /** 未指定格式。 */
    UNSPECIFIED((byte) 0),

    /** JSON 文本序列化。 */
    JSON((byte) 1),

    /** Protocol Buffers 二进制序列化。 */
    PROTOBUF((byte) 2),

    /** FlatBuffers 二进制序列化。 */
    FLAT_BUFFERS((byte) 3);

    private final byte value;

    /** 绑定单字节编码值。 */
    SerializationType(byte value) {
        this.value = value;
    }

    /** 返回序列化类型的字节值。 */
    public byte getValue() {
        return value;
    }

    /** 按字节值解析序列化类型，未知时返回 {@link #UNSPECIFIED}。 */
    public static SerializationType valueOf(byte value) {
        for (SerializationType type : SerializationType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return SerializationType.UNSPECIFIED;
    }
}
