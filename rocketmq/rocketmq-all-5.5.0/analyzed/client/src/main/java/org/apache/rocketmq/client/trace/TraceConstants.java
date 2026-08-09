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
package org.apache.rocketmq.client.trace;

import org.apache.rocketmq.common.topic.TopicValidator;

/**
 * 消息轨迹模块常量：内部 Producer 组名、轨迹 Topic 前缀及
 * OpenTracing 风格的属性键名。
 */
public class TraceConstants {

    /** 轨迹内部 Producer 组名前缀。 */
    public static final String GROUP_NAME_PREFIX = "_INNER_TRACE_PRODUCER";
    /** 轨迹消息内容字段分隔符。 */
    public static final char CONTENT_SPLITOR = (char) 1;
    /** 轨迹消息属性字段分隔符。 */
    public static final char FIELD_SPLITOR = (char) 2;
    /** 轨迹内部 Producer 实例名。 */
    public static final String TRACE_INSTANCE_NAME = "PID_CLIENT_INNER_TRACE_PRODUCER";
    /** 轨迹数据 Topic 前缀（系统 Topic）。 */
    public static final String TRACE_TOPIC_PREFIX = TopicValidator.SYSTEM_TOPIC_PREFIX + "TRACE_DATA_";
    /** Span 目标端前缀。 */
    public static final String TO_PREFIX = "To_";
    /** Span 来源端前缀。 */
    public static final String FROM_PREFIX = "From_";
    /** 事务结束 Span 操作名。 */
    public static final String END_TRANSACTION = "EndTransaction";
    /** OpenTracing 服务名。 */
    public static final String ROCKETMQ_SERVICE = "rocketmq";
    /** 轨迹属性：是否成功。 */
    public static final String ROCKETMQ_SUCCESS = "rocketmq.success";
    /** 轨迹属性：消息 Tag。 */
    public static final String ROCKETMQ_TAGS = "rocketmq.tags";
    /** 轨迹属性：消息 Keys。 */
    public static final String ROCKETMQ_KEYS = "rocketmq.keys";
    /** 轨迹属性：存储主机。 */
    public static final String ROCKETMQ_STORE_HOST = "rocketmq.store_host";
    /** 轨迹属性：消息体长度。 */
    public static final String ROCKETMQ_BODY_LENGTH = "rocketmq.body_length";
    /** 轨迹属性：msgId（历史拼写 mgs_id）。 */
    public static final String ROCKETMQ_MSG_ID = "rocketmq.mgs_id";
    /** 轨迹属性：消息类型。 */
    public static final String ROCKETMQ_MSG_TYPE = "rocketmq.mgs_type";
    /** 轨迹属性：区域 ID。 */
    public static final String ROCKETMQ_REGION_ID = "rocketmq.region_id";
    /** 轨迹属性：事务 ID。 */
    public static final String ROCKETMQ_TRANSACTION_ID = "rocketmq.transaction_id";
    /** 轨迹属性：事务状态。 */
    public static final String ROCKETMQ_TRANSACTION_STATE = "rocketmq.transaction_state";
    /** 轨迹属性：是否来自事务回查。 */
    public static final String ROCKETMQ_IS_FROM_TRANSACTION_CHECK = "rocketmq.is_from_transaction_check";
    /** 轨迹属性：重试次数。 */
    public static final String ROCKETMQ_RETRY_TIMERS = "rocketmq.retry_times";
}
