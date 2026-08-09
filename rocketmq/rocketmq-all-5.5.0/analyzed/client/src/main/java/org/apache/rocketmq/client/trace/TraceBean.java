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
package org.apache.rocketmq.client.trace;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageType;

/**
 * 消息轨迹单条记录的数据载体：封装 topic、msgId、主机、事务状态等
 * 写入轨迹 Topic 所需的字段。
 */
public class TraceBean {
    /** 本机 IP 字符串（IPv4 或 IPv6）。 */
    private static final String LOCAL_ADDRESS;
    /** 消息 Topic。 */
    private String topic = "";
    /** 全局消息 ID。 */
    private String msgId = "";
    /** 物理 offset 消息 ID。 */
    private String offsetMsgId = "";
    /** 消息 Tag。 */
    private String tags = "";
    /** 消息 Keys。 */
    private String keys = "";
    /** 消息存储 Broker 主机地址。 */
    private String storeHost = LOCAL_ADDRESS;
    /** 客户端主机地址。 */
    private String clientHost = LOCAL_ADDRESS;
    /** 消息存储时间戳。 */
    private long storeTime;
    /** 消费重试次数。 */
    private int retryTimes;
    /** 消息体字节长度。 */
    private int bodyLength;
    /** 消息类型（普通/事务/延迟等）。 */
    private MessageType msgType;
    /** 本地事务状态。 */
    private LocalTransactionState transactionState;
    /** 事务消息 ID。 */
    private String transactionId;
    /** 是否来自 Broker 事务回查。 */
    private boolean fromTransactionCheck;

    /** 静态初始化本机 IP 地址字符串。 */
    static {
        byte[] ip = UtilAll.getIP();
        if (ip.length == 4) {
            LOCAL_ADDRESS = UtilAll.ipToIPv4Str(ip);
        } else {
            LOCAL_ADDRESS = UtilAll.ipToIPv6Str(ip);
        }
    }

    /** 返回消息类型。 */
    public MessageType getMsgType() {
        return msgType;
    }


    /** 设置消息类型。 */
    public void setMsgType(final MessageType msgType) {
        this.msgType = msgType;
    }


    /** 返回 offsetMsgId。 */
    public String getOffsetMsgId() {
        return offsetMsgId;
    }


    /** 设置 offsetMsgId。 */
    public void setOffsetMsgId(final String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    /** 返回 Topic。 */
    public String getTopic() {
        return topic;
    }


    /** 设置 Topic。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }


    /** 返回 msgId。 */
    public String getMsgId() {
        return msgId;
    }


    /** 设置 msgId。 */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }


    /** 返回 Tag。 */
    public String getTags() {
        return tags;
    }


    /** 设置 Tag。 */
    public void setTags(String tags) {
        this.tags = tags;
    }


    /** 返回 Keys。 */
    public String getKeys() {
        return keys;
    }


    /** 设置 Keys。 */
    public void setKeys(String keys) {
        this.keys = keys;
    }


    /** 返回存储主机。 */
    public String getStoreHost() {
        return storeHost;
    }


    /** 设置存储主机。 */
    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }


    /** 返回客户端主机。 */
    public String getClientHost() {
        return clientHost;
    }


    /** 设置客户端主机。 */
    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }


    /** 返回存储时间。 */
    public long getStoreTime() {
        return storeTime;
    }


    /** 设置存储时间。 */
    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }


    /** 返回重试次数。 */
    public int getRetryTimes() {
        return retryTimes;
    }


    /** 设置重试次数。 */
    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }


    /** 返回消息体长度。 */
    public int getBodyLength() {
        return bodyLength;
    }


    /** 设置消息体长度。 */
    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    /** 返回事务状态。 */
    public LocalTransactionState getTransactionState() {
        return transactionState;
    }

    /** 设置事务状态。 */
    public void setTransactionState(LocalTransactionState transactionState) {
        this.transactionState = transactionState;
    }

    /** 返回事务 ID。 */
    public String getTransactionId() {
        return transactionId;
    }

    /** 设置事务 ID。 */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /** 是否来自事务回查。 */
    public boolean isFromTransactionCheck() {
        return fromTransactionCheck;
    }

    /** 标记是否来自事务回查。 */
    public void setFromTransactionCheck(boolean fromTransactionCheck) {
        this.fromTransactionCheck = fromTransactionCheck;
    }
}
