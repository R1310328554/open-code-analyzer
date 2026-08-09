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
package org.apache.rocketmq.common.stats;

/**
 * Broker/客户端运行时统计项名称常量，供 {@link StatsItemSet} 与监控上报引用。
 */
public class Stats {

    /** 队列写入消息条数。 */
    public static final String QUEUE_PUT_NUMS = "QUEUE_PUT_NUMS";
    /** 队列写入消息字节数。 */
    public static final String QUEUE_PUT_SIZE = "QUEUE_PUT_SIZE";
    /** 队列读取消息条数。 */
    public static final String QUEUE_GET_NUMS = "QUEUE_GET_NUMS";
    /** 队列读取消息字节数。 */
    public static final String QUEUE_GET_SIZE = "QUEUE_GET_SIZE";
    /** Topic 写入消息条数。 */
    public static final String TOPIC_PUT_NUMS = "TOPIC_PUT_NUMS";
    /** Topic 写入消息字节数。 */
    public static final String TOPIC_PUT_SIZE = "TOPIC_PUT_SIZE";
    /** 消费组拉取消息条数。 */
    public static final String GROUP_GET_NUMS = "GROUP_GET_NUMS";
    /** 消费组拉取消息字节数。 */
    public static final String GROUP_GET_SIZE = "GROUP_GET_SIZE";
    /** 回退（sendback）写入条数。 */
    public static final String SNDBCK_PUT_NUMS = "SNDBCK_PUT_NUMS";
    /** Broker 总写入条数。 */
    public static final String BROKER_PUT_NUMS = "BROKER_PUT_NUMS";
    /** Broker 总读取条数。 */
    public static final String BROKER_GET_NUMS = "BROKER_GET_NUMS";
    /** 消费组从磁盘读取条数。 */
    public static final String GROUP_GET_FROM_DISK_NUMS = "GROUP_GET_FROM_DISK_NUMS";
    /** 消费组从磁盘读取字节数。 */
    public static final String GROUP_GET_FROM_DISK_SIZE = "GROUP_GET_FROM_DISK_SIZE";
    /** Broker 从磁盘读取条数。 */
    public static final String BROKER_GET_FROM_DISK_NUMS = "BROKER_GET_FROM_DISK_NUMS";
    /** Broker 从磁盘读取字节数。 */
    public static final String BROKER_GET_FROM_DISK_SIZE = "BROKER_GET_FROM_DISK_SIZE";
    /** 商业版发送次数。 */
    public static final String COMMERCIAL_SEND_TIMES = "COMMERCIAL_SEND_TIMES";
    /** 商业版回退次数。 */
    public static final String COMMERCIAL_SNDBCK_TIMES = "COMMERCIAL_SNDBCK_TIMES";
    /** 商业版接收次数。 */
    public static final String COMMERCIAL_RCV_TIMES = "COMMERCIAL_RCV_TIMES";
    /** 商业版 epoll 接收轮次。 */
    public static final String COMMERCIAL_RCV_EPOLLS = "COMMERCIAL_RCV_EPOLLS";
    /** 商业版发送字节数。 */
    public static final String COMMERCIAL_SEND_SIZE = "COMMERCIAL_SEND_SIZE";
    /** 商业版接收字节数。 */
    public static final String COMMERCIAL_RCV_SIZE = "COMMERCIAL_RCV_SIZE";
    /** 商业版权限校验失败次数。 */
    public static final String COMMERCIAL_PERM_FAILURES = "COMMERCIAL_PERM_FAILURES";

    /** 消费组降级拉取字节数。 */
    public static final String GROUP_GET_FALL_SIZE = "GROUP_GET_FALL_SIZE";
    /** 消费组降级拉取耗时。 */
    public static final String GROUP_GET_FALL_TIME = "GROUP_GET_FALL_TIME";
    /** 消费组拉取延迟。 */
    public static final String GROUP_GET_LATENCY = "GROUP_GET_LATENCY";
    /** Topic 写入延迟。 */
    public static final String TOPIC_PUT_LATENCY = "TOPIC_PUT_LATENCY";
    /** 消费组 ACK 条数。 */
    public static final String GROUP_ACK_NUMS = "GROUP_ACK_NUMS";
    /** 消费组 Checkpoint 条数。 */
    public static final String GROUP_CK_NUMS = "GROUP_CK_NUMS";
}
