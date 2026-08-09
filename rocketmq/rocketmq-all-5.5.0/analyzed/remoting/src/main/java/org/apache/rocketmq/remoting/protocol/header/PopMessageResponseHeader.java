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
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * Pop 消息响应头：返回 Pop 时间、不可见时长、队列剩余消息数及偏移信息。
 */
public class PopMessageResponseHeader implements CommandCustomHeader {


    /** Pop 操作时间戳（毫秒）。 */
    @CFNotNull
    private long popTime;
    /** 消息不可见时长（毫秒）。 */
    @CFNotNull
    private long invisibleTime;

    /** 复活队列 ID。 */
    @CFNotNull
    private int reviveQid;
    /** 队列中剩余可 Pop 消息数。 */
    @CFNotNull
    private long restNum;

    /** 起始偏移量信息（序列化字符串）。 */
    private String startOffsetInfo;
    /** 消息偏移量信息（序列化字符串）。 */
    private String msgOffsetInfo;
    /** 顺序消费计数信息（序列化字符串）。 */
    private String orderCountInfo;

    /** 校验响应头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public long getPopTime() {
        return popTime;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    public long getRestNum() {
        return restNum;
    }

    public void setRestNum(long restNum) {
        this.restNum = restNum;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public int getReviveQid() {
        return reviveQid;
    }

    public void setReviveQid(int reviveQid) {
        this.reviveQid = reviveQid;
    }

    public String getStartOffsetInfo() {
        return startOffsetInfo;
    }

    public void setStartOffsetInfo(String startOffsetInfo) {
        this.startOffsetInfo = startOffsetInfo;
    }

    public String getMsgOffsetInfo() {
        return msgOffsetInfo;
    }

    public void setMsgOffsetInfo(String msgOffsetInfo) {
        this.msgOffsetInfo = msgOffsetInfo;
    }

    public String getOrderCountInfo() {
        return orderCountInfo;
    }

    public void setOrderCountInfo(String orderCountInfo) {
        this.orderCountInfo = orderCountInfo;
    }
}
