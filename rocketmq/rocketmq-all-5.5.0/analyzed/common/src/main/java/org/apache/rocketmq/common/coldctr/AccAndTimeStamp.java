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
package org.apache.rocketmq.common.coldctr;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 冷读控制计数与时间戳：记录冷数据累计访问量及最近冷读、创建时间。
 */
public class AccAndTimeStamp {

    /** 冷读累计计数（原子累加）。 */
    public AtomicLong coldAcc = new AtomicLong(0L);
    /** 最近一次冷读时间戳（毫秒）。 */
    public Long lastColdReadTimeMills = System.currentTimeMillis();
    /** 记录创建时间戳（毫秒）。 */
    public Long createTimeMills = System.currentTimeMillis();

    /** 使用外部传入的冷读计数器构造。 */
    public AccAndTimeStamp(AtomicLong coldAcc) {
        this.coldAcc = coldAcc;
    }

    /** 获取冷读累计计数。 */
    public AtomicLong getColdAcc() {
        return coldAcc;
    }

    /** 设置冷读累计计数。 */
    public void setColdAcc(AtomicLong coldAcc) {
        this.coldAcc = coldAcc;
    }

    /** 获取最近冷读时间戳。 */
    public Long getLastColdReadTimeMills() {
        return lastColdReadTimeMills;
    }

    /** 设置最近冷读时间戳。 */
    public void setLastColdReadTimeMills(Long lastColdReadTimeMills) {
        this.lastColdReadTimeMills = lastColdReadTimeMills;
    }

    /** 获取创建时间戳。 */
    public Long getCreateTimeMills() {
        return createTimeMills;
    }

    /** 设置创建时间戳。 */
    public void setCreateTimeMills(Long createTimeMills) {
        this.createTimeMills = createTimeMills;
    }

    @Override
    public String toString() {
        return "AccAndTimeStamp{" +
            "coldAcc=" + coldAcc +
            ", lastColdReadTimeMills=" + lastColdReadTimeMills +
            ", createTimeMills=" + createTimeMills +
            '}';
    }
}
