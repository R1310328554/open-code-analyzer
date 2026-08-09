/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.node;

/**
 * 支持借用未来时间窗口令牌以实现优先级等待的接口。
 *
 * @author Eric Zhao
 * @since 1.5.0
 */
public interface OccupySupport {

    /**
     * 尝试占用后续时间窗口的令牌。占用成功时返回小于 {@link OccupyTimeoutProperty} 中
     * {@code occupyTimeout} 的值。
     *
     * <p>
     * 每次占用未来窗口令牌时，当前线程应睡眠相应时长以平滑 QPS。
     * 占用时长受 {@link OccupyTimeoutProperty} 中 {@code occupyTimeout} 限制。
     * </p>
     *
     * @param currentTime  当前时间（毫秒）
     * @param acquireCount 要获取的令牌数
     * @param threshold    QPS 阈值
     * @return 应睡眠的毫秒数。若 >= {@link OccupyTimeoutProperty} 中的 {@code occupyTimeout} 表示占用失败，
     * 此时应直接拒绝请求
     */
    long tryOccupyNext(long currentTime, int acquireCount, double threshold);

    /**
     * 获取当前等待中的令牌量，便于调试。
     *
     * @return 当前等待量
     */
    long waiting();

    /**
     * 添加已占用令牌的等待请求。
     *
     * @param futureTime   应累加 acquireCount 的未来时间戳
     * @param acquireCount 令牌数
     */
    void addWaitingRequest(long futureTime, int acquireCount);

    /**
     * 增加占用通过请求数，表示借用后续窗口令牌的通过请求。
     *
     * @param acquireCount 令牌数
     */
    void addOccupiedPass(int acquireCount);

    /**
     * 获取当前占用通过 QPS。
     *
     * @return 当前占用通过 QPS
     */
    double occupiedPassQps();
}
