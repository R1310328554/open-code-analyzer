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

package org.apache.rocketmq.proxy.service.message;

import org.apache.rocketmq.common.consumer.ReceiptHandle;

/**
 * Pop 消费回执与 messageId 的绑定封装，用于批量 Ack。
 */
public class ReceiptHandleMessage {

    /** Pop 消费回执句柄。 */
    private final ReceiptHandle receiptHandle;
    /** 关联的消息 ID。 */
    private final String messageId;

    /** @param receiptHandle Pop 回执 @param messageId 消息 ID */
    public ReceiptHandleMessage(ReceiptHandle receiptHandle, String messageId) {
        this.receiptHandle = receiptHandle;
        this.messageId = messageId;
    }

    /** 返回 Pop 回执句柄。 */
    public ReceiptHandle getReceiptHandle() {
        return receiptHandle;
    }

    /** 返回消息 ID。 */
    public String getMessageId() {
        return messageId;
    }
}
