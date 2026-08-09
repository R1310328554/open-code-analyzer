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
 * $Id: HeartbeatData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.heartbeat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

import java.util.HashSet;
import java.util.Set;

/**
 * 客户端心跳数据：汇总生产者/消费者注册信息，支持 fingerprint 检测订阅变更。
 */
public class HeartbeatData extends RemotingSerializable {
    /** 客户端唯一标识。 */
    private String clientID;
    /** 已注册生产者集合。 */
    private Set<ProducerData> producerDataSet = new HashSet<>();
    /** 已注册消费者集合。 */
    private Set<ConsumerData> consumerDataSet = new HashSet<>();
    /** 心跳指纹（用于检测订阅是否变更）。 */
    private int heartbeatFingerprint = 0;
    /** 是否为无订阅信息的轻量心跳。 */
    private boolean isWithoutSub = false;

    /** 返回客户端 ID。 */
    public String getClientID() {
        return clientID;
    }

    /** 设置客户端 ID。 */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /** 返回生产者数据集合。 */
    public Set<ProducerData> getProducerDataSet() {
        return producerDataSet;
    }

    /** 设置生产者数据集合。 */
    public void setProducerDataSet(Set<ProducerData> producerDataSet) {
        this.producerDataSet = producerDataSet;
    }

    /** 返回消费者数据集合。 */
    public Set<ConsumerData> getConsumerDataSet() {
        return consumerDataSet;
    }

    /** 设置消费者数据集合。 */
    public void setConsumerDataSet(Set<ConsumerData> consumerDataSet) {
        this.consumerDataSet = consumerDataSet;
    }

    /** 返回心跳指纹。 */
    public int getHeartbeatFingerprint() {
        return heartbeatFingerprint;
    }

    /** 设置心跳指纹。 */
    public void setHeartbeatFingerprint(int heartbeatFingerprint) {
        this.heartbeatFingerprint = heartbeatFingerprint;
    }

    /** 返回是否为无订阅心跳。 */
    public boolean isWithoutSub() {
        return isWithoutSub;
    }

    /** 设置无订阅心跳标志。 */
    public void setWithoutSub(boolean withoutSub) {
        isWithoutSub = withoutSub;
    }

    /** 返回含 clientID 与生产/消费集合的调试字符串。 */
    @Override
    public String toString() {
        return "HeartbeatData [clientID=" + clientID + ", producerDataSet=" + producerDataSet
            + ", consumerDataSet=" + consumerDataSet + "]";
    }

    /** 计算心跳指纹：忽略 subVersion、clientID 等易变字段后取 JSON hashCode。 */
    public int computeHeartbeatFingerprint() {
        HeartbeatData heartbeatDataCopy = JSON.parseObject(JSON.toJSONString(this, JSONWriter.Feature.ReferenceDetection), HeartbeatData.class);
        for (ConsumerData consumerData : heartbeatDataCopy.getConsumerDataSet()) {
            for (SubscriptionData subscriptionData : consumerData.getSubscriptionDataSet()) {
                subscriptionData.setSubVersion(0L);
            }
        }
        heartbeatDataCopy.setWithoutSub(false);
        heartbeatDataCopy.setHeartbeatFingerprint(0);
        heartbeatDataCopy.setClientID("");
        return JSON.toJSONString(heartbeatDataCopy, JSONWriter.Feature.ReferenceDetection).hashCode();
    }
}
