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
package com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule;

/**
 * 更新网关流控规则的请求值对象，携带阈值、统计窗口与参数流控项。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class UpdateFlowRuleReqVo {

    /** 规则 ID。 */
    private Long id;

    /** 应用名。 */
    private String app;

    /** 限流阈值类型（QPS/线程数等）。 */
    private Integer grade;

    /** 限流阈值。 */
    private Double count;

    /** 统计窗口长度。 */
    private Long interval;

    /** 统计窗口单位。 */
    private Integer intervalUnit;

    /** 流控行为（直接拒绝/匀速排队等）。 */
    private Integer controlBehavior;

    /** 突发流量额外许可数。 */
    private Integer burst;

    /** 匀速排队最大等待时间（毫秒）。 */
    private Integer maxQueueingTimeoutMs;

    /** 参数流控项配置。 */
    private GatewayParamFlowItemVo paramItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public Integer getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(Integer intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Integer getControlBehavior() {
        return controlBehavior;
    }

    public void setControlBehavior(Integer controlBehavior) {
        this.controlBehavior = controlBehavior;
    }

    public Integer getBurst() {
        return burst;
    }

    public void setBurst(Integer burst) {
        this.burst = burst;
    }

    public Integer getMaxQueueingTimeoutMs() {
        return maxQueueingTimeoutMs;
    }

    public void setMaxQueueingTimeoutMs(Integer maxQueueingTimeoutMs) {
        this.maxQueueingTimeoutMs = maxQueueingTimeoutMs;
    }

    public GatewayParamFlowItemVo getParamItem() {
        return paramItem;
    }

    public void setParamItem(GatewayParamFlowItemVo paramItem) {
        this.paramItem = paramItem;
    }
}
