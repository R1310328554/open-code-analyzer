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

package org.apache.rocketmq.broker.longpolling;

/**
 * POP 长轮询挂起结果：标识请求是否成功进入等待队列或遭拒绝/超时。
 */
public enum PollingResult {
    /** 成功挂起，等待新消息到达后唤醒。 */
    POLLING_SUC,
    /** 轮询队列已满，拒绝挂起。 */
    POLLING_FULL,
    /** 挂起前已超时，直接返回。 */
    POLLING_TIMEOUT,
    /** 未进入长轮询（pollTime 无效或服务已停止）。 */
    NOT_POLLING;
}
