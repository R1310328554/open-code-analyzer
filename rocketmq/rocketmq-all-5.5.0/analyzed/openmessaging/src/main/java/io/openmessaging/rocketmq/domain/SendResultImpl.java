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

import io.openmessaging.KeyValue;
import io.openmessaging.producer.SendResult;

/**
 * OMS 发送结果实现：封装 Broker 返回的消息 ID 与扩展属性。
 */
public class SendResultImpl implements SendResult {
    /** 消息唯一标识，对应 RocketMQ msgId。 */
    private String messageId;
    /** 发送结果附加属性键值对。 */
    private KeyValue properties;

    /** 构造发送结果实例。 */
    public SendResultImpl(final String messageId, final KeyValue properties) {
        this.messageId = messageId;
        this.properties = properties;
    }

    /** 返回消息 ID。 */
    @Override
    public String messageId() {
        return messageId;
    }

    /** 返回结果扩展属性。 */
    public KeyValue properties() {
        return properties;
    }
}
