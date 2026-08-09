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
package org.apache.rocketmq.store.timer;

import org.apache.rocketmq.common.message.MessageExt;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 定时消息处理请求：封装物理偏移、延迟时间与同步门闩。
 */
public class TimerRequest {

    /** CommitLog 物理偏移。 */
    private final long offsetPy;
    /** CommitLog 消息大小。 */
    private final int sizePy;
    /** 延迟触发时间。 */
    private final long delayTime;

    /** Timer 单元魔数/类型标识。 */
    private final int magic;

    /** 入队时间戳。 */
    private long enqueueTime;
    /** 关联的消息体（可选）。 */
    private MessageExt msg;


    //optional would be a good choice, but it relies on JDK 8
    /** 异步完成同步门闩。 */
    private CountDownLatch latch;

    /** 是否已释放门闩。 */
    private boolean released;

    //whether the operation is successful
    /** 处理是否成功。 */
    private boolean succ;

    /** 待删除 Topic 集合（可选）。 */
    private Set<String> deleteList;

        /** 构造无消息体的 Timer 请求。 */
    public TimerRequest(long offsetPy, int sizePy, long delayTime, long enqueueTime, int magic) {
        this(offsetPy, sizePy, delayTime, enqueueTime, magic, null);
    }

        /** 构造带消息体的 Timer 请求。 */
    public TimerRequest(long offsetPy, int sizePy, long delayTime, long enqueueTime, int magic, MessageExt msg) {
        this.offsetPy = offsetPy;
        this.sizePy = sizePy;
        this.delayTime = delayTime;
        this.enqueueTime = enqueueTime;
        this.magic = magic;
        this.msg = msg;
    }

    /** CommitLog 物理偏移。 */
    public long getOffsetPy() {
        return offsetPy;
    }

    /** 消息体大小。 */
    public int getSizePy() {
        return sizePy;
    }

    /** 延迟时间。 */
    public long getDelayTime() {
        return delayTime;
    }

    /** 入队时间。 */
    public long getEnqueueTime() {
        return enqueueTime;
    }

    /** 关联消息。 */
    public MessageExt getMsg() {
        return msg;
    }

    /** 设置关联消息。 */
    public void setMsg(MessageExt msg) {
        this.msg = msg;
    }

    /** 魔数。 */
    public int getMagic() {
        return magic;
    }

    /** 删除 Topic 列表。 */
    public Set<String> getDeleteList() {
        return deleteList;
    }

    /** 设置删除 Topic 列表。 */
    public void setDeleteList(Set<String> deleteList) {
        this.deleteList = deleteList;
    }

    /** 设置同步门闩。 */
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }
    /** 设置入队时间。 */
    public void setEnqueueTime(long enqueueTime) {
        this.enqueueTime = enqueueTime;
    }
    /** 幂等释放同步门闩（默认成功）。 */
    public void idempotentRelease() {
        idempotentRelease(true);
    }

    /** 幂等释放同步门闩并记录结果。 */
    public void idempotentRelease(boolean succ) {
        this.succ = succ;
        if (!released && latch != null) {
            released = true;
            latch.countDown();
        }
    }

    /** 返回处理是否成功。 */
    public boolean isSucc() {
        return succ;
    }

    @Override
    public String toString() {
        return "TimerRequest{" +
            "offsetPy=" + offsetPy +
            ", sizePy=" + sizePy +
            ", delayTime=" + delayTime +
            ", enqueueTime=" + enqueueTime +
            ", magic=" + magic +
            ", msg=" + msg +
            ", latch=" + latch +
            ", released=" + released +
            ", succ=" + succ +
            ", deleteList=" + deleteList +
            '}';
    }
}
