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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.slots.statistic.metric.ArrayMetric;
import com.alibaba.csp.sentinel.slots.statistic.metric.Metric;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * <p>统计节点维护三类实时统计指标：</p>
 * <ol>
 * <li>秒级指标（{@code rollingCounterInSecond}）</li>
 * <li>分钟级指标（{@code rollingCounterInMinute}）</li>
 * <li>线程数</li>
 * </ol>
 *
 * <p>
 * Sentinel 使用滑动窗口实时记录并统计资源指标。
 * {@link ArrayMetric} 背后的滑动窗口基础设施为 {@code LeapArray}。
 * </p>
 *
 * <p>
 * 场景 1：首个请求到达时，Sentinel 会创建新的时间窗口桶，
 * 用于存储运行中的统计量（如总 RT、入站 QPS、阻断 QPS 等），
 * 窗口跨度由采样数决定。
 * </p>
 * <pre>
 * 	0      100ms
 *  +-------+--→ 滑动窗口
 * 	    ^
 * 	    |
 * 	  request
 * </pre>
 * <p>
 * Sentinel 汇总有效桶的统计量来判断请求是否可通过。
 * 例如规则限定最多 100 次通过，则累加有效桶中的 QPS 并与阈值比较。
 * </p>
 *
 * <p>场景 2：连续请求</p>
 * <pre>
 *  0    100ms    200ms    300ms
 *  +-------+-------+-------+-----→ 滑动窗口
 *                      ^
 *                      |
 *                   request
 * </pre>
 *
 * <p>场景 3：请求持续到达，旧桶失效</p>
 * <pre>
 *  0    100ms    200ms	  800ms	   900ms  1000ms    1300ms
 *  +-------+-------+ ...... +-------+-------+ ...... +-------+-----→ 滑动窗口
 *                                                      ^
 *                                                      |
 *                                                    request
 * </pre>
 *
 * <p>滑动窗口应变为：</p>
 * <pre>
 * 300ms     800ms  900ms  1000ms  1300ms
 *  + ...... +-------+ ...... +-------+-----→ 滑动窗口
 *                                                      ^
 *                                                      |
 *                                                    request
 * </pre>
 *
 * @author qinan.qn
 * @author jialiang.linjl
 */
public class StatisticNode implements Node {

    /**
     * 保存最近 {@code INTERVAL} 毫秒内的统计量。
     * {@code INTERVAL} 按 {@code sampleCount} 划分为多个时间片。
     */
    private transient volatile Metric rollingCounterInSecond = new ArrayMetric(SampleCountProperty.SAMPLE_COUNT,
        IntervalProperty.INTERVAL);

    /**
     * 保存最近 60 秒的统计量。窗口长度刻意设为 1000 毫秒，
     * 即每秒一个桶，以便精确统计每一秒的数据。
     */
    private transient Metric rollingCounterInMinute = new ArrayMetric(60, 60 * 1000, false);

    /**
     * 线程数计数器。
     */
    private LongAdder curThreadNum = new LongAdder();

    /**
     * 上次拉取指标时的时间戳。
     */
    private long lastFetchTime = -1;

    @Override
    public Map<Long, MetricNode> metrics() {
        // 在单线程调度池下，拉取操作是线程安全的。
        long currentTime = TimeUtil.currentTimeMillis();
        currentTime = currentTime - currentTime % 1000;
        Map<Long, MetricNode> metrics = new ConcurrentHashMap<>();
        List<MetricNode> nodesOfEverySecond = rollingCounterInMinute.details();
        long newLastFetchTime = lastFetchTime;
        // 遍历所有资源的指标，过滤有效（非空且最新）的指标。
        for (MetricNode node : nodesOfEverySecond) {
            if (isNodeInTime(node, currentTime) && isValidMetricNode(node)) {
                metrics.put(node.getTimestamp(), node);
                newLastFetchTime = Math.max(newLastFetchTime, node.getTimestamp());
            }
        }
        lastFetchTime = newLastFetchTime;

        return metrics;
    }

    @Override
    public List<MetricNode> rawMetricsInMin(Predicate<Long> timePredicate) {
        return rollingCounterInMinute.detailsOnCondition(timePredicate);
    }

    private boolean isNodeInTime(MetricNode node, long currentTime) {
        return node.getTimestamp() > lastFetchTime && node.getTimestamp() < currentTime;
    }

    private boolean isValidMetricNode(MetricNode node) {
        return node.getPassQps() > 0 || node.getBlockQps() > 0 || node.getSuccessQps() > 0
            || node.getExceptionQps() > 0 || node.getRt() > 0 || node.getOccupiedPassQps() > 0;
    }

    @Override
    public void reset() {
        rollingCounterInSecond = new ArrayMetric(SampleCountProperty.SAMPLE_COUNT, IntervalProperty.INTERVAL);
    }

    @Override
    public long totalRequest() {
        return rollingCounterInMinute.pass() + rollingCounterInMinute.block();
    }

    @Override
    public long blockRequest() {
        return rollingCounterInMinute.block();
    }

    @Override
    public double blockQps() {
        return rollingCounterInSecond.block() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public double previousBlockQps() {
        return this.rollingCounterInMinute.previousWindowBlock();
    }

    @Override
    public double previousPassQps() {
        return this.rollingCounterInMinute.previousWindowPass();
    }

    @Override
    public double totalQps() {
        return passQps() + blockQps();
    }

    @Override
    public long totalSuccess() {
        return rollingCounterInMinute.success();
    }

    @Override
    public double exceptionQps() {
        return rollingCounterInSecond.exception() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public long totalException() {
        return rollingCounterInMinute.exception();
    }

    @Override
    public double passQps() {
        return rollingCounterInSecond.pass() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public long totalPass() {
        return rollingCounterInMinute.pass();
    }

    @Override
    public double successQps() {
        return rollingCounterInSecond.success() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public double maxSuccessQps() {
        return (double) rollingCounterInSecond.maxSuccess() * rollingCounterInSecond.getSampleCount()
                / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public double occupiedPassQps() {
        return rollingCounterInSecond.occupiedPass() / rollingCounterInSecond.getWindowIntervalInSec();
    }

    @Override
    public double avgRt() {
        long successCount = rollingCounterInSecond.success();
        if (successCount == 0) {
            return 0;
        }

        return rollingCounterInSecond.rt() * 1.0 / successCount;
    }

    @Override
    public double minRt() {
        return rollingCounterInSecond.minRt();
    }

    @Override
    public int curThreadNum() {
        return (int)curThreadNum.sum();
    }

    @Override
    public void addPassRequest(int count) {
        rollingCounterInSecond.addPass(count);
        rollingCounterInMinute.addPass(count);
    }

    @Override
    public void addRtAndSuccess(long rt, int successCount) {
        rollingCounterInSecond.addSuccess(successCount);
        rollingCounterInSecond.addRT(rt);

        rollingCounterInMinute.addSuccess(successCount);
        rollingCounterInMinute.addRT(rt);
    }

    @Override
    public void increaseBlockQps(int count) {
        rollingCounterInSecond.addBlock(count);
        rollingCounterInMinute.addBlock(count);
    }

    @Override
    public void increaseExceptionQps(int count) {
        rollingCounterInSecond.addException(count);
        rollingCounterInMinute.addException(count);
    }

    @Override
    public void increaseThreadNum() {
        curThreadNum.increment();
    }

    @Override
    public void decreaseThreadNum() {
        curThreadNum.decrement();
    }

    @Override
    public void debug() {
        rollingCounterInSecond.debug();
    }

    @Override
    public long tryOccupyNext(long currentTime, int acquireCount, double threshold) {
        double maxCount = threshold * IntervalProperty.INTERVAL / 1000;
        long currentBorrow = rollingCounterInSecond.waiting();
        if (currentBorrow >= maxCount) {
            return OccupyTimeoutProperty.getOccupyTimeout();
        }

        int windowLength = IntervalProperty.INTERVAL / SampleCountProperty.SAMPLE_COUNT;
        long earliestTime = currentTime - currentTime % windowLength + windowLength - IntervalProperty.INTERVAL;

        int idx = 0;
        /*
         * 注意：此处 {@code currentPass} 可能小于当前真实值，
         * 因为从调用 rollingCounterInSecond.pass() 起已有时间差。
         * 高并发下以下代码可能导致更多令牌被借用。
         */
        long currentPass = rollingCounterInSecond.pass();
        while (earliestTime < currentTime) {
            long waitInMs = idx * windowLength + windowLength - currentTime % windowLength;
            if (waitInMs >= OccupyTimeoutProperty.getOccupyTimeout()) {
                break;
            }
            long windowPass = rollingCounterInSecond.getWindowPass(earliestTime);
            if (currentPass + currentBorrow + acquireCount - windowPass <= maxCount) {
                return waitInMs;
            }
            earliestTime += windowLength;
            currentPass -= windowPass;
            idx++;
        }

        return OccupyTimeoutProperty.getOccupyTimeout();
    }

    @Override
    public long waiting() {
        return rollingCounterInSecond.waiting();
    }

    @Override
    public void addWaitingRequest(long futureTime, int acquireCount) {
        rollingCounterInSecond.addWaiting(futureTime, acquireCount);
    }

    @Override
    public void addOccupiedPass(int acquireCount) {
        rollingCounterInMinute.addOccupiedPass(acquireCount);
        rollingCounterInMinute.addPass(acquireCount);
    }
}
