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
 * 配置键表级前缀：标识该键属于逻辑表命名空间（区别于其他 KV 用途）。
 */
public enum TablePrefix {
    /** 未指定前缀。 */
    UNSPECIFIED((byte) 0),
    /** 标准配置表前缀。 */
    TABLE((byte) 1);

    private final byte value;

    /** 绑定单字节前缀值。 */
    TablePrefix(byte value) {
        this.value = value;
    }

    /** 返回表前缀的字节值。 */
    public byte getValue() {
        return value;
    }
}
