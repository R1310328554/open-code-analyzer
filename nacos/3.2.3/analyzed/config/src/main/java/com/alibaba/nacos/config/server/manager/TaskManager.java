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

package com.alibaba.nacos.config.server.manager;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.task.AbstractDelayTask;
import com.alibaba.nacos.common.task.engine.NacosDelayTaskExecuteEngine;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.monitor.MetricsMonitor;
import com.alibaba.nacos.config.server.utils.LogUtil;
import org.slf4j.Logger;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 延迟任务管理器：基于单线程引擎顺序执行 dump 等后台任务，
 * 提供 await 阻塞、JMX 监控与 dump 队列指标上报。
 * TaskManager, is aim to process the task which is need to be done. And this class process the task by single thread to
 * ensure task should be process successfully.
 *
 * @author huali
 */
public final class TaskManager extends NacosDelayTaskExecuteEngine implements TaskManagerMBean {
    
    private static final Logger LOGGER = LogUtil.DEFAULT_LOG;
    
    private String name;
    
    Condition notEmpty = this.lock.newCondition();
    
    /**
     * 创建命名任务管理器，内部使用 32 容量延迟队列。
     *
     * @param name 管理器名称（亦用于 JMX ObjectName）
     */
    public TaskManager(String name) {
        super(name, 32, LOGGER, 100L);
        this.name = name;
    }
    
    /** 关闭任务引擎，忽略 shutdown 过程中的 NacosException。Close task manager. */
    
    public void close() {
        try {
            super.shutdown();
        } catch (NacosException ignored) {
        }
    }
    
    /**
     * 阻塞直至任务队列为空（Condition 等待）。
     *
     * @throws InterruptedException InterruptedException.
     */
    public void await() throws InterruptedException {
        this.lock.lock();
        try {
            while (!this.isEmpty()) {
                this.notEmpty.await();
            }
        } finally {
            this.lock.unlock();
        }
    }
    
    /**
     * 带超时的队列排空等待。
     *
     * @param timeout timeout value.
     * @param unit    time unit.
     * @return 是否在超时前等到队列空
     * @throws InterruptedException InterruptedException.
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        this.lock.lock();
        boolean isawait = false;
        try {
            while (!this.isEmpty()) {
                isawait = this.notEmpty.await(timeout, unit);
            }
            return isawait;
        } finally {
            this.lock.unlock();
        }
    }
    
    /** 添加延迟任务并更新 dump 监控 gauge */
    @Override
    public void addTask(Object key, AbstractDelayTask newTask) {
        super.addTask(key, newTask);
        MetricsMonitor.getDumpTaskMonitor().set(tasks.size());
    }
    
    /** 移除任务并更新 dump 监控 gauge */
    @Override
    public AbstractDelayTask removeTask(Object key) {
        AbstractDelayTask result = super.removeTask(key);
        MetricsMonitor.getDumpTaskMonitor().set(tasks.size());
        return result;
    }
    
    /** 批处理任务后刷新监控并在队列空时 signalAll */
    @Override
    protected void processTasks() {
        super.processTasks();
        MetricsMonitor.getDumpTaskMonitor().set(tasks.size());
        if (tasks.isEmpty()) {
            this.lock.lock();
            try {
                this.notEmpty.signalAll();
            } finally {
                this.lock.unlock();
            }
        }
    }
    
    /** JMX：返回各任务类型及最后处理时间的文本摘要 */
    @Override
    public String getTaskInfos() {
        StringBuilder sb = new StringBuilder();
        for (Object taskType : getAllProcessorKey()) {
            sb.append(taskType).append(':');
            AbstractDelayTask task = this.tasks.get(taskType);
            if (task != null) {
                sb.append(new Date(task.getLastProcessTime()));
            } else {
                sb.append("finished");
            }
            sb.append(Constants.NACOS_LINE_SEPARATOR);
        }
        
        return sb.toString();
    }
    
    /** 向 Platform MBeanServer 注册本 TaskManager。Init and register the mbean object. */
    
    public void init() {
        try {
            ObjectName oName =
                new ObjectName(this.name + ":type=" + TaskManager.class.getSimpleName());
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, oName);
        } catch (Exception e) {
            LOGGER.error("registerMBean_fail", e);
        }
    }
}
