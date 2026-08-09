/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.protocol.subscription;

import com.google.common.base.MoreObjects;
import java.util.concurrent.TimeUnit;

/**
 * 自定义重试策略：与 messageDelayLevel 延迟级别表兼容，供消费组按固定阶梯延迟重投。
 *
 * @see <a href="https://github.com/apache/rocketmq/blob/3bd4b2b2f61a824196f19b03146e2c929c62777b/store/src/main/java/org/apache/rocketmq/store/config/MessageStoreConfig.java#L137">org.apache.rocketmq.store.config.MessageStoreConfig</a>
 */
public class CustomizedRetryPolicy implements RetryPolicy {
    /** 默认延迟阶梯（毫秒）：1s 5s 10s … 2h，与 messageDelayLevel 对齐。 */
    private long[] next = new long[] {
        TimeUnit.SECONDS.toMillis(1),
        TimeUnit.SECONDS.toMillis(5),
        TimeUnit.SECONDS.toMillis(10),
        TimeUnit.SECONDS.toMillis(30),
        TimeUnit.MINUTES.toMillis(1),
        TimeUnit.MINUTES.toMillis(2),
        TimeUnit.MINUTES.toMillis(3),
        TimeUnit.MINUTES.toMillis(4),
        TimeUnit.MINUTES.toMillis(5),
        TimeUnit.MINUTES.toMillis(6),
        TimeUnit.MINUTES.toMillis(7),
        TimeUnit.MINUTES.toMillis(8),
        TimeUnit.MINUTES.toMillis(9),
        TimeUnit.MINUTES.toMillis(10),
        TimeUnit.MINUTES.toMillis(20),
        TimeUnit.MINUTES.toMillis(30),
        TimeUnit.HOURS.toMillis(1),
        TimeUnit.HOURS.toMillis(2)
    };

    public CustomizedRetryPolicy() {
    }

    /** 使用自定义延迟数组构造。 */
    public CustomizedRetryPolicy(long[] next) {
        this.next = next;
    }

    /** 返回延迟阶梯数组。 */
    public long[] getNext() {
        return next;
    }

    /** 设置延迟阶梯数组。 */
    public void setNext(long[] next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("next", next)
            .toString();
    }

    /**
     * 按重试次数计算下次延迟：index = reconsumeTimes + 2，与旧 delayLevelTable 索引兼容。
     *
     * @param reconsumeTimes Message reconsumeTimes {@link org.apache.rocketmq.common.message.MessageExt#getReconsumeTimes}
     * @see <a href="https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/broker/src/main/java/org/apache/rocketmq/broker/processor/AbstractSendMessageProcessor.java#L210">org.apache.rocketmq.broker.processor.AbstractSendMessageProcessor</a>
     * @see <a href="https://github.com/apache/rocketmq/blob/3bddd514646826253a239f95959c14840a87034a/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java#L242">org.apache.rocketmq.store.DefaultMessageStore</a>
     */
    @Override
    public long nextDelayDuration(int reconsumeTimes) {
        if (reconsumeTimes < 0) {
            reconsumeTimes = 0;
        }
        int index = reconsumeTimes + 2;
        if (index >= next.length) {
            index = next.length - 1;
        }
        return next[index];
    }
}
