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

/**
 * Broker 单时间窗口统计项：累计量、TPS 与平均处理耗时。
 */
public class BrokerStatsItem {
    /** 窗口内累计计数（如消息条数）。 */
    private long sum;
    /** 吞吐量（条/秒）。 */
    private double tps;
    /** 平均处理耗时（毫秒/条）。 */
    private double avgpt;

    /** 返回累计量。 */
    public long getSum() {
        return sum;
    }

    /** 设置累计量。 */
    public void setSum(long sum) {
        this.sum = sum;
    }

    /** 返回 TPS。 */
    public double getTps() {
        return tps;
    }

    /** 设置 TPS。 */
    public void setTps(double tps) {
        this.tps = tps;
    }

    /** 返回平均耗时。 */
    public double getAvgpt() {
        return avgpt;
    }

    /** 设置平均耗时。 */
    public void setAvgpt(double avgpt) {
        this.avgpt = avgpt;
    }
}
