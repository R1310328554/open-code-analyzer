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
package io.openmessaging.rocketmq.domain;

/**
 * 非标准 OMS 配置键：RocketMQ 驱动扩展属性名常量。
 */
public interface NonStandardKeys {
    /** RocketMQ 消费组配置键。 */
    String CONSUMER_GROUP = "rmq.consumer.group";
    /** RocketMQ 生产组配置键。 */
    String PRODUCER_GROUP = "rmq.producer.group";
    /** 最大重投递次数配置键。 */
    String MAX_REDELIVERY_TIMES = "rmq.max.redelivery.times";
    /** 消息消费超时（分钟）配置键。 */
    String MESSAGE_CONSUME_TIMEOUT = "rmq.message.consume.timeout";
    /** 消费线程池最大线程数配置键。 */
    String MAX_CONSUME_THREAD_NUMS = "rmq.max.consume.thread.nums";
    /** 消费线程池最小线程数配置键。 */
    String MIN_CONSUME_THREAD_NUMS = "rmq.min.consume.thread.nums";
    /** 消息消费状态（成功/稍后重试）上下文键。 */
    String MESSAGE_CONSUME_STATUS = "rmq.message.consume.status";
    /** 消息目标 Topic/Queue 配置键。 */
    String MESSAGE_DESTINATION = "rmq.message.destination";
    /** Pull 单次拉取条数配置键。 */
    String PULL_MESSAGE_BATCH_NUMS = "rmq.pull.message.batch.nums";
    /** Pull 本地缓存容量配置键。 */
    String PULL_MESSAGE_CACHE_CAPACITY = "rmq.pull.message.cache.capacity";
}
