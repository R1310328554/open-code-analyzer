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
package io.openmessaging.rocketmq;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.ResourceManager;
import io.openmessaging.consumer.PullConsumer;
import io.openmessaging.consumer.PushConsumer;
import io.openmessaging.consumer.StreamingConsumer;
import io.openmessaging.exception.OMSNotSupportedException;
import io.openmessaging.producer.Producer;
import io.openmessaging.rocketmq.consumer.PullConsumerImpl;
import io.openmessaging.rocketmq.consumer.PushConsumerImpl;
import io.openmessaging.rocketmq.producer.ProducerImpl;
import io.openmessaging.rocketmq.utils.OMSUtil;

/**
 * OpenMessaging 接入点实现：基于 RocketMQ 创建 Producer/Pull/Push 消费者实例。
 */
public class MessagingAccessPointImpl implements MessagingAccessPoint {

    /** 接入点级配置属性（NameServer 地址、命名空间等）。 */
    private final KeyValue accessPointProperties;

    /** 使用给定属性构造 RocketMQ 版 MessagingAccessPoint。 */
    public MessagingAccessPointImpl(final KeyValue accessPointProperties) {
        this.accessPointProperties = accessPointProperties;
    }

    @Override
    /** 返回接入点配置属性副本视图。 */
    public KeyValue attributes() {
        return accessPointProperties;
    }

    @Override
    /** 返回 OMS RocketMQ 驱动实现版本号。 */
    public String implVersion() {
        return "0.3.0";
    }

    @Override
    /** 使用接入点默认属性创建 Producer。 */
    public Producer createProducer() {
        return new ProducerImpl(this.accessPointProperties);
    }

    @Override
    /** 合并接入点与实例属性后创建 Producer。 */
    public Producer createProducer(KeyValue properties) {
        return new ProducerImpl(OMSUtil.buildKeyValue(this.accessPointProperties, properties));
    }

    @Override
    /** 使用接入点默认属性创建 Push 消费者。 */
    public PushConsumer createPushConsumer() {
        return new PushConsumerImpl(accessPointProperties);
    }

    @Override
    /** 合并属性后创建 Push 消费者。 */
    public PushConsumer createPushConsumer(KeyValue properties) {
        return new PushConsumerImpl(OMSUtil.buildKeyValue(this.accessPointProperties, properties));
    }

    @Override
    /** 使用接入点默认属性创建 Pull 消费者。 */
    public PullConsumer createPullConsumer() {
        return new PullConsumerImpl(accessPointProperties);
    }

    @Override
    /** 合并属性后创建 Pull 消费者。 */
    public PullConsumer createPullConsumer(KeyValue attributes) {
        return new PullConsumerImpl(OMSUtil.buildKeyValue(this.accessPointProperties, attributes));
    }

    @Override
    /** Streaming 消费者尚未实现。 */
    public StreamingConsumer createStreamingConsumer() {
        return null;
    }

    @Override
    /** Streaming 消费者尚未实现。 */
    public StreamingConsumer createStreamingConsumer(KeyValue attributes) {
        return null;
    }

    @Override
    /** 当前版本不支持 ResourceManager。 */
    public ResourceManager resourceManager() {
        throw new OMSNotSupportedException("-1", "ResourceManager is not supported in current version.");
    }

    @Override
    /** 接入点级启动（无状态，忽略）。 */
    public void startup() {
        //Ignore
    }

    @Override
    /** 接入点级关闭（无状态，忽略）。 */
    public void shutdown() {
        //Ignore
    }
}
