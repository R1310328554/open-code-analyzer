/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.model.response;

import java.util.List;

/**
 * 集群服务端负载指标汇总。
 *
 * <p>聚合各节点 {@link ServerLoaderMetric} 明细，并计算 SDK 连接数的最大、最小、平均与阈值。</p>
 *
 * @author yunye
 * @since 3.0.0
 */
public class ServerLoaderMetrics {
    
    /** 各节点负载指标明细列表。 */
    private List<ServerLoaderMetric> detail;
    
    /** 集群成员节点总数。 */
    private int memberCount;
    
    /** 已上报负载指标的节点数。 */
    private int metricsCount;
    
    /** 是否所有节点均已返回负载指标。 */
    private boolean completed;
    
    /** 各节点 SDK 连接数的最大值。 */
    private int max;
    
    /** 各节点 SDK 连接数的最小值。 */
    private int min;
    
    /** SDK 连接数平均值（total / 节点数）。 */
    private int avg;
    
    /** 负载均衡阈值（avg × 1.1）。 */
    private String threshold;
    
    /** 全集群 SDK 连接数总和。 */
    private int total;
    
    /** 获取各节点负载明细。 */
    public List<ServerLoaderMetric> getDetail() {
        return detail;
    }
    
    /** 设置各节点负载明细。 */
    public void setDetail(List<ServerLoaderMetric> detail) {
        this.detail = detail;
    }
    
    /** 获取集群成员总数。 */
    public int getMemberCount() {
        return memberCount;
    }
    
    /** 设置集群成员总数。 */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    /** 获取已上报指标的节点数。 */
    public int getMetricsCount() {
        return metricsCount;
    }
    
    /** 设置已上报指标的节点数。 */
    public void setMetricsCount(int metricsCount) {
        this.metricsCount = metricsCount;
    }
    
    /** 是否所有节点均已上报指标。 */
    public boolean isCompleted() {
        return completed;
    }
    
    /** 设置指标采集完成标志。 */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /** 获取 SDK 连接数最大值。 */
    public int getMax() {
        return max;
    }
    
    /** 设置 SDK 连接数最大值。 */
    public void setMax(int max) {
        this.max = max;
    }
    
    /** 获取 SDK 连接数最小值。 */
    public int getMin() {
        return min;
    }
    
    /** 设置 SDK 连接数最小值。 */
    public void setMin(int min) {
        this.min = min;
    }
    
    /** 获取 SDK 连接数平均值。 */
    public int getAvg() {
        return avg;
    }
    
    /** 设置 SDK 连接数平均值。 */
    public void setAvg(int avg) {
        this.avg = avg;
    }
    
    /** 获取负载均衡阈值。 */
    public String getThreshold() {
        return threshold;
    }
    
    /** 设置负载均衡阈值。 */
    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }
    
    /** 获取 SDK 连接数总和。 */
    public int getTotal() {
        return total;
    }
    
    /** 设置 SDK 连接数总和。 */
    public void setTotal(int total) {
        this.total = total;
    }
}
