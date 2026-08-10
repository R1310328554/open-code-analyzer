/*
 * Copyright 2016 The Netty Project
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

import io.netty.util.concurrent.AutoScalingEventExecutorChooserFactory.AutoScalingUtilizationMetric;

import java.util.List;

/**
 * Factory that creates new {@link EventExecutorChooser}s.
 *
 * <p>为 {@link EventExecutorGroup} 创建 {@link EventExecutorChooser} 的工厂，
 * 决定新连接/任务绑定到哪个 EventLoop（轮询、哈希、自动扩缩容等策略由具体实现决定）。</p>
 */
public interface EventExecutorChooserFactory {

    /**
     * Returns a new {@link EventExecutorChooser}.
     *
     * <p>基于给定 executor 数组创建选择器实例。</p>
     */
    EventExecutorChooser newChooser(EventExecutor[] executors);

    /**
     * Chooses the next {@link EventExecutor} to use.
     *
     * <p>从一组 EventExecutor 中选出下一个要使用的实例。</p>
     */
    interface EventExecutorChooser {

        /**
         * Returns the new {@link EventExecutor} to use.
         *
         * <p>返回本次应使用的 {@link EventExecutor}。</p>
         */
        EventExecutor next();
    }

    /**
     * An {@link EventExecutorChooser} that exposes metrics for observation.
     *
     * <p>可观测的选择器：暴露活跃 executor 数量与各 executor 利用率，供自动扩缩容决策使用。</p>
     */
    interface ObservableEventExecutorChooser extends EventExecutorChooser {

        /**
         * Returns the current number of active {@link EventExecutor}s.
         * @return the number of active executors.
         *
         * <p>当前活跃 {@link EventExecutor} 数量。</p>
         */
        int activeExecutorCount();

        /**
         * Returns a list containing the last calculated utilization for each
         * {@link EventExecutor} in the group.
         *
         * @return an umodifiable view of the executor utilizations.
         *
         * <p>各 executor 最近一次计算的利用率指标列表（只读视图）。</p>
         */
        List<AutoScalingUtilizationMetric> executorUtilizations();
    }
}
