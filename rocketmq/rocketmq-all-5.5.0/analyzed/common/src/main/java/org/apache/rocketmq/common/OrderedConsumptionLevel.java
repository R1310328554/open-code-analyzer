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
package org.apache.rocketmq.common;

/**
 * 顺序消费粒度：按队列或按 Sharding Key。
 */
public enum OrderedConsumptionLevel {
    /** 按消息队列顺序消费。 */
    QUEUE(0),
    /** 按 Sharding Key 顺序消费。 */
    SHARDING_KEY(1);

    /** 枚举整型值。 */
    private final int value;

    OrderedConsumptionLevel(int value) {
        this.value = value;
    }

    /** 返回整型值。 */
    public int getValue() {
        return value;
    }

    /** 按整型解析，1 为 SHARDING_KEY，否则 QUEUE。 */
    public static OrderedConsumptionLevel valueOf(int value) {
        if (value == 1) {
            return SHARDING_KEY;
        }
        return QUEUE;
    }
}
