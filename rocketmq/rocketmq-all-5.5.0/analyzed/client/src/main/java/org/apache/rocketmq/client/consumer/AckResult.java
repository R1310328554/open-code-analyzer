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
package org.apache.rocketmq.client.consumer;


/** POP 消息 Ack 操作的结果封装。 */
public class AckResult {
    /** Ack 状态。 */
    private AckStatus status;
    /** 附加信息（如错误详情）。 */
    private String extraInfo;
    /** Pop 操作时间戳。 */
    private long popTime;

    /** 设置 Pop 时间戳。 */
    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    /** 获取 Pop 时间戳。 */
    public long getPopTime() {
        return popTime;
    }

    /** 获取 Ack 状态。 */
    public AckStatus getStatus() {
        return status;
    }

    /** 设置 Ack 状态。 */
    public void setStatus(AckStatus status) {
        this.status = status;
    }

    /** 设置附加信息。 */
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    /** 获取附加信息。 */
    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public String toString() {
        return "AckResult [AckStatus=" + status + ",extraInfo=" + extraInfo + "]";
    }
}
