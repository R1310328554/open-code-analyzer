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

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 事务消息监听器：半消息发送成功后执行本地事务，
 * 并在 Broker 回查时返回本地事务状态。
 */
public interface TransactionListener {
    /**
     * 半消息发送成功后执行本地事务。
     *
     * @param msg 半消息（Prepare）
     * @param arg 业务自定义参数
     * @return 本地事务状态
     */
    LocalTransactionState executeLocalTransaction(final Message msg, final Object arg);

    /**
     * Broker 未收到提交/回滚时发起回查，此方法返回本地事务状态。
     *
     * @param msg 回查消息
     * @return 本地事务状态
     */
    LocalTransactionState checkLocalTransaction(final MessageExt msg);
}