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

/**
 * $Id: SendMessageResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.FastCodesHeader;

/**
 * 发送消息的响应头：返回 msgId、队列位点及事务/批量/撤回相关标识。
 * 实现 FastCodesHeader 以支持高效编解码。
 */
public class SendMessageResponseHeader implements CommandCustomHeader, FastCodesHeader {
    /** 服务端分配的消息唯一 ID。 */
    @CFNotNull
    private String msgId;
    /** 消息写入的队列 ID。 */
    @CFNotNull
    private Integer queueId;
    /** 消息在队列中的逻辑 offset。 */
    @CFNotNull
    private Long queueOffset;
    /** 事务消息 ID，非事务消息可为空。 */
    private String transactionId;
    /** 批量消息唯一标识，可为空。 */
    private String batchUniqId;
    /** 消息撤回句柄，用于后续 recall 操作，可为空。 */
    private String recallHandle;

    /** 校验响应头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 将响应字段编码写入 ByteBuf。 */
    @Override
    public void encode(ByteBuf out) {
        writeIfNotNull(out, "msgId", msgId);
        writeIfNotNull(out, "queueId", queueId);
        writeIfNotNull(out, "queueOffset", queueOffset);
        writeIfNotNull(out, "transactionId", transactionId);
        writeIfNotNull(out, "batchUniqId", batchUniqId);
        writeIfNotNull(out, "recallHandle", recallHandle);
    }

    /** 从字段映射解码响应头各字段。 */
    @Override
    public void decode(HashMap<String, String> fields) throws RemotingCommandException {
        String str = getAndCheckNotNull(fields, "msgId");
        if (str != null) {
            this.msgId = str;
        }

        str = getAndCheckNotNull(fields, "queueId");
        if (str != null) {
            this.queueId = Integer.parseInt(str);
        }

        str = getAndCheckNotNull(fields, "queueOffset");
        if (str != null) {
            this.queueOffset = Long.parseLong(str);
        }

        str = fields.get("transactionId");
        if (str != null) {
            this.transactionId = str;
        }

        str = fields.get("batchUniqId");
        if (str != null) {
            this.batchUniqId = str;
        }

        str = fields.get("recallHandle");
        if (str != null) {
            this.recallHandle = str;
        }
    }

    /** 返回消息 ID。 */
    public String getMsgId() {
        return msgId;
    }

    /** 设置消息 ID。 */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /** 返回队列 ID。 */
    public Integer getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    /** 返回队列 offset。 */
    public Long getQueueOffset() {
        return queueOffset;
    }

    /** 设置队列 offset。 */
    public void setQueueOffset(Long queueOffset) {
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

    /** 返回批量唯一 ID。 */
    public String getBatchUniqId() {
        return batchUniqId;
    }

    /** 设置批量唯一 ID。 */
    public void setBatchUniqId(String batchUniqId) {
        this.batchUniqId = batchUniqId;
    }

    /** 返回撤回句柄。 */
    public String getRecallHandle() {
        return recallHandle;
    }

    /** 设置撤回句柄。 */
    public void setRecallHandle(String recallHandle) {
        this.recallHandle = recallHandle;
    }
}
