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

package org.apache.rocketmq.client.latency;

/**
 * 延迟故障容错接口：根据发送延迟与可达性动态隔离 Broker，
 * 并在恢复后重新纳入选路范围。
 *
 * @param <T> 故障项标识类型，通常为 Broker 名称。
 */
public interface LatencyFaultTolerance<T> {
    /**
     * 更新 Broker 故障项：记录当前延迟、不可用时长及可达性。
     *
     * @param name Broker 名称
     * @param currentLatency 本次发送耗时（毫秒）
     * @param notAvailableDuration 隔离时长（毫秒），到期前视为不可用
     * @param reachable 当前是否网络可达
     */
    void updateFaultItem(final T name, final long currentLatency, final long notAvailableDuration,
                         final boolean reachable);

    /**
     * 判断 Broker 是否可用（隔离期已过）。
     *
     * @param name Broker 名称
     * @return true 表示可用
     */
    boolean isAvailable(final T name);

    /**
     * 判断 Broker 是否可达（网络探测正常）。
     *
     * @param name Broker 名称
     * @return true 表示可达
     */
    boolean isReachable(final T name);

    /**
     * 从故障表中移除指定 Broker。
     *
     * @param name Broker 名称
     */
    void remove(final T name);

    /**
     * 兜底策略：无可用 Broker 时随机选取一个可达项。
     *
     * @return 随机 Broker 名称，无则 null
     */
    T pickOneAtLeast();

    /** 启动后台探测线程，周期性检测 Broker 可达性。 */
    void startDetector();

    /** 关闭探测线程池。 */
    void shutdown();

    /** 执行一轮可达性探测，不创建新线程。 */
    void detectByOneRound();

    /**
     * 设置单次探测超时（毫秒）。
     *
     * @param detectTimeout 超时上限
     */
    void setDetectTimeout(final int detectTimeout);

    /**
     * 设置每个 Broker 的探测间隔（毫秒）。
     *
     * @param detectInterval 探测周期
     */
    void setDetectInterval(final int detectInterval);

    /**
     * 启用或禁用后台探测器。
     *
     * @param startDetectorEnable 是否启动探测
     */
    void setStartDetectorEnable(final boolean startDetectorEnable);

    /**
     * 探测器是否已启用。
     *
     * @return true 表示应启动探测
     */
    boolean isStartDetectorEnable();
}
