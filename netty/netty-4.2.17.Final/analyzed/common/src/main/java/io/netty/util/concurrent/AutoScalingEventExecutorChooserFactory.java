/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A factory that creates auto-scaling {@link EventExecutorChooser} instances.
 * This chooser implements a dynamic, utilization-based auto-scaling strategy.
 * <p>
 * It enables the {@link io.netty.channel.EventLoopGroup} to automatically scale the number of active
 * {@link io.netty.channel.EventLoop} threads between a minimum and maximum threshold.
 * The scaling decision is based on the average utilization of the active threads, measured over a
 * configurable time window.
 * <p>
 * An {@code EventLoop} can be suspended if its utilization is consistently below the
 * {@code scaleDownThreshold}. Conversely, if the group's average utilization is consistently
 * above the {@code scaleUpThreshold}, a suspended thread will be automatically resumed to handle
 * the increased load.
 * <p>
 * To control the aggressiveness of scaling actions, the {@code maxRampUpStep} and {@code maxRampDownStep}
 * parameters limit the maximum number of threads that can be activated or suspended in a single scaling cycle.
 * Furthermore, to ensure decisions are based on sustained trends rather than transient spikes, the
 * {@code scalingPatienceCycles} defines how many consecutive monitoring windows a condition must be met
 * before a scaling action is triggered.
 *
 * <p>基于利用率的 EventLoop 自动伸缩选择器工厂：在 min/max 线程数之间，按监控窗口内的平均利用率决定挂起（scale down）或唤醒（scale up）线程。通过 {@code maxRampUpStep}/{@code maxRampDownStep} 限制单周期变化量，{@code scalingPatienceCycles} 要求条件连续满足若干周期后才触发，避免抖动。</p>
 */
public final class AutoScalingEventExecutorChooserFactory implements EventExecutorChooserFactory {

    /**
     * A container for the utilization metric of a single EventExecutor.
     * This object is intended to be created once and have its {@code utilization}
     * field updated periodically.
     *
     * <p>单个 {@link EventExecutor} 的利用率快照容器；监控任务周期性更新 {@code utilization}（0.0～1.0）。</p>
     */
    public static final class AutoScalingUtilizationMetric {
        private final EventExecutor executor;
        private final AtomicLong utilizationBits = new AtomicLong();

        AutoScalingUtilizationMetric(EventExecutor executor) {
            this.executor = executor;
        }

        /**
         * Returns the most recently calculated utilization for the associated executor.
         * @return a value from 0.0 to 1.0.
         *
         * <p>最近一次监控周期计算出的利用率。</p>
         */
        public double utilization() {
            return Double.longBitsToDouble(utilizationBits.get());
        }

        /**
         * Returns the {@link EventExecutor} this metric belongs too.
         * @return the executor.
         *
         * <p>关联的执行器实例。</p>
         */
        public EventExecutor executor() {
            return executor;
        }

        void setUtilization(double utilization) {
            long bits = Double.doubleToRawLongBits(utilization);
            utilizationBits.lazySet(bits);
        }
    }

    /** 唤醒挂起 EventLoop 用的空任务。 */
    private static final Runnable NO_OOP_TASK = () -> { };
    private final int minChildren;
    private final int maxChildren;
    private final long utilizationCheckPeriodNanos;
    private final double scaleDownThreshold;
    private final double scaleUpThreshold;
    private final int maxRampUpStep;
    private final int maxRampDownStep;
    private final int scalingPatienceCycles;

    /**
     * Creates a new factory for a scaling-enabled {@link EventExecutorChooser}.
     *
     * @param minThreads               the minimum number of threads to keep active.
     * @param maxThreads               the maximum number of threads to scale up to.
     * @param utilizationWindow        the period at which to check group utilization.
     * @param windowUnit               the unit for {@code utilizationWindow}.
     * @param scaleDownThreshold       the average utilization below which a thread may be suspended.
     * @param scaleUpThreshold         the average utilization above which a thread may be resumed.
     * @param maxRampUpStep            the maximum number of threads to add in one cycle.
     * @param maxRampDownStep          the maximum number of threads to remove in one cycle.
     * @param scalingPatienceCycles    the number of consecutive cycles a condition must be met before scaling.
     *
     * <p>校验 min≤max、阈值区间及 ramp 参数后保存配置。</p>
     */
    public AutoScalingEventExecutorChooserFactory(int minThreads, int maxThreads, long utilizationWindow,
                                                  TimeUnit windowUnit, double scaleDownThreshold,
                                                  double scaleUpThreshold, int maxRampUpStep, int maxRampDownStep,
                                                  int scalingPatienceCycles) {
        minChildren = ObjectUtil.checkPositiveOrZero(minThreads, "minThreads");
        maxChildren = ObjectUtil.checkPositive(maxThreads, "maxThreads");
        if (minThreads > maxThreads) {
            throw new IllegalArgumentException(String.format(
                    "minThreads: %d must not be greater than maxThreads: %d", minThreads, maxThreads));
        }
        utilizationCheckPeriodNanos = ObjectUtil.checkNotNull(windowUnit, "windowUnit")
                                                     .toNanos(ObjectUtil.checkPositive(utilizationWindow,
                                                                                       "utilizationWindow"));
        this.scaleDownThreshold = ObjectUtil.checkInRange(scaleDownThreshold, 0.0, 1.0, "scaleDownThreshold");
        this.scaleUpThreshold = ObjectUtil.checkInRange(scaleUpThreshold, 0.0, 1.0, "scaleUpThreshold");
        if (scaleDownThreshold >= scaleUpThreshold) {
            throw new IllegalArgumentException(
                    "scaleDownThreshold must be less than scaleUpThreshold: " +
                    scaleDownThreshold + " >= " + scaleUpThreshold);
        }
        this.maxRampUpStep = ObjectUtil.checkPositive(maxRampUpStep, "maxRampUpStep");
        this.maxRampDownStep = ObjectUtil.checkPositive(maxRampDownStep, "maxRampDownStep");
        this.scalingPatienceCycles = ObjectUtil.checkPositiveOrZero(scalingPatienceCycles, "scalingPatienceCycles");
    }

    @Override
    public EventExecutorChooser newChooser(EventExecutor[] executors) {
        return new AutoScalingEventExecutorChooser(executors);
    }

    /**
     * An immutable snapshot of the chooser's state. All state transitions
     * are managed by atomically swapping this object.
     *
     * <p>不可变状态快照：活跃线程数、下次唤醒起始索引、当前活跃执行器数组及其轮询选择器。通过 CAS 替换整个快照实现无锁状态迁移。</p>
     */
    private static final class AutoScalingState {
        final int activeChildrenCount;
        final long nextWakeUpIndex;
        final EventExecutor[] activeExecutors;
        final EventExecutorChooser activeExecutorsChooser;

        AutoScalingState(int activeChildrenCount, long nextWakeUpIndex, EventExecutor[] activeExecutors) {
            this.activeChildrenCount = activeChildrenCount;
            this.nextWakeUpIndex = nextWakeUpIndex;
            this.activeExecutors = activeExecutors;
            activeExecutorsChooser = DefaultEventExecutorChooserFactory.INSTANCE.newChooser(activeExecutors);
        }
    }

    private final class AutoScalingEventExecutorChooser implements ObservableEventExecutorChooser {
        private final EventExecutor[] executors;
        private final EventExecutorChooser allExecutorsChooser;
        private final AtomicReference<AutoScalingState> state;
        private final List<AutoScalingUtilizationMetric> utilizationMetrics;

        AutoScalingEventExecutorChooser(EventExecutor[] executors) {
            this.executors = executors;
            List<AutoScalingUtilizationMetric> metrics = new ArrayList<>(executors.length);
            for (EventExecutor executor : executors) {
                metrics.add(new AutoScalingUtilizationMetric(executor));
            }
            utilizationMetrics = Collections.unmodifiableList(metrics);
            allExecutorsChooser = DefaultEventExecutorChooserFactory.INSTANCE.newChooser(executors);

            AutoScalingState initialState = new AutoScalingState(maxChildren, 0L, executors);
            state = new AtomicReference<>(initialState);

            ScheduledFuture<?> utilizationMonitoringTask = GlobalEventExecutor.INSTANCE.scheduleAtFixedRate(
                    new UtilizationMonitor(), utilizationCheckPeriodNanos, utilizationCheckPeriodNanos,
                    TimeUnit.NANOSECONDS);

            if (executors.length > 0) {
                executors[0].terminationFuture().addListener(future -> utilizationMonitoringTask.cancel(false));
            }
        }

        /**
         * This method is only responsible for picking from the active executors list.
         * The monitor handles all scaling decisions.
         *
         * <p>仅从当前活跃列表轮询；伸缩决策由 {@link UtilizationMonitor} 后台任务负责。若无活跃线程则紧急唤醒一个并临时使用全量选择器。</p>
         */
        @Override
        public EventExecutor next() {
            // 读取当前状态快照
            AutoScalingState currentState = this.state.get();

            if (currentState.activeExecutors.length == 0) {
                // minChildren 为 0 且刚挂起最后一个活跃线程时的兜底路径 and the monitor has just suspended the last active thread.
                // To prevent an error and ensure the group can recover, we wake one up and use the
                // chooser that contains all executors as a safe temporary choice.
                tryScaleUpBy(1);
                return allExecutorsChooser.next();
            }
            return currentState.activeExecutorsChooser.next();
        }

        /**
         * Tries to increase the active thread count by waking up suspended executors.
         * This method is thread-safe and updates the state atomically.
         *
         * @param amount    The desired number of threads to add to the active count.
         *
         * <p>从 {@code nextWakeUpIndex} 起扫描挂起的 {@link SingleThreadEventExecutor}，投递空任务唤醒，CAS 更新活跃列表。</p>
         */
        private void tryScaleUpBy(int amount) {
            if (amount <= 0) {
                return;
            }

            for (;;) {
                AutoScalingState oldState = state.get();
                if (oldState.activeChildrenCount >= maxChildren) {
                    return;
                }

                int canAdd = Math.min(amount, maxChildren - oldState.activeChildrenCount);
                List<EventExecutor> wokenUp = new ArrayList<>(canAdd);
                final long startIndex = oldState.nextWakeUpIndex;

                for (int i = 0; i < executors.length; i++) {
                    EventExecutor child = executors[(int) Math.abs((startIndex + i) % executors.length)];

                    if (wokenUp.size() >= canAdd) {
                        break; // We have woken up all the threads we reserved.
                    }
                    if (child instanceof SingleThreadEventExecutor) {
                        SingleThreadEventExecutor stee = (SingleThreadEventExecutor) child;
                        if (stee.isSuspended()) {
                            stee.execute(NO_OOP_TASK);
                            wokenUp.add(stee);
                        }
                    }
                }

                if (wokenUp.isEmpty()) {
                    return;
                }

                // Create the new state.
                List<EventExecutor> newActiveList = new ArrayList<>(oldState.activeExecutors.length + wokenUp.size());
                Collections.addAll(newActiveList, oldState.activeExecutors);
                newActiveList.addAll(wokenUp);

                AutoScalingState newState = new AutoScalingState(
                        oldState.activeChildrenCount + wokenUp.size(),
                        startIndex + wokenUp.size(),
                        newActiveList.toArray(new EventExecutor[0]));

                if (state.compareAndSet(oldState, newState)) {
                    return;
                }
                // CAS 失败则重试
            }
        }

        @Override
        public int activeExecutorCount() {
            return state.get().activeChildrenCount;
        }

        @Override
        public List<AutoScalingUtilizationMetric> executorUtilizations() {
            return utilizationMetrics;
        }

        private final class UtilizationMonitor implements Runnable {
            private final List<SingleThreadEventExecutor> consistentlyIdleChildren = new ArrayList<>(maxChildren);
            private long lastCheckTimeNanos;

            @Override
            public void run() {
                if (executors.length == 0 || executors[0].isShuttingDown()) {
                    // 组正在关闭，停止伸缩决策
                    // The lifecycle listener on the terminationFuture will handle the final cancellation.
                    return;
                }

                // 计算距上次监控的实际经过时间
                final long now = executors[0].ticker().nanoTime();
                long totalTime;

                if (lastCheckTimeNanos == 0) {
                    // 首次运行用配置周期作为基准窗口 to avoid skipping the cycle.
                    totalTime = utilizationCheckPeriodNanos;
                } else {
                    // 后续运行用实际时间差
                    totalTime = now - lastCheckTimeNanos;
                }

                // 更新下次周期的时间戳
                lastCheckTimeNanos = now;

                if (totalTime <= 0) {
                    // 时钟异常则跳过本周期 or the interval is invalid.
                    return;
                }

                int consistentlyBusyChildren = 0;
                consistentlyIdleChildren.clear();

                final AutoScalingState currentState = state.get();

                for (int i = 0; i < executors.length; i++) {
                    EventExecutor child = executors[i];
                    if (!(child instanceof SingleThreadEventExecutor)) {
                        continue;
                    }

                    SingleThreadEventExecutor eventExecutor = (SingleThreadEventExecutor) child;

                    double utilization = 0.0;
                    if (!eventExecutor.isSuspended()) {
                        long activeTime = eventExecutor.getAndResetAccumulatedActiveTimeNanos();

                        if (activeTime == 0) {
                            long lastActivity = eventExecutor.getLastActivityTimeNanos();
                            long idleTime = now - lastActivity;

                            // 空闲时间小于监控窗口 ⇒ 窗口内仍有活跃时间
                            // it means it was active for the remainder of that window.
                            if (idleTime < totalTime) {
                                activeTime = totalTime - idleTime;
                            }
                            // 整窗空闲则利用率为 0
                        }

                        utilization = Math.min(1.0, (double) activeTime / totalTime);

                        if (utilization < scaleDownThreshold) {
                            // 低利用率：累加 idle 周期，重置 busy
                            int idleCycles = eventExecutor.getAndIncrementIdleCycles();
                            eventExecutor.resetBusyCycles();
                            if (idleCycles >= scalingPatienceCycles &&
                                eventExecutor.getNumOfRegisteredChannels() <= 0) {
                                consistentlyIdleChildren.add(eventExecutor);
                            }
                        } else if (utilization > scaleUpThreshold) {
                            // 高利用率：累加 busy 周期，重置 idle
                            int busyCycles = eventExecutor.getAndIncrementBusyCycles();
                            eventExecutor.resetIdleCycles();
                            if (busyCycles >= scalingPatienceCycles) {
                                consistentlyBusyChildren++;
                            }
                        } else {
                            // 正常区间：重置 idle/busy 计数
                            eventExecutor.resetIdleCycles();
                            eventExecutor.resetBusyCycles();
                        }
                    }

                    utilizationMetrics.get(i).setUtilization(utilization);
                }

                int currentActive = currentState.activeChildrenCount;

                // 基于连续多周期稳定状态做伸缩决策
                if (consistentlyBusyChildren > 0 && currentActive < maxChildren) {
                    // 扩容：存在连续 busy 的子线程
                    int threadsToAdd = Math.min(consistentlyBusyChildren, maxRampUpStep);
                    threadsToAdd = Math.min(threadsToAdd, maxChildren - currentActive);
                    if (threadsToAdd > 0) {
                        tryScaleUpBy(threadsToAdd);
                        // tryScaleUpBy 已更新状态
                        return; // Exit to avoid conflicting scale down logic in the same cycle.
                    }
                }

                boolean changed = false; // 缩容后需重建活跃列表
                if (!consistentlyIdleChildren.isEmpty() && currentActive > minChildren) {
                    // 缩容：存在连续 idle 且无注册 channel 的子线程

                    int threadsToRemove = Math.min(consistentlyIdleChildren.size(), maxRampDownStep);
                    threadsToRemove = Math.min(threadsToRemove, currentActive - minChildren);

                    for (int i = 0; i < threadsToRemove; i++) {
                        SingleThreadEventExecutor childToSuspend = consistentlyIdleChildren.get(i);
                        if (childToSuspend.trySuspend()) {
                            // 挂起后重置周期计数，避免唤醒后立即再挂
                            childToSuspend.resetBusyCycles();
                            childToSuspend.resetIdleCycles();
                            changed = true;
                        }
                    }
                }

                // 缩容或状态不一致时重建活跃执行器快照
                if (changed || currentActive != currentState.activeExecutors.length) {
                    rebuildActiveExecutors();
                }
            }

            /**
             * Atomically updates the state by creating a new snapshot with the current set of active executors.
             *
             * <p>扫描未挂起的执行器，CAS 替换 {@link AutoScalingState}。</p>
             */
            private void rebuildActiveExecutors() {
                for (;;) {
                    AutoScalingState oldState = state.get();
                    List<EventExecutor> active = new ArrayList<>(oldState.activeChildrenCount);
                    for (EventExecutor executor : executors) {
                        if (!executor.isSuspended()) {
                            active.add(executor);
                        }
                    }
                    EventExecutor[] newActiveExecutors = active.toArray(new EventExecutor[0]);

                    // 扫描得到的活跃数可能与快照不一致（并发修改）
                    // another thread likely changed it. We use the count from our fresh scan.
                    // 重建非扩容操作，保留 nextWakeUpIndex
                    AutoScalingState newState = new AutoScalingState(
                            newActiveExecutors.length, oldState.nextWakeUpIndex, newActiveExecutors);

                    if (state.compareAndSet(oldState, newState)) {
                        break;
                    }
                }
            }
        }
    }
}
