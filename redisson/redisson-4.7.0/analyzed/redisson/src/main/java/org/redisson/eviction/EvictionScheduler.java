/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.eviction;

import org.redisson.api.MapCacheOptions;
import org.redisson.command.CommandAsyncExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 过期条目清理调度器。
 * <p>
 * 在 5 秒至 2 小时的可调间隔内删除 Redis 中已过期的 key；
 * 根据每次清理数量动态调整下次执行延迟（见 {@link EvictionTask}）。
 *
 * @author Nikita Koksharov
 *
 */
public final class EvictionScheduler {

    /** 按对象名称索引的清理任务映射（同一名称仅注册一次）。 */
    private final Map<String, EvictionTask> tasks = new ConcurrentHashMap<>();
    /** 异步命令执行器，供各 EvictionTask 访问 Redis。 */
    private final CommandAsyncExecutor executor;

    /** @param executor Redisson 异步命令执行器 */
    public EvictionScheduler(CommandAsyncExecutor executor) {
        this.executor = executor;
    }

    /** 按名称注册清理任务，已存在则跳过（computeIfAbsent 保证幂等）。 */
    private void addTask(String name, Supplier<EvictionTask> supplier) {
        tasks.computeIfAbsent(name, k -> {
            EvictionTask task = supplier.get();
            task.schedule();
            return task;
        });
    }

    /** 为 RMultimap 注册过期清理任务。 */
    public void scheduleCleanMultimap(String name, String timeoutSetName) {
        addTask(name, () -> new MultimapEvictionTask(name, timeoutSetName, executor));
    }

    /** 为 JCache 兼容结构注册清理任务并发布过期事件。 */
    public void scheduleJCache(String name, String timeoutSetName, String expiredChannelName) {
        addTask(name, () -> new JCacheEvictionTask(name, timeoutSetName, expiredChannelName, executor));
    }

    /** 为 RTimeSeries 注册过期清理任务。 */
    public void scheduleTimeSeries(String name, String timeoutSetName) {
        addTask(name, () -> new TimeSeriesEvictionTask(name, timeoutSetName, executor));
    }

    /** 为 RScoredSortedSet 注册带时间偏移的清理任务。 */
    public void schedule(String name, long shiftInMilliseconds) {
        addTask(name, () -> new ScoredSetEvictionTask(name, executor, shiftInMilliseconds));
    }

    /** 为 RSetCache 注册清理任务并通过指定命令发布过期通知。 */
    public void scheduleSetCache(String name, String expiredChannelName, String publishCommand) {
        addTask(name, () -> new SetCacheEvictionTask(name, expiredChannelName, executor, publishCommand));
    }

    /**
     * 为 RMapCache 注册综合清理任务（TTL、maxIdle、最后访问时间等）。
     *
     * @param options 可为 null；非 null 时读取 removeEmptyEvictionTask 选项
     */
        boolean removeEmpty;
        if (options != null) {
            removeEmpty = options.isRemoveEmptyEvictionTask();
        } else {
            removeEmpty = false;
        }

        addTask(name, () -> new MapCacheEvictionTask(name, timeoutSetName, maxIdleSetName, expiredChannelName, lastAccessTimeSetName,
                executor, removeEmpty, this, publishCommand));
    }

    /** 取消并移除指定名称的清理任务。 */
    public void remove(String name) {
        tasks.computeIfPresent(name, (k, task) -> {
            task.cancel();
            return null;
        });
    }

}
