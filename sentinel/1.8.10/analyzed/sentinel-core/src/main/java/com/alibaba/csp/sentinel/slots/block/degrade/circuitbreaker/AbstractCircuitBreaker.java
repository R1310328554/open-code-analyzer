/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.csp.sentinel.util.function.BiConsumer;

/**
 * 熔断器抽象基类，实现 {@link CircuitBreaker} 的状态机与通用逻辑。
 * <p>管理 CLOSED / OPEN / HALF_OPEN 三态转换，并通知 {@link CircuitBreakerStateChangeObserver}。</p>
 *
 * @author Eric Zhao
 * @since 1.8.0
 */
public abstract class AbstractCircuitBreaker implements CircuitBreaker {
    protected static final double MAX_RATIO = 1.0d;

    protected final DegradeRule rule;
    protected final int recoveryTimeoutMs;

    private final EventObserverRegistry observerRegistry;

    protected final AtomicReference<State> currentState = new AtomicReference<>(State.CLOSED);
    protected volatile long nextRetryTimestamp;

    /** 使用默认 {@link EventObserverRegistry} 实例构造熔断器。 */
    public AbstractCircuitBreaker(DegradeRule rule) {
        this(rule, EventObserverRegistry.getInstance());
    }

    /** 指定状态变更观察者注册表构造熔断器（包内可见，便于测试）。 */
    AbstractCircuitBreaker(DegradeRule rule, EventObserverRegistry observerRegistry) {
        AssertUtil.notNull(observerRegistry, "observerRegistry cannot be null");
        if (!DegradeRuleManager.isValidRule(rule)) {
            throw new IllegalArgumentException("Invalid DegradeRule: " + rule);
        }
        this.observerRegistry = observerRegistry;
        this.rule = rule;
        this.recoveryTimeoutMs = rule.getTimeWindow() * 1000;
    }

    /** 返回关联的降级规则。 */
    @Override
    public DegradeRule getRule() {
        return rule;
    }

    /** 返回当前熔断器状态。 */
    @Override
    public State currentState() {
        return currentState.get();
    }

    /** 尝试获取调用许可：CLOSED 直接放行；OPEN 在恢复超时后转入 HALF_OPEN 并允许探测请求。 */
    @Override
    public boolean tryPass(Context context) {
        // Template implementation.
        if (currentState.get() == State.CLOSED) {
            return true;
        }
        if (currentState.get() == State.OPEN) {
            // For half-open state we allow a request for probing.
            return retryTimeoutArrived() && fromOpenToHalfOpen(context);
        }
        return false;
    }

    /**
     * 重置统计数据。
     */
    abstract void resetStat();

    /** 判断是否已到达下次重试时间点。 */
    protected boolean retryTimeoutArrived() {
        return TimeUtil.currentTimeMillis() >= nextRetryTimestamp;
    }

    /** 根据恢复超时时间更新下次重试时间戳。 */
    protected void updateNextRetryTimestamp() {
        this.nextRetryTimestamp = TimeUtil.currentTimeMillis() + recoveryTimeoutMs;
    }

    /** 从 CLOSED 转换到 OPEN，并通知观察者。 */
    protected boolean fromCloseToOpen(double snapshotValue) {
        State prev = State.CLOSED;
        if (currentState.compareAndSet(prev, State.OPEN)) {
            updateNextRetryTimestamp();

            notifyObservers(prev, State.OPEN, snapshotValue);
            return true;
        }
        return false;
    }

    /** 从 OPEN 转换到 HALF_OPEN，注册 Entry 终止钩子以处理被后续规则阻断的探测请求。 */
    protected boolean fromOpenToHalfOpen(Context context) {
        if (currentState.compareAndSet(State.OPEN, State.HALF_OPEN)) {
            notifyObservers(State.OPEN, State.HALF_OPEN, null);
            Entry entry = context.getCurEntry();
            entry.whenTerminate(new BiConsumer<Context, Entry>() {
                @Override
                public void accept(Context context, Entry entry) {
                    // Note: This works as a temporary workaround for https://github.com/alibaba/Sentinel/issues/1638
                    // Without the hook, the circuit breaker won't recover from half-open state in some circumstances
                    // when the request is actually blocked by upcoming rules (not only degrade rules).
                    if (entry.getBlockError() != null) {
                        // Fallback to OPEN due to detecting request is blocked
                        currentState.compareAndSet(State.HALF_OPEN, State.OPEN);
                        notifyObservers(State.HALF_OPEN, State.OPEN, 1.0d);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void notifyObservers(CircuitBreaker.State prevState, CircuitBreaker.State newState, Double snapshotValue) {
        for (CircuitBreakerStateChangeObserver observer : observerRegistry.getStateChangeObservers()) {
            observer.onStateChange(prevState, newState, rule, snapshotValue);
        }
    }

    /** 从 HALF_OPEN 回退到 OPEN。 */
    protected boolean fromHalfOpenToOpen(double snapshotValue) {
        if (currentState.compareAndSet(State.HALF_OPEN, State.OPEN)) {
            updateNextRetryTimestamp();
            notifyObservers(State.HALF_OPEN, State.OPEN, snapshotValue);
            return true;
        }
        return false;
    }

    /** 从 HALF_OPEN 恢复到 CLOSED，并重置统计。 */
    protected boolean fromHalfOpenToClose() {
        if (currentState.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
            resetStat();
            notifyObservers(State.HALF_OPEN, State.CLOSED, null);
            return true;
        }
        return false;
    }

    /** 根据当前状态将熔断器转换到 OPEN。 */
    protected void transformToOpen(double triggerValue) {
        State cs = currentState.get();
        switch (cs) {
            case CLOSED:
                fromCloseToOpen(triggerValue);
                break;
            case HALF_OPEN:
                fromHalfOpenToOpen(triggerValue);
                break;
            default:
                break;
        }
    }
}
