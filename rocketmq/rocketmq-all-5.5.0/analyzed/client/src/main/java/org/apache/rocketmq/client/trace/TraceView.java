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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 轨迹视图：从轨迹 Topic 消息体解码出的单条轨迹展示模型，
 * 供控制台或查询工具按 msgId 过滤并展示发送/消费链路。
 */
public class TraceView {

    /** 消息 ID。 */
    private String msgId;
    private String tags;
    private String keys;
    private String storeHost;
    private String clientHost;
    private int costTime;
    private String msgType;
    private String offSetMsgId;
    private long timeStamp;
    private long bornTime;
    private String topic;
    private String groupName;
    /** 轨迹状态（success / failed）。 */
    private String status;

    /**
     * 从轨迹 Topic 的 {@link MessageExt} 中解码与指定 key 匹配的轨迹视图列表。
     *
     * @param key 目标 msgId
     * @param messageExt 轨迹 Topic 消息
     * @return 匹配的轨迹视图列表
     */
        List<TraceView> messageTraceViewList = new ArrayList<>();
        String messageBody = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        if (messageBody == null || messageBody.length() <= 0) {
            return messageTraceViewList;
        }

        List<TraceContext> traceContextList = TraceDataEncoder.decoderFromTraceDataString(messageBody);

        for (TraceContext context : traceContextList) {
            TraceView messageTraceView = new TraceView();
            TraceBean traceBean = context.getTraceBeans().get(0);
            if (!traceBean.getMsgId().equals(key)) {
                continue;
            }
            messageTraceView.setCostTime(context.getCostTime());
            messageTraceView.setGroupName(context.getGroupName());
            if (context.isSuccess()) {
                messageTraceView.setStatus("success");
            } else {
                messageTraceView.setStatus("failed");
            }
            messageTraceView.setKeys(traceBean.getKeys());
            messageTraceView.setMsgId(traceBean.getMsgId());
            messageTraceView.setTags(traceBean.getTags());
            messageTraceView.setTopic(traceBean.getTopic());
            messageTraceView.setMsgType(context.getTraceType().name());
            messageTraceView.setOffSetMsgId(traceBean.getOffsetMsgId());
            messageTraceView.setTimeStamp(context.getTimeStamp());
            messageTraceView.setStoreHost(traceBean.getStoreHost());
            messageTraceView.setClientHost(messageExt.getBornHostString());
            messageTraceViewList.add(messageTraceView);
        }
        return messageTraceViewList;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getStoreHost() {
        return storeHost;
    }

    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getOffSetMsgId() {
        return offSetMsgId;
    }

    public void setOffSetMsgId(String offSetMsgId) {
        this.offSetMsgId = offSetMsgId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getBornTime() {
        return bornTime;
    }

    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}