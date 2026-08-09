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
 * 指数退避重试策略：初始延迟按 multiplier 指数增长，上限为 max。
 * 实现 {@link RetryPolicy} 供消费组配置。
 */
public class ExponentialRetryPolicy implements RetryPolicy {
    /** 首次重试延迟（毫秒），默认 5 秒。 */
    private long initial = TimeUnit.SECONDS.toMillis(5);
    /** 最大重试延迟（毫秒），默认 2 小时。 */
    private long max = TimeUnit.HOURS.toMillis(2);
    /** 指数乘数，默认 2。 */
    private long multiplier = 2;

    public ExponentialRetryPolicy() {
    }

    /** 指定初始延迟、上限与乘数构造。 */
    public ExponentialRetryPolicy(long initial, long max, long multiplier) {
        this.initial = initial;
        this.max = max;
        this.multiplier = multiplier;
    }

    /** 返回初始延迟。 */
    public long getInitial() {
        return initial;
    }

    /** 设置初始延迟。 */
    public void setInitial(long initial) {
        this.initial = initial;
    }

    /** 返回最大延迟。 */
    public long getMax() {
        return max;
    }

    /** 设置最大延迟。 */
    public void setMax(long max) {
        this.max = max;
    }

    /** 返回指数乘数。 */
    public long getMultiplier() {
        return multiplier;
    }

    /** 设置指数乘数。 */
    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("initial", initial)
            .add("max", max)
            .add("multiplier", multiplier)
            .toString();
    }

    @Override
    /** 按重试次数计算下次延迟：min(max, initial * multiplier^reconsumeTimes)。 */
    public long nextDelayDuration(int reconsumeTimes) {
        if (reconsumeTimes < 0) {
            reconsumeTimes = 0;
        }
        if (reconsumeTimes > 32) {
            reconsumeTimes = 32;
        }
        return Math.min(max, initial * (long) Math.pow(multiplier, reconsumeTimes));
    }
}
