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
package org.apache.rocketmq.client.producer;

/**
 * 同步发送返回的状态码：表示消息是否成功写入及副本/刷盘是否满足 SLA。
 */
public enum SendStatus {
    /** 发送成功，刷盘与副本均正常。 */
    SEND_OK,
    /** 消息已写入但同步刷盘超时。 */
    FLUSH_DISK_TIMEOUT,
    /** 消息已写入主节点但同步到从节点超时。 */
    FLUSH_SLAVE_TIMEOUT,
    /** 从节点不可用，无法完成同步复制。 */
    SLAVE_NOT_AVAILABLE,
}
