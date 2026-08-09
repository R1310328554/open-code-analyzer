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

package org.apache.rocketmq.tools.admin.api;

/**
 * 消息轨迹类型枚举：标识消费组对消息的处理状态。
 */
public enum TrackType {
    /** 消息已被正常消费。 */
    CONSUMED,
    /** 消息已投递但被消费端过滤丢弃。 */
    CONSUMED_BUT_FILTERED,
    /** 消息处于 Pull 消费模式处理中。 */
    PULL,
    /** 消息尚未被消费。 */
    NOT_CONSUME_YET,
    /** 消费端当前不在线。 */
    NOT_ONLINE,
    /** 广播消费模式下已投递。 */
    CONSUME_BROADCASTING,
    /** 未知或无法判定的轨迹状态。 */
    UNKNOWN
}
