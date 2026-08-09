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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.Objects;

/**
 * 集群模式下的流控规则配置。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterFlowConfig {

    /** 全局唯一流控 ID。 */
    private Long flowId;

    /** 阈值类型（按本地均分或全局阈值）。 */
    private int thresholdType = ClusterRuleConstant.FLOW_THRESHOLD_AVG_LOCAL;
    private boolean fallbackToLocalWhenFail = true;

    /** 集群策略：0 表示普通模式。 */
    private int strategy = ClusterRuleConstant.FLOW_CLUSTER_STRATEGY_NORMAL;

    private int sampleCount = ClusterRuleConstant.DEFAULT_CLUSTER_SAMPLE_COUNT;
    /** 统计滑动窗口的时间长度（毫秒）。 */
    private int windowIntervalMs = RuleConstant.DEFAULT_WINDOW_INTERVAL_MS;

    /** 客户端持有令牌超过 resourceTimeout 时，resourceTimeoutStrategy 生效。 */
    private long resourceTimeout = 2000;

    /** 令牌超时策略：0 忽略，1 释放令牌。 */
    private int resourceTimeoutStrategy = RuleConstant.DEFAULT_RESOURCE_TIMEOUT_STRATEGY;

    /**
     * 优先级请求（prioritized=true）被阻断时，acquireRefuseStrategy 生效：
     * 0 忽略并阻断；1 重试一次；2 重试直至成功。
     */
    private int acquireRefuseStrategy = RuleConstant.DEFAULT_BLOCK_STRATEGY;

    /** 客户端离线后，服务端在 clientOfflineTime 之后删除其持有的全部令牌。 */
    private long clientOfflineTime = 2000;

    public long getResourceTimeout() {
        return resourceTimeout;
    }

    public void setResourceTimeout(long resourceTimeout) {
        this.resourceTimeout = resourceTimeout;
    }

    public int getResourceTimeoutStrategy() {
        return resourceTimeoutStrategy;
    }

    public void setResourceTimeoutStrategy(int resourceTimeoutStrategy) {
        this.resourceTimeoutStrategy = resourceTimeoutStrategy;
    }

    public int getAcquireRefuseStrategy() {
        return acquireRefuseStrategy;
    }

    public void setAcquireRefuseStrategy(int acquireRefuseStrategy) {
        this.acquireRefuseStrategy = acquireRefuseStrategy;
    }

    public long getClientOfflineTime() {
        return clientOfflineTime;
    }

    public void setClientOfflineTime(long clientOfflineTime) {
        this.clientOfflineTime = clientOfflineTime;
    }

    public Long getFlowId() {
        return flowId;
    }

    public ClusterFlowConfig setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public int getThresholdType() {
        return thresholdType;
    }

    public ClusterFlowConfig setThresholdType(int thresholdType) {
        this.thresholdType = thresholdType;
        return this;
    }

    public int getStrategy() {
        return strategy;
    }

    public ClusterFlowConfig setStrategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public boolean isFallbackToLocalWhenFail() {
        return fallbackToLocalWhenFail;
    }

    public ClusterFlowConfig setFallbackToLocalWhenFail(boolean fallbackToLocalWhenFail) {
        this.fallbackToLocalWhenFail = fallbackToLocalWhenFail;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public ClusterFlowConfig setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
        return this;
    }

    public int getWindowIntervalMs() {
        return windowIntervalMs;
    }

    public ClusterFlowConfig setWindowIntervalMs(int windowIntervalMs) {
        this.windowIntervalMs = windowIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClusterFlowConfig that = (ClusterFlowConfig) o;

        if (thresholdType != that.thresholdType) {
            return false;
        }
        if (fallbackToLocalWhenFail != that.fallbackToLocalWhenFail) {
            return false;
        }
        if (strategy != that.strategy) {
            return false;
        }
        if (sampleCount != that.sampleCount) {
            return false;
        }
        if (windowIntervalMs != that.windowIntervalMs) {
            return false;
        }
        if (resourceTimeout != that.resourceTimeout) {
            return false;
        }
        if (clientOfflineTime != that.clientOfflineTime) {
            return false;
        }
        if (resourceTimeoutStrategy != that.resourceTimeoutStrategy) {
            return false;
        }
        if (acquireRefuseStrategy != that.acquireRefuseStrategy) {
            return false;
        }
        return Objects.equals(flowId, that.flowId);
    }

    @Override
    public int hashCode() {
        int result = flowId != null ? flowId.hashCode() : 0;
        result = 31 * result + thresholdType;
        result = 31 * result + (fallbackToLocalWhenFail ? 1 : 0);
        result = 31 * result + strategy;
        result = 31 * result + sampleCount;
        result = 31 * result + windowIntervalMs;
        result = (int) (31 * result + resourceTimeout);
        result = (int) (31 * result + clientOfflineTime);
        result = 31 * result + resourceTimeoutStrategy;
        result = 31 * result + acquireRefuseStrategy;
        return result;
    }

    @Override
    public String toString() {
        return "ClusterFlowConfig{" +
                "flowId=" + flowId +
                ", thresholdType=" + thresholdType +
                ", fallbackToLocalWhenFail=" + fallbackToLocalWhenFail +
                ", strategy=" + strategy +
                ", sampleCount=" + sampleCount +
                ", windowIntervalMs=" + windowIntervalMs +
                ", resourceTimeout=" + resourceTimeout +
                ", resourceTimeoutStrategy=" + resourceTimeoutStrategy +
                ", acquireRefuseStrategy=" + acquireRefuseStrategy +
                ", clientOfflineTime=" + clientOfflineTime +
                '}';
    }
}
