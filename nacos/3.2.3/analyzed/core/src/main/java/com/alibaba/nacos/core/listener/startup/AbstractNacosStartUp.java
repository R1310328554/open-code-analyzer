/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.listener.startup;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Nacos 启动阶段抽象基类，封装各阶段通用的启动状态与周期性日志输出。
 * <p>实现 {@link NacosStartUp}，子类只需关注阶段名称与启动信息文案。</p>
 * Abstract Nacos start up.
 *
 * @author xiweng.yy
 */
public abstract class AbstractNacosStartUp implements NacosStartUp {
    
    /** 当前启动阶段标识，如 core、web、console。 */
    private final String phase;
    
    /** 启动过程中每秒打印 "starting..." 的定时调度器。 */
    private volatile ScheduledExecutorService startLoggingScheduledExecutor;
    
    /** 是否仍处于启动中（用于控制定时日志是否继续输出）。 */
    private volatile boolean starting;
    
    /** 阶段开始启动的时间戳（毫秒），供子类计算耗时。 */
    private volatile long startTimestamp;
    
    /** 指定启动阶段名称并初始化基类状态。 */
    protected AbstractNacosStartUp(String phase) {
        this.phase = phase;
    }
    
    /** 返回本实现对应的启动阶段标识。 */
    @Override
    public String startUpPhase() {
        return phase;
    }
    
    /** 标记阶段进入启动中，创建单线程定时器周期性输出启动日志。 */
    @Override
    public void starting() {
        starting = true;
        startTimestamp = System.currentTimeMillis();
        this.startLoggingScheduledExecutor = ExecutorFactory.newSingleScheduledExecutorService(
            new NameThreadFactory(String.format("com.alibaba.nacos.%s.nacos-starting", phase)));
    }
    
    /** 每秒输出一次 "{阶段名} is starting..." 直至 {@link #started()} 或 {@link #failed} 被调用。 */
    @Override
    public void logStartingInfo(Logger logger) {
        startLoggingScheduledExecutor.scheduleWithFixedDelay(() -> {
            if (starting) {
                logger.info(String.format("%s is starting...", getPhaseNameInStartingInfo()));
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /** 启动完成：停止定时日志并关闭调度线程池。 */
    @Override
    public void started() {
        starting = false;
        closeExecutor();
    }
    
    /** 启动失败：停止日志、关闭调度器并关闭 Spring 上下文。 */
    @Override
    public void failed(Throwable exception, ConfigurableApplicationContext context) {
        starting = false;
        closeExecutor();
        context.close();
    }
    
    /** 供子类获取阶段启动起始时间戳。 */
    protected long getStartTimestamp() {
        return startTimestamp;
    }
    
    /**
     * 启动日志中展示的阶段名称（如 "Nacos Server"、"Nacos Server API"）。
     *
     * @return phase name
     */
    protected abstract String getPhaseNameInStartingInfo();
    
    /** 立即关闭启动日志调度线程池。 */
    private void closeExecutor() {
        startLoggingScheduledExecutor.shutdownNow();
    }
}
