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
 * 新增网关流控规则请求体，含资源标识、限流阈值、统计窗口及可选参数流控项。
 *
 * @author cdfive
 * @since 1.7.0
 */
public class AddFlowRuleReqVo {

    /** 目标应用名。 */
    private String app;

    /** 目标客户端机器 IP。 */
    private String ip;

    /** 目标客户端机器端口。 */
    private Integer port;

    /** 受流控资源名（路由 ID 或 API 分组名）。 */
    private String resource;

    /** 资源模式（路由/API 分组等，见 {@code SentinelGatewayConstants}）。 */
    private Integer resourceMode;

    /** 限流维度（QPS/并发线程数等）。 */
    private Integer grade;

    /** 限流阈值。 */
    private Double count;

    /** 统计窗口长度。 */
    private Long interval;

    /** 统计窗口时间单位。 */
    private Integer intervalUnit;

    /** 流控效果（直接拒绝/匀速排队等）。 */
    private Integer controlBehavior;

    /** 突发流量允许额外通过的请求数。 */
    private Integer burst;

    /** 匀速排队模式下的最大排队超时（毫秒）。 */
    private Integer maxQueueingTimeoutMs;

    /** 可选的参数流控匹配项。 */
    private GatewayParamFlowItemVo paramItem;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(Integer resourceMode) {
        this.resourceMode = resourceMode;
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
