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
package com.alibaba.csp.sentinel.node;

import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.metric.DebugSupport;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * 保存资源的实时统计数据。
 *
 * @author qinan.qn
 * @author leyou
 * @author Eric Zhao
 */
public interface Node extends OccupySupport, DebugSupport {

    /**
     * 获取每分钟入站请求数（{@code pass + block}）。
     *
     * @return 每分钟总请求数
     */
    long totalRequest();

    /**
     * 获取每分钟通过请求数。
     *
     * @return 每分钟通过请求总数
     * @since 1.5.0
     */
    long totalPass();

    /**
     * 获取每分钟 {@link Entry#exit()} 次数。
     *
     * @return 每分钟完成请求总数
     */
    long totalSuccess();

    /**
     * 获取每分钟被阻断的请求数（totalBlockRequest）。
     *
     * @return 每分钟被阻断请求总数
     */
    long blockRequest();

    /**
     * 获取每分钟业务异常数。
     *
     * @return 每分钟业务异常总数
     */
    long totalException();

    /**
     * 获取每秒通过请求 QPS。
     *
     * @return 通过请求的 QPS
     */
    double passQps();

    /**
     * 获取每秒阻断请求 QPS。
     *
     * @return 阻断请求的 QPS
     */
    double blockQps();

    /**
     * 获取 {@link #passQps()} + {@link #blockQps()} 的每秒请求 QPS。
     *
     * @return 通过与阻断请求的总 QPS
     */
    double totalQps();

    /**
     * 获取每秒 {@link Entry#exit()} 请求 QPS。
     *
     * @return 完成请求的 QPS
     */
    double successQps();

    /**
     * 获取截至目前估计的最大成功 QPS。
     *
     * @return 最大完成 QPS
     */
    double maxSuccessQps();

    /**
     * 获取每秒异常 QPS。
     *
     * @return 异常发生的 QPS
     */
    double exceptionQps();

    /**
     * 获取每秒平均响应时间（RT）。
     *
     * @return 每秒平均响应时间
     */
    double avgRt();

    /**
     * 获取最小响应时间。
     *
     * @return 已记录的最小响应时间
     */
    double minRt();

    /**
     * 获取当前活跃线程数。
     *
     * @return 当前活跃线程数
     */
    int curThreadNum();

    /**
     * 获取上一秒的阻断 QPS。
     */
    double previousBlockQps();

    /**
     * 上一时间窗口的通过 QPS。
     */
    double previousPassQps();

    /**
     * 获取资源的所有有效指标节点。
     *
     * @return 资源的有效指标节点
     */
    Map<Long, MetricNode> metrics();

    /**
     * 获取满足时间谓词的所有原始指标项。
     *
     * @param timePredicate 时间谓词
     * @return 满足时间谓词的原始指标项
     * @since 1.7.0
     */
    List<MetricNode> rawMetricsInMin(Predicate<Long> timePredicate);

    /**
     * 增加通过计数。
     *
     * @param count 要增加的通过数
     */
    void addPassRequest(int count);

    /**
     * 增加 RT 与成功计数。
     *
     * @param rt      响应时间
     * @param success 要增加的成功数
     */
    void addRtAndSuccess(long rt, int success);

    /**
     * 增加阻断计数。
     *
     * @param count 要增加的阻断数
     */
    void increaseBlockQps(int count);

    /**
     * 增加业务异常计数。
     *
     * @param count 要增加的异常数
     */
    void increaseExceptionQps(int count);

    /**
     * 增加当前线程计数。
     */
    void increaseThreadNum();

    /**
     * 减少当前线程计数。
     */
    void decreaseThreadNum();

    /**
     * 重置内部计数器。当 {@link IntervalProperty#INTERVAL} 或
     * {@link SampleCountProperty#SAMPLE_COUNT} 变更时需要重置。
     */
    void reset();
}
