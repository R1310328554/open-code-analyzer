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

import com.alibaba.fastjson2.JSON;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消息发送结果：包含发送状态、msgId、目标队列、偏移量及事务/追踪等扩展字段。
 */
public class SendResult {
    /** 发送状态（OK、刷盘超时等）。 */
    private SendStatus sendStatus;
    /** Broker 生成的全局消息 ID。 */
    private String msgId;
    /** 消息写入的目标队列。 */
    private MessageQueue messageQueue;
    /** 消息在队列中的逻辑偏移量。 */
    private long queueOffset;
    /** 事务消息 ID（非事务消息可为 null）。 */
    private String transactionId;
    /** 基于 host+offset 的物理消息 ID。 */
    private String offsetMsgId;
    /** 消息所属区域 ID。 */
    private String regionId;
    /** 是否开启消息轨迹追踪。 */
    private boolean traceOn = true;
    /** Broker 原始响应体（扩展场景）。 */
    private byte[] rawRespBody;
    /** 消息撤回句柄。 */
    private String recallHandle;

    /** 无参构造，供 JSON 反序列化等使用。 */
    public SendResult() {
    }

    public SendResult(SendStatus sendStatus, String msgId, String offsetMsgId, MessageQueue messageQueue,
        long queueOffset) {
        this.sendStatus = sendStatus;
        this.msgId = msgId;
        this.offsetMsgId = offsetMsgId;
        this.messageQueue = messageQueue;
        this.queueOffset = queueOffset;
    }

    public SendResult(final SendStatus sendStatus, final String msgId, final MessageQueue messageQueue,
        final long queueOffset, final String transactionId,
        final String offsetMsgId, final String regionId) {
        this.sendStatus = sendStatus;
        this.msgId = msgId;
        this.messageQueue = messageQueue;
        this.queueOffset = queueOffset;
        this.transactionId = transactionId;
        this.offsetMsgId = offsetMsgId;
        this.regionId = regionId;
    }

    /** 将 SendResult 序列化为 JSON 字符串。 */
    public static String encoderSendResultToJson(final Object obj) {
        return JSON.toJSONString(obj);
    }

    /** 从 JSON 字符串反序列化为 SendResult。 */
    public static SendResult decoderSendResultFromJson(String json) {
        return JSON.parseObject(json, SendResult.class);
    }

    /** 是否开启轨迹追踪。 */
    public boolean isTraceOn() {
        return traceOn;
    }

    /** 设置是否开启轨迹追踪。 */
    public void setTraceOn(final boolean traceOn) {
        this.traceOn = traceOn;
    }

    /** 返回区域 ID。 */
    public String getRegionId() {
        return regionId;
    }

    /** 设置区域 ID。 */
    public void setRegionId(final String regionId) {
        this.regionId = regionId;
    }

    /** 返回全局 msgId。 */
    public String getMsgId() {
        return msgId;
    }

    /** 设置全局 msgId。 */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /** 返回发送状态。 */
    public SendStatus getSendStatus() {
        return sendStatus;
    }

    /** 设置发送状态。 */
    public void setSendStatus(SendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    /** 返回目标 MessageQueue。 */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    /** 设置目标 MessageQueue。 */
    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    /** 返回队列偏移量。 */
    public long getQueueOffset() {
        return queueOffset;
    }

    /** 设置队列偏移量。 */
    public void setQueueOffset(long queueOffset) {
        this.queueOffset = queueOffset;
    }

    /** 返回事务 ID。 */
    public String getTransactionId() {
        return transactionId;
    }

    /** 设置事务 ID。 */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /** 返回物理 offsetMsgId。 */
    public String getOffsetMsgId() {
        return offsetMsgId;
    }

    /** 设置物理 offsetMsgId。 */
    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    /** 返回撤回句柄。 */
    public String getRecallHandle() {
        return recallHandle;
    }

    /** 设置撤回句柄。 */
    public void setRecallHandle(String recallHandle) {
        this.recallHandle = recallHandle;
    }

    @Override
    public String toString() {
        return "SendResult [sendStatus=" + sendStatus + ", msgId=" + msgId + ", offsetMsgId=" + offsetMsgId + ", messageQueue=" + messageQueue
            + ", queueOffset=" + queueOffset + ", recallHandle=" + recallHandle + "]";
    }

    /** 设置 Broker 原始响应体。 */
    public void setRawRespBody(byte[] body) {
        this.rawRespBody = body;
    }

    /** 返回 Broker 原始响应体。 */
    public byte[] getRawRespBody() {
        return rawRespBody;
    }
}
