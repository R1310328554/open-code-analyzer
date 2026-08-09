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
 * 配置记录类型前缀：标识 RocksDB 键值对中的记录语义（版本元数据或业务数据）。
 */
public enum RecordPrefix {
    /** 未指定类型。 */
    UNSPECIFIED((byte)0),
    /** 数据版本记录。 */
    DATA_VERSION((byte)1),
    /** 业务配置数据记录。 */
    DATA((byte)2);

    private final byte value;

    /** 绑定单字节编码值。 */
    RecordPrefix(byte value) {
        this.value = value;
    }

    /** 返回记录前缀的字节值。 */
    public byte getValue() {
        return value;
    }
}
