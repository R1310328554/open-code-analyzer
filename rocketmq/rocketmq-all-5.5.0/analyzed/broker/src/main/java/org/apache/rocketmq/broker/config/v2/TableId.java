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
 * 配置逻辑表标识：嵌入 RocksDB 键中的 2 字节表 ID。
 *
 * @see <a href="https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html">Table, Key Value Mapping</a>
 */
public enum TableId {
    /** 未指定表。 */
    UNSPECIFIED((short) 0),
    /** 消费位点表。 */
    CONSUMER_OFFSET((short) 1),
    /** Pull 位点表。 */
    PULL_OFFSET((short) 2),
    /** Topic 配置表。 */
    TOPIC((short) 3),
    /** 订阅组配置表。 */
    SUBSCRIPTION_GROUP((short) 4);

    private final short value;

    /** 绑定 2 字节表 ID。 */
    TableId(short value) {
        this.value = value;
    }

    /** 返回表 ID 短整型值。 */
    public short getValue() {
        return value;
    }
}
