/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.statistic.metric;

import java.util.List;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.data.MetricBucket;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * 记录受保护资源调用指标的基础结构。
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public interface Metric extends DebugSupport {

    /**
     * 获取成功总数。
     *
     * @return 成功计数
     */
    long success();

    /**
     * 获取最大成功计数。
     *
     * @return 最大成功计数
     */
    long maxSuccess();

    /**
     * 获取异常总数。
     *
     * @return 异常计数
     */
    long exception();

    /**
     * 获取阻断总数。
     *
     * @return 阻断计数
     */
    long block();

    /**
     * 获取放行总数，不包含 {@link #occupiedPass()}。
     *
     * @return 放行计数
     */
    long pass();

    /**
     * 获取响应时间总和。
     *
     * @return 总 RT
     */
    long rt();

    /**
     * 获取最小 RT。
     *
     * @return 最小 RT
     */
    long minRt();

    /**
     * 获取全部资源的聚合指标节点。
     *
     * @return 全部资源的指标节点列表
     */
    List<MetricNode> details();

    /**
     * 生成满足时间谓词的聚合指标项。
     *
     * @param timePredicate 时间谓词
     * @return 聚合指标项
     * @since 1.7.0
     */
    List<MetricNode> detailsOnCondition(Predicate<Long> timePredicate);

    /**
     * 获取原始窗口数组。
     *
     * @return 窗口指标数组
     */
    MetricBucket[] windows();

    /**
     * 累加当前异常计数。
     *
     * @param n 要增加的数量
     */
    void addException(int n);

    /**
     * 累加当前阻断计数。
     *
     * @param n 要增加的数量
     */
    void addBlock(int n);

    /**
     * 累加当前完成计数。
     *
     * @param n 要增加的数量
     */
    void addSuccess(int n);

    /**
     * 累加当前放行计数。
     *
     * @param n 要增加的数量
     */
    void addPass(int n);

    /**
     * 将给定 RT 累加到当前总 RT。
     *
     * @param rt 响应时间
     */
    void addRT(long rt);

    /**
     * 获取滑动窗口长度（秒）。
     *
     * @return 滑动窗口长度
     */
    double getWindowIntervalInSec();

    /**
     * 获取滑动窗口采样数。
     *
     * @return 滑动窗口采样数
     */
    int getSampleCount();

    /**
     * 注意：此操作不会刷新窗口，因此不会生成新桶。
     *
     * @param timeMillis 有效时间（毫秒）
     * @return 与给定时间戳精确对应的桶的放行数；时间戳无效时返回 0
     * @since 1.5.0
     */
    long getWindowPass(long timeMillis);

    // 预占相关（@since 1.5.0）

    /**
     * 累加预占放行数，表示借用后续窗口配额的放行请求。
     *
     * @param acquireCount 令牌数量
     * @since 1.5.0
     */
    void addOccupiedPass(int acquireCount);

    /**
     * 添加预占请求。
     *
     * @param futureTime   应累加 acquireCount 的未来时间戳
     * @param acquireCount 令牌数量
     * @since 1.5.0
     */
    void addWaiting(long futureTime, int acquireCount);

    /**
     * 获取等待中的放行数。
     *
     * @return 等待放行计数
     * @since 1.5.0
     */
    long waiting();

    /**
     * 获取预占放行计数。
     *
     * @return 预占放行计数
     * @since 1.5.0
     */
    long occupiedPass();

    // 工具方法。

    long previousWindowBlock();

    long previousWindowPass();
}
