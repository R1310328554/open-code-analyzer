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

package org.apache.rocketmq.common.stats;

/**
 * 统计快照：保存某一时间窗口内的 SUM、TPS、调用次数与平均耗时。
 */
public class StatsSnapshot {
    /** 窗口内累计值。 */
    private long sum;
    /** 每秒吞吐量。 */
    private double tps;

    /** 窗口内调用次数。 */
    private long times;
    /** 平均单次耗时（per time）。 */
    private double avgpt;

    /** 返回累计值。 */
    public long getSum() {
        return sum;
    }

    /** 设置累计值。 */
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

    /** 返回调用次数。 */
    public long getTimes() {
        return times;
    }

    /** 设置调用次数。 */
    public void setTimes(long times) {
        this.times = times;
    }
}
