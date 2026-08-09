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
package org.apache.rocketmq.store;

/**
 * 拉取消息状态枚举：描述 offset 合法性、队列匹配及消息是否存在等结果。
 */
public enum GetMessageStatus {

    /** 成功找到消息。 */
    FOUND,

    /** 无匹配消息（如 TAG 过滤未命中）。 */
    NO_MATCHED_MESSAGE,

    /** 消息正在被删除。 */
    MESSAGE_WAS_REMOVING,

    /** offset 指向空槽位。 */
    OFFSET_FOUND_NULL,

    /** offset 严重越界。 */
    OFFSET_OVERFLOW_BADLY,

    /** offset 越界一条（通常可重试）。 */
    OFFSET_OVERFLOW_ONE,

    /** offset 过小，低于最小可读位置。 */
    OFFSET_TOO_SMALL,

    /** 无匹配的逻辑队列。 */
    NO_MATCHED_LOGIC_QUEUE,

    /** 队列中暂无消息。 */
    NO_MESSAGE_IN_QUEUE,

    /** offset 已被重置。 */
    OFFSET_RESET
}
