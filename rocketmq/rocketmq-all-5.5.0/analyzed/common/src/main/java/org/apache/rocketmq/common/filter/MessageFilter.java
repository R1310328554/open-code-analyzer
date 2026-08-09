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

package org.apache.rocketmq.common.filter;

import org.apache.rocketmq.common.message.MessageExt;

/**
 * 消息过滤 SPI：Broker/Consumer 按订阅表达式判定消息是否匹配。
 */
public interface MessageFilter {
    /**
     * 判断消息是否通过过滤。
     *
     * @param msg 待检消息
     * @param context 过滤上下文（含消费组等）
     * @return true 表示匹配订阅条件
     */
    boolean match(final MessageExt msg, final FilterContext context);
}
