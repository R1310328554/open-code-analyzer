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
package com.alibaba.csp.sentinel.cluster.flow.statistic.metric;

import java.util.List;

import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterFlowEvent;
import com.alibaba.csp.sentinel.cluster.flow.statistic.data.ClusterMetricBucket;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 集群流控指标统计，基于 {@link ClusterMetricLeapArray} 滑动窗口聚合 {@link ClusterFlowEvent}。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterMetric {

    private final ClusterMetricLeapArray metric;

    public ClusterMetric(int sampleCount, int intervalInMs) {
        AssertUtil.isTrue(sampleCount > 0, "sampleCount should be positive");
        AssertUtil.isTrue(intervalInMs > 0, "interval should be positive");
        AssertUtil.isTrue(intervalInMs % sampleCount == 0, "time span needs to be evenly divided");
        this.metric = new ClusterMetricLeapArray(sampleCount, intervalInMs);
    }

    public void add(ClusterFlowEvent event, long count) {
        metric.currentWindow().value().add(event, count);
    }

    public long getCurrentCount(ClusterFlowEvent event) {
        return metric.currentWindow().value().get(event);
    }

    /**
     * 获取指定事件在整个统计窗口内的总计数。
     *
     * @param event 待统计的事件类型
     * @return 该事件的总计数
     */
    public long getSum(ClusterFlowEvent event) {
        metric.currentWindow();
        long sum = 0;

        List<ClusterMetricBucket> buckets = metric.values();
        for (ClusterMetricBucket bucket : buckets) {
            sum += bucket.get(event);
        }
        return sum;
    }

    /**
     * 获取指定事件的每秒平均计数（QPS）。
     *
     * @param event 待统计的事件类型
     * @return 该事件的每秒平均计数
     */
    public double getAvg(ClusterFlowEvent event) {
        return getSum(event) / metric.getIntervalInSecond();
    }

    /**
     * 尝试预占用即将到来的时间桶。
     *
     * @return 等待下一桶的毫秒数；无法预占用时返回 0
     */
    public int tryOccupyNext(ClusterFlowEvent event, int acquireCount, double threshold) {
        double latestQps = getAvg(ClusterFlowEvent.PASS);
        if (!canOccupy(event, acquireCount, latestQps, threshold)) {
            return 0;
        }
        metric.addOccupyPass(acquireCount);
        add(ClusterFlowEvent.WAITING, acquireCount);
        return 1000 / metric.getSampleCount();
    }

    private boolean canOccupy(ClusterFlowEvent event, int acquireCount, double latestQps, double threshold) {
        long headPass = metric.getFirstCountOfWindow(event);
        long occupiedCount = metric.getOccupiedCount(event);
        //  待占用桶（= 即将到来的桶）
        //       ↓
        // | 头桶 |    |    |    | 当前桶 |
        // +------+----+----+----+--------+
        //   (headPass)
        return latestQps + (acquireCount + occupiedCount) - headPass <= threshold;
    }
}
