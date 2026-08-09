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

package org.apache.rocketmq.remoting.protocol.body;

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Broker 运行统计聚合：分钟/小时/天三个时间粒度的 {@link BrokerStatsItem}。
 */
public class BrokerStatsData extends RemotingSerializable {

    /** 分钟级统计。 */
    private BrokerStatsItem statsMinute;

    /** 小时级统计。 */
    private BrokerStatsItem statsHour;

    /** 天级统计。 */
    private BrokerStatsItem statsDay;

    /** 返回分钟统计。 */
    public BrokerStatsItem getStatsMinute() {
        return statsMinute;
    }

    /** 设置分钟统计。 */
    public void setStatsMinute(BrokerStatsItem statsMinute) {
        this.statsMinute = statsMinute;
    }

    /** 返回小时统计。 */
    public BrokerStatsItem getStatsHour() {
        return statsHour;
    }

    public void setStatsHour(BrokerStatsItem statsHour) {
        this.statsHour = statsHour;
    }

    /** 返回天级统计。 */
    public BrokerStatsItem getStatsDay() {
        return statsDay;
    }

    public void setStatsDay(BrokerStatsItem statsDay) {
        this.statsDay = statsDay;
    }
}
