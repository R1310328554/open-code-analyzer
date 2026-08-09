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
package org.redisson.api;

import org.redisson.api.executor.TaskListener;
import org.redisson.config.Config;
import org.redisson.executor.SpringTasksInjector;
import org.redisson.executor.TasksInjector;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RExecutorService 工作线程配置。
 * 
 * @author Nikita Koksharov
 *
 */
public final class WorkerOptions {

    private int workers = 1;
    private ExecutorService executorService;
    private TasksInjector tasksInjector;
    private long taskTimeout;
    private long taskLateThreshold;
    private List<TaskListener> listeners = new ArrayList<>();
    
    private WorkerOptions() {
    }
    
    public static WorkerOptions defaults() {
        return new WorkerOptions();
    }
    
    public int getWorkers() {
        return workers;
    }

    /**
     * 定义用于执行任务的工作线程数量。
     * Default is <code>1</code>.
     * 
     * @param workers 工作线程数量
     * @return 当前实例
     */
    public WorkerOptions workers(int workers) {
        this.workers = workers;
        return this;
    }
    
    /**
     * 请改用 {@code tasksInjector(new SpringTasksInjector(beanFactory))}。
     *
     * @param beanFactory Spring BeanFactory 实例
     * @return 当前实例
     */
    @Deprecated
    public WorkerOptions beanFactory(BeanFactory beanFactory) {
        if (beanFactory != null) {
            this.tasksInjector = new SpringTasksInjector(beanFactory);
        }
        return this;
    }

    /**
     * 定义用于 CDI 注入的任务执行器；
     * 例如 Spring 的 {@code @Autowired}、{@code @Value} 或 JSR-330 的 {@code @Inject}。
     *
     * @param tasksInjector 任务注入器
     * @return 当前实例
     *
     * @see SpringTasksInjector
     */
    public WorkerOptions tasksInjector(TasksInjector tasksInjector) {
        this.tasksInjector = tasksInjector;
        return this;
    }

    public TasksInjector getTasksInjector() {
        return tasksInjector;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * 定义用于执行任务的自定义 {@link ExecutorService}；
     * 默认使用 {@link Config#setExecutor(ExecutorService)}。
     * 
     * @param executorService 自定义 ExecutorService
     * @return 当前实例
     */
    public WorkerOptions executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * 定义自任务开始执行起的超时时间。
     *
     * @param timeout 任务超时
     * @param unit 时间单位
     * @return 当前实例
     */
    public WorkerOptions taskTimeout(long timeout, TimeUnit unit) {
        this.taskTimeout = unit.toMillis(timeout);
        return this;
    }

    public long getTaskTimeout() {
        return taskTimeout;
    }

    /**
     * 定义定时任务的迟到阈值。
     * <p>
     * 若工作线程在计划执行时间超过该阈值后才领取任务，则跳过而非执行。
     * <p>
     * Default is <code>null</code> - disabled, all missed tasks run immediately.
     *
     * @param threshold 允许的最大迟到时间
     * @return 当前实例
     */
    public WorkerOptions taskLateThreshold(Duration threshold) {
        this.taskLateThreshold = threshold.toMillis();
        return this;
    }

    public long getTaskLateThreshold() {
        return taskLateThreshold;
    }

    /**
     * 添加任务监听器。
     *
     * @see org.redisson.api.executor.TaskSuccessListener
     * @see org.redisson.api.executor.TaskFailureListener
     * @see org.redisson.api.executor.TaskStartedListener
     * @see org.redisson.api.executor.TaskFinishedListener
     *
     * @param listener 任务监听器
     * @return 当前实例
     */
    public WorkerOptions addListener(TaskListener listener) {
        listeners.add(listener);
        return this;
    }

    public List<TaskListener> getListeners() {
        return listeners;
    }
}
