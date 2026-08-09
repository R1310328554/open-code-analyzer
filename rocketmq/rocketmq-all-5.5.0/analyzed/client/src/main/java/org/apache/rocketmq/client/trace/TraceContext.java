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

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.common.message.MessageClientIDSetter;

import java.util.List;

/**
 * 消息轨迹上下文：记录一次轨迹事件（发送/消费/事务等）的元数据与关联 {@link TraceBean} 列表。
 * 按时间戳排序，用于异步上报与轨迹查询。
 */
public class TraceContext implements Comparable<TraceContext> {

    /** 轨迹类型（发布、消费前/后、事务结束、撤回等）。 */
    private TraceType traceType;
    /** 事件发生时间戳（毫秒）。 */
    private long timeStamp = System.currentTimeMillis();
    /** 轨迹所属区域 ID。 */
    private String regionId = "";
    /** 轨迹所属区域名称。 */
    private String regionName = "";
    /** Producer 或 Consumer 组名（已去除命名空间）。 */
    private String groupName = "";
    /** 操作耗时（毫秒）。 */
    private int costTime = 0;
    /** 操作是否成功。 */
    private boolean isSuccess = true;
    /** 消费前后轨迹关联用的请求 ID。 */
    private String requestId = MessageClientIDSetter.createUniqID();
    /** 消费返回类型编码（对应 {@link org.apache.rocketmq.client.consumer.listener.ConsumeReturnType}）。 */
    private int contextCode = 0;
    /** 访问通道（本地/云等），影响轨迹编码格式。 */
    private AccessChannel accessChannel;
    /** 本上下文关联的消息轨迹明细列表。 */
    private List<TraceBean> traceBeans;

    public int getContextCode() {
        return contextCode;
    }

    public void setContextCode(final int contextCode) {
        this.contextCode = contextCode;
    }

    public List<TraceBean> getTraceBeans() {
        return traceBeans;
    }

    public void setTraceBeans(List<TraceBean> traceBeans) {
        this.traceBeans = traceBeans;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public AccessChannel getAccessChannel() {
        return accessChannel;
    }

    public void setAccessChannel(AccessChannel accessChannel) {
        this.accessChannel = accessChannel;
    }

    @Override
    /** 按时间戳升序比较，用于轨迹排序。 */
    public int compareTo(TraceContext o) {
        return Long.compare(this.timeStamp, o.getTimeStamp());
    }

    @Override
    /** 拼接轨迹类型、组名、区域及消息摘要的调试字符串。 */
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("TraceContext{").append(traceType).append("_").append(groupName).append("_")
            .append(regionId).append("_").append(isSuccess).append("_");
        if (traceBeans != null && traceBeans.size() > 0) {
            for (TraceBean bean : traceBeans) {
                sb.append(bean.getMsgId()).append("_").append(bean.getTopic()).append("_");
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
