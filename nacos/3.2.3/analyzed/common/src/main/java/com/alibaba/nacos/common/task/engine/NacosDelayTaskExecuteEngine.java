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

package com.alibaba.nacos.common.task.engine;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.task.AbstractDelayTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nacos 延迟任务执行引擎：单线程定时扫描 {@link #tasks} 队列，
 * 对满足 {@link com.alibaba.nacos.common.task.AbstractDelayTask#shouldProcess()} 的任务
 * 调用对应 {@link com.alibaba.nacos.common.task.NacosTaskProcessor}；失败或异常时
 * 更新 lastProcessTime 并重新入队。同 key 新任务通过 {@link AbstractDelayTask#merge} 合并。
 * Nacos delay task execute engine.
 *
 * @author xiweng.yy
 */
public class NacosDelayTaskExecuteEngine extends AbstractNacosTaskExecuteEngine<AbstractDelayTask> {
    
    /** 定时扫描并处理延迟任务的调度线程池 */
    private final ScheduledExecutorService processingExecutor;
    
    /** 任务 key → 延迟任务实例的并发存储 */
    protected final ConcurrentHashMap<Object, AbstractDelayTask> tasks;
    
    /** 保护 tasks 读写与 size 统计的可重入锁 */
    protected final ReentrantLock lock = new ReentrantLock();
    
    /** 使用默认容量 32、扫描间隔 100ms 构造引擎 */
    public NacosDelayTaskExecuteEngine(String name) {
        this(name, null);
    }
    
    /** 指定日志 Logger，其余参数同双参 name 构造 */
    public NacosDelayTaskExecuteEngine(String name, Logger logger) {
        this(name, 32, logger, 100L);
    }
    
    /**
     * 完整构造：初始化任务 map、单线程调度器，按 processInterval 毫秒周期执行扫描。
     *
     * @param name            线程工厂名称前缀
     * @param initCapacity    任务 map 初始容量
     * @param logger          日志实例
     * @param processInterval 扫描间隔（毫秒）
     */
        super(logger);
        tasks = new ConcurrentHashMap<>(initCapacity);
        processingExecutor =
            ExecutorFactory.newSingleScheduledExecutorService(new NameThreadFactory(name));
        processingExecutor
            .scheduleWithFixedDelay(new ProcessRunnable(), processInterval, processInterval,
                TimeUnit.MILLISECONDS);
    }
    
    /** 当前队列中延迟任务数量（加锁统计） */
    @Override
    public int size() {
        lock.lock();
        try {
            return tasks.size();
        } finally {
            lock.unlock();
        }
    }
    
    /** 队列是否为空 */
    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return tasks.isEmpty();
        } finally {
            lock.unlock();
        }
    }
    
    /** 移除并返回可处理的任务；未到执行时间则返回 null */
    @Override
    public AbstractDelayTask removeTask(Object key) {
        lock.lock();
        try {
            AbstractDelayTask task = tasks.get(key);
            if (null != task && task.shouldProcess()) {
                return tasks.remove(key);
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }
    
    /** 返回当前所有任务 key 的快照集合 */
    @Override
    public Collection<Object> getAllTaskKeys() {
        Collection<Object> keys = new HashSet<>();
        lock.lock();
        try {
            keys.addAll(tasks.keySet());
        } finally {
            lock.unlock();
        }
        return keys;
    }
    
    /** 清空任务并关闭调度线程池 */
    @Override
    public void shutdown() throws NacosException {
        tasks.clear();
        processingExecutor.shutdown();
    }
    
    /** 添加或合并延迟任务：同 key 存在时调用 merge 后覆盖 */
    @Override
    public void addTask(Object key, AbstractDelayTask newTask) {
        lock.lock();
        try {
            AbstractDelayTask existTask = tasks.get(key);
            if (null != existTask) {
                newTask.merge(existTask);
            }
            tasks.put(key, newTask);
        } finally {
            lock.unlock();
        }
    }
    
    /** 扫描所有 key，取出可处理任务并委托处理器；失败则重试入队 */

    protected void processTasks() {
        Collection<Object> keys = getAllTaskKeys();
        for (Object taskKey : keys) {
            AbstractDelayTask task = removeTask(taskKey);
            if (null == task) {
                continue;
            }
            NacosTaskProcessor processor = getProcessor(taskKey);
            try {
                // 处理失败时重新入队等待下次调度
                if (!processor.process(task)) {
                    retryFailedTask(taskKey, task);
                }
            } catch (Throwable e) {
                getEngineLog().error("Nacos task execute error ", e);
                retryFailedTask(taskKey, task);
            }
        }
    }
    
    /** 更新 lastProcessTime 后将失败任务重新加入队列 */
    private void retryFailedTask(Object key, AbstractDelayTask task) {
        task.setLastProcessTime(System.currentTimeMillis());
        addTask(key, task);
    }
    
    /** 定时触发的任务扫描 Runnable，异常仅记录日志不中断调度 */
    private class ProcessRunnable implements Runnable {
        
        @Override
        public void run() {
            try {
                processTasks();
            } catch (Throwable e) {
                getEngineLog().error(e.toString(), e);
            }
        }
    }
}
