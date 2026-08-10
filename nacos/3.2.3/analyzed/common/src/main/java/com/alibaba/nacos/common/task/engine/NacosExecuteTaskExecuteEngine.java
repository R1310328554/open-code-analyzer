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
import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.common.utils.ThreadUtils;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Nacos 即时任务执行引擎：按 tag 的 hash 分片到多个
 * {@link TaskExecuteWorker}，有注册处理器时同步调用 {@link com.alibaba.nacos.common.task.NacosTaskProcessor#process}，
 * 否则将 {@link com.alibaba.nacos.common.task.AbstractExecuteTask} 放入 worker 队列异步执行。
 * Nacos execute task execute engine.
 *
 * @author xiweng.yy
 */
public class NacosExecuteTaskExecuteEngine
    extends AbstractNacosTaskExecuteEngine<AbstractExecuteTask> {
    
    /** 分片 worker 数组，数量由 dispatchWorkerCount 决定 */
    private final TaskExecuteWorker[] executeWorkers;
    
    /** 按 CPU 核数自动选择 worker 数量 */
    public NacosExecuteTaskExecuteEngine(String name, Logger logger) {
        this(name, logger, ThreadUtils.getSuitableThreadCount(1));
    }
    
    /**
     * 指定 worker 分片数，每个 mod 对应一个 {@link TaskExecuteWorker}。
     *
     * @param name                线程名前缀
     * @param logger              日志
     * @param dispatchWorkerCount 并行 worker 数量
     */
        super(logger);
        executeWorkers = new TaskExecuteWorker[dispatchWorkerCount];
        for (int mod = 0; mod < dispatchWorkerCount; ++mod) {
            executeWorkers[mod] =
                new TaskExecuteWorker(name, mod, dispatchWorkerCount, getEngineLog());
        }
    }
    
    /** 各 worker 待处理任务数之和 */
    @Override
    public int size() {
        int result = 0;
        for (TaskExecuteWorker each : executeWorkers) {
            result += each.pendingTaskCount();
        }
        return result;
    }
    
    /** 是否无任何待处理即时任务 */
    @Override
    public boolean isEmpty() {
        return 0 == size();
    }
    
    /** 有处理器则同步 process，否则按 tag hash 分派到 worker 队列 */
    @Override
    public void addTask(Object tag, AbstractExecuteTask task) {
        NacosTaskProcessor processor = getProcessor(tag);
        if (null != processor) {
            processor.process(task);
            return;
        }
        TaskExecuteWorker worker = getWorker(tag);
        worker.process(task);
    }
    
    /** 根据 tag.hashCode 取模选择 worker */
    private TaskExecuteWorker getWorker(Object tag) {
        int idx = (tag.hashCode() & Integer.MAX_VALUE) % workersCount();
        return executeWorkers[idx];
    }
    
    /** 返回 worker 数组长度 */
    private int workersCount() {
        return executeWorkers.length;
    }
    
    /** 即时引擎不支持按 key 移除任务 */
    @Override
    public AbstractExecuteTask removeTask(Object key) {
        throw new UnsupportedOperationException("ExecuteTaskEngine do not support remove task");
    }
    
    /** 即时引擎不支持枚举全部任务 key */
    @Override
    public Collection<Object> getAllTaskKeys() {
        throw new UnsupportedOperationException(
            "ExecuteTaskEngine do not support get all task keys");
    }
    
    /** 关闭所有 worker 线程 */
    @Override
    public void shutdown() throws NacosException {
        for (TaskExecuteWorker each : executeWorkers) {
            each.shutdown();
        }
    }
    
    /**
     * 汇总各 worker 名称与 pending 任务数，便于运维诊断。
     *
     * @return 多行状态文本
     */
    public String workersStatus() {
        StringBuilder sb = new StringBuilder();
        for (TaskExecuteWorker worker : executeWorkers) {
            sb.append(worker.status()).append('\n');
        }
        return sb.toString();
    }
}
