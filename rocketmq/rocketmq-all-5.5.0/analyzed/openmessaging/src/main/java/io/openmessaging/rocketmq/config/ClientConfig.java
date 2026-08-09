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
package io.openmessaging.rocketmq.config;

import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.rocketmq.domain.NonStandardKeys;

/**
 * OMS RocketMQ 客户端配置：映射标准 OMS 键与 RocketMQ 专有扩展项。
 */
public class ClientConfig implements OMSBuiltinKeys, NonStandardKeys {
    /** 驱动实现类名。 */
    private String driverImpl;
    /** NameServer 接入点地址（可逗号分隔多个）。 */
    private String accessPoints;
    /** OMS 命名空间。 */
    private String namespace;
    /** 生产者标识。 */
    private String producerId;
    /** 消费者标识（对应 RocketMQ Consumer Group）。 */
    private String consumerId;
    /** 通用操作超时（毫秒）。 */
    private int operationTimeout = 5000;
    /** 区域标识。 */
    private String region;
    /** 路由源（Topic/Queue 等）。 */
    private String routingSource;
    /** 路由目标。 */
    private String routingDestination;
    /** 路由表达式（如 Tag 过滤）。 */
    private String routingExpression;
    /** RocketMQ 消费组名。 */
    private String rmqConsumerGroup;
    /** RocketMQ 生产组名，默认 OMS 内置组。 */
    private String rmqProducerGroup = "__OMS_PRODUCER_DEFAULT_GROUP";
    /** 最大重投递次数。 */
    private int rmqMaxRedeliveryTimes = 16;
    /** 单条消息消费超时（分钟）。 */
    private int rmqMessageConsumeTimeout = 15; //In minutes
    /** 消费线程池最大线程数。 */
    private int rmqMaxConsumeThreadNums = 64;
    /** 消费线程池最小线程数。 */
    private int rmqMinConsumeThreadNums = 20;
    /** 消息目标 Topic/Queue 名称。 */
    private String rmqMessageDestination;
    /** Pull 模式单次拉取消息条数。 */
    private int rmqPullMessageBatchNums = 32;
    /** Pull 模式本地消息缓存容量。 */
    private int rmqPullMessageCacheCapacity = 1000;

    public String getDriverImpl() {
        return driverImpl;
    }

    public void setDriverImpl(final String driverImpl) {
        this.driverImpl = driverImpl;
    }

    public String getAccessPoints() {
        return accessPoints;
    }

    public void setAccessPoints(final String accessPoints) {
        this.accessPoints = accessPoints;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(final String producerId) {
        this.producerId = producerId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(final String consumerId) {
        this.consumerId = consumerId;
    }

    public int getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(final int operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

    public String getRoutingSource() {
        return routingSource;
    }

    public void setRoutingSource(final String routingSource) {
        this.routingSource = routingSource;
    }

    public String getRmqConsumerGroup() {
        return rmqConsumerGroup;
    }

    public void setRmqConsumerGroup(final String rmqConsumerGroup) {
        this.rmqConsumerGroup = rmqConsumerGroup;
    }

    public String getRmqProducerGroup() {
        return rmqProducerGroup;
    }

    public void setRmqProducerGroup(final String rmqProducerGroup) {
        this.rmqProducerGroup = rmqProducerGroup;
    }

    public int getRmqMaxRedeliveryTimes() {
        return rmqMaxRedeliveryTimes;
    }

    public void setRmqMaxRedeliveryTimes(final int rmqMaxRedeliveryTimes) {
        this.rmqMaxRedeliveryTimes = rmqMaxRedeliveryTimes;
    }

    public int getRmqMessageConsumeTimeout() {
        return rmqMessageConsumeTimeout;
    }

    public void setRmqMessageConsumeTimeout(final int rmqMessageConsumeTimeout) {
        this.rmqMessageConsumeTimeout = rmqMessageConsumeTimeout;
    }

    public int getRmqMaxConsumeThreadNums() {
        return rmqMaxConsumeThreadNums;
    }

    public void setRmqMaxConsumeThreadNums(final int rmqMaxConsumeThreadNums) {
        this.rmqMaxConsumeThreadNums = rmqMaxConsumeThreadNums;
    }

    public int getRmqMinConsumeThreadNums() {
        return rmqMinConsumeThreadNums;
    }

    public void setRmqMinConsumeThreadNums(final int rmqMinConsumeThreadNums) {
        this.rmqMinConsumeThreadNums = rmqMinConsumeThreadNums;
    }

    public String getRmqMessageDestination() {
        return rmqMessageDestination;
    }

    public void setRmqMessageDestination(final String rmqMessageDestination) {
        this.rmqMessageDestination = rmqMessageDestination;
    }

    public int getRmqPullMessageBatchNums() {
        return rmqPullMessageBatchNums;
    }

    public void setRmqPullMessageBatchNums(final int rmqPullMessageBatchNums) {
        this.rmqPullMessageBatchNums = rmqPullMessageBatchNums;
    }

    public int getRmqPullMessageCacheCapacity() {
        return rmqPullMessageCacheCapacity;
    }

    public void setRmqPullMessageCacheCapacity(final int rmqPullMessageCacheCapacity) {
        this.rmqPullMessageCacheCapacity = rmqPullMessageCacheCapacity;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRoutingDestination() {
        return routingDestination;
    }

    public void setRoutingDestination(String routingDestination) {
        this.routingDestination = routingDestination;
    }

    public String getRoutingExpression() {
        return routingExpression;
    }

    public void setRoutingExpression(String routingExpression) {
        this.routingExpression = routingExpression;
    }
}
