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
 * 写消息结果状态枚举：CommitLog 写入、刷盘、同步及校验各阶段的返回码。
 */
public enum PutMessageStatus {
    /** 写入成功。 */
    PUT_OK,
    /** 刷盘超时。 */
    FLUSH_DISK_TIMEOUT,
    /** 同步从节点刷盘超时。 */
    FLUSH_SLAVE_TIMEOUT,
    /** 从节点不可用。 */
    SLAVE_NOT_AVAILABLE,
    /** 存储服务不可用。 */
    SERVICE_NOT_AVAILABLE,
    /** 创建 MappedFile 失败。 */
    CREATE_MAPPED_FILE_FAILED,
    /** 消息内容或格式非法。 */
    MESSAGE_ILLEGAL,
    /** 消息属性大小超限。 */
    PROPERTIES_SIZE_EXCEEDED,
    /** 操作系统页缓存繁忙。 */
    OS_PAGE_CACHE_BUSY,
    /** 未知错误。 */
    UNKNOWN_ERROR,
    /** 同步副本数量不足。 */
    IN_SYNC_REPLICAS_NOT_ENOUGH,
    /** 写入远程 Broker 失败。 */
    PUT_TO_REMOTE_BROKER_FAIL,
    /** LMQ 消费队列数量超限。 */
    LMQ_CONSUME_QUEUE_NUM_EXCEEDED,
    /** 时间轮流控拒绝写入。 */
    WHEEL_TIMER_FLOW_CONTROL,
    /** 时间轮消息非法。 */
    WHEEL_TIMER_MSG_ILLEGAL,
    /** 时间轮功能未启用。 */
    WHEEL_TIMER_NOT_ENABLE
}
