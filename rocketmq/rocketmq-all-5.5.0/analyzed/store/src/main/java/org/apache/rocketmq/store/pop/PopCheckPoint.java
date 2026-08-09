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
package org.apache.rocketmq.store.pop;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

/**
 * Pop 消费检查点：跟踪 invisible 窗口、位图 ack 状态及 revive 偏移。
 */
public class PopCheckPoint implements Comparable<PopCheckPoint> {
    /** 检查点覆盖的起始队列偏移。 */
    @JSONField(name = "so")
    private long startOffset;
    /** Pop 操作时间戳。 */
    @JSONField(name = "pt")
    private long popTime;
    /** 消息不可见时长（毫秒）。 */
    @JSONField(name = "it")
    private long invisibleTime;
    /** 位图：标记各偏移是否已 ack。 */
    @JSONField(name = "bm")
    private int bitMap;
    /** 本检查点覆盖的消息条数。 */
    @JSONField(name = "n")
    private byte num;
    /** 队列 ID。 */
    @JSONField(name = "q")
    private int queueId;
    /** 主题名。 */
    @JSONField(name = "t")
    private String topic;
    /** 消费者标识（consumer id）。 */
    private String cid;
    /** Revive 队列中的偏移位置。 */
    @JSONField(name = "ro")
    private long reviveOffset;
    /** 新版检查点：相对 startOffset 的偏移差值列表。 */
    @JSONField(name = "d")
    private List<Integer> queueOffsetDiff;
    /** Broker 名称。 */
    @JSONField(name = "bn")
    String brokerName;
    /** 检查点重新投递次数。 */
    @JSONField(name = "rp")
    String rePutTimes; // ck rePut times
    /** 是否挂起（nack 时不增加重试次数，默认 false）。 */
    @JSONField(name = "sp")
    private boolean suspend; // nack without inc reconsume times, false default.

    public long getReviveOffset() {
        return reviveOffset;
    }

    public void setReviveOffset(long reviveOffset) {
        this.reviveOffset = reviveOffset;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public void setPopTime(long popTime) {
        this.popTime = popTime;
    }

    public void setInvisibleTime(long invisibleTime) {
        this.invisibleTime = invisibleTime;
    }

    public long getPopTime() {
        return popTime;
    }

    public long getInvisibleTime() {
        return invisibleTime;
    }

    /** 返回 revive 触发时间（popTime + invisibleTime）。 */
    public long getReviveTime() {
        return popTime + invisibleTime;
    }

    public int getBitMap() {
        return bitMap;
    }

    public void setBitMap(int bitMap) {
        this.bitMap = bitMap;
    }

    public byte getNum() {
        return num;
    }

    public void setNum(byte num) {
        this.num = num;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @JSONField(name = "c")
    public String getCId() {
        return cid;
    }

    @JSONField(name = "c")
    public void setCId(String cid) {
        this.cid = cid;
    }

    public List<Integer> getQueueOffsetDiff() {
        return queueOffsetDiff;
    }

    public void setQueueOffsetDiff(List<Integer> queueOffsetDiff) {
        this.queueOffsetDiff = queueOffsetDiff;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getRePutTimes() {
        return rePutTimes;
    }

    public void setRePutTimes(String rePutTimes) {
        this.rePutTimes = rePutTimes;
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void setSuspend(boolean suspend) {
        this.suspend = suspend;
    }

    /** 追加一条相对 startOffset 的偏移差值。 */
    public void addDiff(int diff) {
        if (this.queueOffsetDiff == null) {
            this.queueOffsetDiff = new ArrayList<>(8);
        }
        this.queueOffsetDiff.add(diff);
    }

    /** 根据 ack 偏移查找在位图/差值列表中的索引，未找到返回 -1。 */
    public int indexOfAck(long ackOffset) {
        if (ackOffset < startOffset) {
            return -1;
        }

        // 旧版检查点：按连续偏移计算索引
        if (queueOffsetDiff == null || queueOffsetDiff.isEmpty()) {

            if (ackOffset - startOffset < num) {
                return (int) (ackOffset - startOffset);
            }

            return -1;
        }

        // 新版检查点：在 queueOffsetDiff 中查找
        return queueOffsetDiff.indexOf((int) (ackOffset - startOffset));
    }

    /** 根据索引反查对应的 ack 队列偏移。 */
    public long ackOffsetByIndex(byte index) {
        // old version of checkpoint
        if (queueOffsetDiff == null || queueOffsetDiff.isEmpty()) {
            return startOffset + index;
        }

        return startOffset + queueOffsetDiff.get(index);
    }

    /** 解析 rePutTimes 字符串为整数，失败时返回 Byte.MAX_VALUE。 */
    public int parseRePutTimes() {
        if (null == rePutTimes) {
            return 0;
        }
        try {
            return Integer.parseInt(rePutTimes);
        } catch (Exception e) {
        }
        return Byte.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "PopCheckPoint [topic=" + topic + ", cid=" + cid + ", queueId=" + queueId + ", startOffset=" + startOffset + ", bitMap=" + bitMap + ", num=" + num + ", reviveTime=" + getReviveTime()
            + ", reviveOffset=" + reviveOffset + ", diff=" + queueOffsetDiff + ", brokerName=" + brokerName + ", rePutTimes=" + rePutTimes + ", suspend=" + suspend + "]";
    }

    /** 按 startOffset 升序比较两个检查点。 */
    @Override
    public int compareTo(PopCheckPoint o) {
        return (int) (this.getStartOffset() - o.getStartOffset());
    }
}
