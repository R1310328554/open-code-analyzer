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
package org.apache.rocketmq.client.hook;

/**
 * 发送消息钩子：在 Producer 向 Broker 发送消息前后插入自定义逻辑，
 * 常用于监控、限流、消息轨迹与审计。
 */
public interface SendMessageHook {
    /** 返回钩子唯一名称。 */
    String hookName();

    /** 发送请求发出前回调。 */
    void sendMessageBefore(final SendMessageContext context);

    /** 发送完成后回调（含结果或异常）。 */
    void sendMessageAfter(final SendMessageContext context);
}
