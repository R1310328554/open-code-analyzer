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
package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.Objects;

/**
 * <p>
 * 降级（熔断）用于资源处于不稳定状态时，在接下来定义的时间窗口内对该资源进行降级处理。
 * 判断资源是否稳定有两种方式：
 * </p>
 * <ul>
 * <li>
 * 平均响应时间（{@code DEGRADE_GRADE_RT}）：当平均 RT 超过阈值
 * （{@code DegradeRule} 中的 {@code count}，单位为毫秒）时，资源进入准降级状态。
 * 若后续 5 个请求的 RT 仍超过该阈值，则触发降级，即在下一个时间窗口
 * （{@code timeWindow}，单位为秒）内阻断对该资源的所有访问。
 * </li>
 * <li>
 * 异常比例：当每秒异常数与成功 QPS 之比超过阈值时，在即将到来的时间窗口内阻断对该资源的访问。
 * </li>
 * </ul>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class DegradeRule extends AbstractRule {

    public DegradeRule() {}

    public DegradeRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * 熔断策略（0：平均 RT，1：异常比例，2：异常数）。
     */
    private int grade = RuleConstant.DEGRADE_GRADE_RT;

    /**
     * 阈值。具体含义取决于 {@code grade} 字段。
     * <ul>
     *     <li>平均 RT 模式下，表示最大响应时间（RT），单位为毫秒。</li>
     *     <li>异常比例模式下，表示 0.0 到 1.0 之间的异常比例。</li>
     *     <li>异常数模式下，表示异常计数。</li>
     * <ul/>
     */
    private double count;

    /**
     * 熔断器打开后的恢复超时时间（秒）。超时后熔断器将转为半开状态，尝试放行少量请求。
     */
    private int timeWindow;

    /**
     * 在活跃统计时间窗口内触发熔断所需的最小请求数。
     *
     * @since 1.7.0
     */
    private int minRequestAmount = RuleConstant.DEGRADE_DEFAULT_MIN_REQUEST_AMOUNT;

    /**
     * RT 模式下慢请求比例的阈值。
     *
     * @since 1.8.0
     */
    private double slowRatioThreshold = 1.0d;

    /**
     * 统计时间窗口的间隔（毫秒）。
     *
     * @since 1.8.0
     */
    private int statIntervalMs = 1000;

    public int getGrade() {
        return grade;
    }

    public DegradeRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public double getCount() {
        return count;
    }

    public DegradeRule setCount(double count) {
        this.count = count;
        return this;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public DegradeRule setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }

    public int getMinRequestAmount() {
        return minRequestAmount;
    }

    public DegradeRule setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
        return this;
    }

    public double getSlowRatioThreshold() {
        return slowRatioThreshold;
    }

    public DegradeRule setSlowRatioThreshold(double slowRatioThreshold) {
        this.slowRatioThreshold = slowRatioThreshold;
        return this;
    }

    public int getStatIntervalMs() {
        return statIntervalMs;
    }

    public DegradeRule setStatIntervalMs(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        DegradeRule rule = (DegradeRule)o;
        return Double.compare(rule.count, count) == 0 &&
            timeWindow == rule.timeWindow &&
            grade == rule.grade &&
            minRequestAmount == rule.minRequestAmount &&
            Double.compare(rule.slowRatioThreshold, slowRatioThreshold) == 0 &&
            statIntervalMs == rule.statIntervalMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), count, timeWindow, grade, minRequestAmount,
            slowRatioThreshold, statIntervalMs);
    }

    @Override
    public String toString() {
        return "DegradeRule{" +
            "resource=" + getResource() +
            ", grade=" + grade +
            ", count=" + count +
            ", limitApp=" + getLimitApp() +
            ", timeWindow=" + timeWindow +
            ", minRequestAmount=" + minRequestAmount +
            ", slowRatioThreshold=" + slowRatioThreshold +
            ", statIntervalMs=" + statIntervalMs +
            '}';
    }
}
