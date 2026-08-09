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
package org.apache.rocketmq.common.consumer;

/**
 * 消费者首次启动或位点丢失时的起始消费位置策略。
 */
public enum ConsumeFromWhere {
    /** 从队列最新位点（last offset）开始消费。 */
    CONSUME_FROM_LAST_OFFSET,

    /** @deprecated 首次启动从最小位点开始，后续从最新位点开始。 */
    @Deprecated
    CONSUME_FROM_LAST_OFFSET_AND_FROM_MIN_WHEN_BOOT_FIRST,
    /** @deprecated 从队列最小位点开始消费。 */
    @Deprecated
    CONSUME_FROM_MIN_OFFSET,
    /** @deprecated 从队列最大位点开始消费。 */
    @Deprecated
    CONSUME_FROM_MAX_OFFSET,
    /** 从队列最早位点（first offset）开始消费。 */
    CONSUME_FROM_FIRST_OFFSET,
    /** 按指定时间戳回溯消费。 */
    CONSUME_FROM_TIMESTAMP,
}
