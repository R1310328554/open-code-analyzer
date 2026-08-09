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
package com.alibaba.csp.sentinel.slots.system;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;

/**
 * <p>
 * Sentinel 系统规则使入站流量与系统容量相匹配，综合考虑平均 RT、QPS 与线程数。
 * 还提供系统负载度量（仅 Linux 可用）。
 * </p>
 * <p>
 * 建议协调 {@link #highestSystemLoad}、{@link #qps}、{@link #avgRt}
 * 与 {@link #maxThread}，确保系统运行在安全水位。
 * </p>
 * <p>
 * 合理设置阈值通常需要性能测试。
 * </p>
 *
 * @author jialiang.linjl
 * @author Carpenter Lee
 * @see SystemRuleManager
 */
public class SystemRule extends AbstractRule {

    /** 负值表示不检查该阈值。 */
    private double highestSystemLoad = -1;
    /** CPU 使用率，取值范围 [0, 1]。 */
    private double highestCpuUsage = -1;
    private double qps = -1;
    private long avgRt = -1;
    private long maxThread = -1;

    public double getQps() {
        return qps;
    }

    /**
     * 设置全局最大 QPS。高并发下实际放行 QPS 可能略高于设定值，近似满足：<br/>
     *
     * <pre>实际放行 QPS = 设定 QPS + 并发线程数</pre>
     *
     * @param qps 全局最大 QPS，≤ 0 表示清除该阈值
     */
    public void setQps(double qps) {
        this.qps = qps;
    }

    public long getMaxThread() {
        return maxThread;
    }

    /**
     * 设置最大并行工作线程数。并发线程数超过 {@code maxThread} 时，
     * 仅允许 {@code maxThread} 个线程并行执行。
     *
     * @param maxThread 最大并行线程数，≤ 0 表示清除该阈值
     */
    public void setMaxThread(long maxThread) {
        this.maxThread = maxThread;
    }

    public long getAvgRt() {
        return avgRt;
    }

    /**
     * 设置全部已通过请求的最大平均 RT（响应时间）。
     *
     * @param avgRt 最大平均响应时间，≤ 0 表示清除该阈值
     */
    public void setAvgRt(long avgRt) {
        this.avgRt = avgRt;
    }

    public double getHighestSystemLoad() {
        return highestSystemLoad;
    }

    /**
     * <p>
     * 设置最高系统负载。该负载与 Linux load 不同，后者不够敏感。
     * 计算时会综合考虑 Linux 系统负载、全局 RT 与全局 QPS，
     * 因此需与 {@link #setAvgRt(long)}、{@link #setQps(double)} 协调配置。
     * </p>
     * <p>
     * 注意：该参数仅在类 Unix 系统上可用。
     * </p>
     *
     * @param highestSystemLoad 最高系统负载，≤ 0 表示清除该阈值
     * @see SystemRuleManager
     */
    public void setHighestSystemLoad(double highestSystemLoad) {
        this.highestSystemLoad = highestSystemLoad;
    }

    /**
     * 获取最高 CPU 使用率，取值范围 [0, 1]。
     *
     * @return 最高 CPU 使用率
     */
    public double getHighestCpuUsage() {
        return highestCpuUsage;
    }

    /**
     * 设置最高 CPU 使用率，取值范围 [0, 1]。
     *
     * @param highestCpuUsage 待设置的值
     */
    public void setHighestCpuUsage(double highestCpuUsage) {
        this.highestCpuUsage = highestCpuUsage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemRule)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SystemRule that = (SystemRule)o;

        if (Double.compare(that.highestSystemLoad, highestSystemLoad) != 0) {
            return false;
        }
        if (Double.compare(that.highestCpuUsage, highestCpuUsage) != 0) {
            return false;
        }

        if (Double.compare(that.qps, qps) != 0) {
            return false;
        }

        if (avgRt != that.avgRt) {
            return false;
        }
        return maxThread == that.maxThread;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(highestSystemLoad);
        result = 31 * result + (int)(temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(highestCpuUsage);
        result = 31 * result + (int)(temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(qps);
        result = 31 * result + (int)(temp ^ (temp >>> 32));

        result = 31 * result + (int)(avgRt ^ (avgRt >>> 32));
        result = 31 * result + (int)(maxThread ^ (maxThread >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SystemRule{" +
            "highestSystemLoad=" + highestSystemLoad +
            ", highestCpuUsage=" + highestCpuUsage +
            ", qps=" + qps +
            ", avgRt=" + avgRt +
            ", maxThread=" + maxThread +
            "}";
    }
}
