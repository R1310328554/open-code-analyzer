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

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/** POP 拉取操作的结果封装。 */
public class PopResult {
    /** 拉取到的消息列表。 */
    private List<MessageExt> msgFoundList;
    /** Pop 状态。 */
    private PopStatus popStatus;
    /** Pop 操作时间戳。 */
    private long popTime;
    /** 消息不可见时长（毫秒）。 */
    private long invisibleTime;
    /** 队列中剩余可 Pop 消息数。 */
    private long restNum;

    /** 构造 Pop 结果。 */
    public PopResult(PopStatus popStatus, List<MessageExt> msgFoundList) {
        this.popStatus = popStatus;
        this.msgFoundList = msgFoundList;
    }

    /** 获取 Pop 时间戳。 */
    public long getPopTime() {
        return popTime;
    }


    /** 设置 Pop 时间戳。 */
    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    /** 获取剩余可 Pop 数量。 */
    public long getRestNum() {
        return restNum;
    }

    /** 设置剩余可 Pop 数量。 */
    public void setRestNum(long restNum) {
        this.restNum = restNum;
    }

    /** 获取不可见时长。 */
    public long getInvisibleTime() {
        return invisibleTime;
    }


    /** 设置不可见时长。 */
    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }


    /** 设置 Pop 状态。 */
    public void setPopStatus(PopStatus popStatus) {
        this.popStatus = popStatus;
    }

    /** 获取 Pop 状态。 */
    public PopStatus getPopStatus() {
        return popStatus;
    }

    /** 获取消息列表。 */
    public List<MessageExt> getMsgFoundList() {
        return msgFoundList;
    }

    /** 设置消息列表。 */
    public void setMsgFoundList(List<MessageExt> msgFoundList) {
        this.msgFoundList = msgFoundList;
    }

    @Override
    public String toString() {
        return "PopResult [popStatus=" + popStatus + ",msgFoundList="
            + (msgFoundList == null ? 0 : msgFoundList.size()) + ",restNum=" + restNum + "]";
    }
}
