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
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 即时任务执行 Worker：内置有界阻塞队列（容量 32768）与常驻消费线程，
 * 实现 {@link com.alibaba.nacos.common.task.NacosTaskProcessor} 将 {@link AbstractExecuteTask}
 * 入队后由 {@link InnerWorker} 顺序执行；单任务耗时超过 1 秒会 WARN 告警。
 * Nacos execute task execute worker.
 *
 * @author xiweng.yy
 */
public final class TaskExecuteWorker implements NacosTaskProcessor, Closeable {
    
    /** 任务队列最大容量：32768（2^15） */

    private static final int QUEUE_CAPACITY = 1 << 15;
    
    /** Worker 日志 */
    private final Logger log;
    
    /** Worker 唯一名称，格式 name_mod%total */
    private final String name;
    
    /** 待执行 Runnable 任务队列 */
    private final BlockingQueue<Runnable> queue;
    
    /** 关闭标志，true 时 InnerWorker 退出循环 */
    private final AtomicBoolean closed;
    
    /** 后台消费线程实例 */
    private final InnerWorker realWorker;
    
    /** 使用默认 Logger 构造 worker */
    public TaskExecuteWorker(final String name, final int mod, final int total) {
        this(name, mod, total, null);
    }
    
    /**
     * 构造并启动消费线程；name 后缀为 mod%total 便于区分分片。
     *
     * @param name   名称前缀
     * @param mod    当前分片序号
     * @param total  分片总数
     * @param logger 日志，null 时用默认
     */
        this.name = name + "_" + mod + "%" + total;
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.closed = new AtomicBoolean(false);
        this.log = null == logger ? LoggerFactory.getLogger(TaskExecuteWorker.class) : logger;
        realWorker = new InnerWorker(this.name);
        realWorker.start();
    }
    
    /** 返回 worker 名称 */
    public String getName() {
        return name;
    }
    
    /** 将 AbstractExecuteTask 放入队列；其他类型忽略 */
    @Override
    public boolean process(NacosTask task) {
        if (task instanceof AbstractExecuteTask) {
            putTask((Runnable) task);
        }
        return true;
    }
    
    /** 阻塞入队，中断时记录错误日志 */
    private void putTask(Runnable task) {
        try {
            queue.put(task);
        } catch (InterruptedException ire) {
            log.error(ire.toString(), ire);
        }
    }
    
    /** 当前队列中待执行任务数 */
    public int pendingTaskCount() {
        return queue.size();
    }
    
    /** 返回名称与 pending 任务数的可读状态行 */

    public String status() {
        return getName() + ", pending tasks: " + pendingTaskCount();
    }
    
    /** 清空队列、置 closed 并中断消费线程 */
    @Override
    public void shutdown() throws NacosException {
        queue.clear();
        closed.compareAndSet(false, true);
        realWorker.interrupt();
    }
    
    /** 后台线程：循环 take 队列任务并 run，记录慢任务与失败 */

    private class InnerWorker extends Thread {
        
        InnerWorker(String name) {
            setDaemon(false);
            setName(name);
        }
        
        @Override
        public void run() {
            while (!closed.get()) {
                try {
                    Runnable task = queue.take();
                    long begin = System.currentTimeMillis();
                    task.run();
                    long duration = System.currentTimeMillis() - begin;
                    if (duration > 1000L) {
                        log.warn("task {} takes {}ms", task, duration);
                    }
                } catch (InterruptedException e) {
                    // [issue #13752] 关闭时中断异常不打印堆栈
                } catch (Throwable e) {
                    log.error("[TASK-FAILED] " + e, e);
                }
            }
        }
    }
}
