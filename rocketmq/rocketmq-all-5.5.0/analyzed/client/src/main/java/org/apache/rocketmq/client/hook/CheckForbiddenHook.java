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

import org.apache.rocketmq.client.exception.MQClientException;

/**
 * 发送禁发校验钩子：在 Producer 发送消息前检查是否允许发往指定 Topic/队列，
 * 不满足策略时抛出 {@link MQClientException} 阻断发送。
 */
public interface CheckForbiddenHook {
    /** 返回钩子唯一名称，用于注册与排查。 */
    String hookName();

    /** 执行禁发校验；不允许发送时抛出 {@link MQClientException}。 */
    void checkForbidden(final CheckForbiddenContext context) throws MQClientException;
}
