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

package org.apache.rocketmq.proxy.processor;

import org.apache.rocketmq.client.consumer.AckResult;
import org.apache.rocketmq.proxy.common.ProxyException;
import org.apache.rocketmq.proxy.service.message.ReceiptHandleMessage;

/**
 * 批量 ACK 单条结果：封装回执消息与 ACK 结果或 Proxy 异常。
 */
public class BatchAckResult {

    /** 对应的回执句柄消息。 */
    private final ReceiptHandleMessage receiptHandleMessage;
    /** ACK 成功时的客户端 AckResult。 */
    private AckResult ackResult;
    /** ACK 失败时的 Proxy 异常。 */
    private ProxyException proxyException;

    /** 构造成功的批量 ACK 结果。 */
    public BatchAckResult(ReceiptHandleMessage receiptHandleMessage,
        AckResult ackResult) {
        this.receiptHandleMessage = receiptHandleMessage;
        this.ackResult = ackResult;
    }

    /** 构造失败的批量 ACK 结果。 */
    public BatchAckResult(ReceiptHandleMessage receiptHandleMessage,
        ProxyException proxyException) {
        this.receiptHandleMessage = receiptHandleMessage;
        this.proxyException = proxyException;
    }

    /** 返回关联的回执句柄消息。 */
    public ReceiptHandleMessage getReceiptHandleMessage() {
        return receiptHandleMessage;
    }

    /** 返回 ACK 结果，失败时为 null。 */
    public AckResult getAckResult() {
        return ackResult;
    }

    /** 返回 Proxy 异常，成功时为 null。 */
    public ProxyException getProxyException() {
        return proxyException;
    }
}
