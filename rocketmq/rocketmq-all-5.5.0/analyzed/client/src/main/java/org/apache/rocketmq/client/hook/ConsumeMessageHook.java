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
 * 消费消息钩子：在 Push/Pull 消费者执行业务监听器前后插入自定义逻辑，
 * 常用于监控、审计与消息轨迹上报。
 */
public interface ConsumeMessageHook {
    /** 返回钩子唯一名称。 */
    String hookName();

    /** 消费监听器执行前回调。 */
    void consumeMessageBefore(final ConsumeMessageContext context);

    /** 消费监听器执行后回调（含成功/失败状态）。 */
    void consumeMessageAfter(final ConsumeMessageContext context);
}
