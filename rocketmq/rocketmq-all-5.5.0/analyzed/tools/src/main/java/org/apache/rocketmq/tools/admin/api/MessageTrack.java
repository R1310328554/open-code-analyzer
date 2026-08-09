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

package org.apache.rocketmq.tools.admin.api;

/**
 * 消息轨迹查询结果：描述某消费组对指定消息的投递/消费状态。
 */
public class MessageTrack {
    /** 消费组名称。 */
    private String consumerGroup;
    /** 轨迹类型（已消费、过滤、未消费等）。 */
    private TrackType trackType;
    /** 异常描述（轨迹异常时非空）。 */
    private String exceptionDesc;

    /** 返回消费组名称。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组名称。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回轨迹类型。 */
    public TrackType getTrackType() {
        return trackType;
    }

    /** 设置轨迹类型。 */
    public void setTrackType(TrackType trackType) {
        this.trackType = trackType;
    }

    /** 返回异常描述。 */
    public String getExceptionDesc() {
        return exceptionDesc;
    }

    /** 设置异常描述。 */
    public void setExceptionDesc(String exceptionDesc) {
        this.exceptionDesc = exceptionDesc;
    }

    @Override
    public String toString() {
        return "MessageTrack [consumerGroup=" + consumerGroup + ", trackType=" + trackType
            + ", exceptionDesc=" + exceptionDesc + "]";
    }
}
