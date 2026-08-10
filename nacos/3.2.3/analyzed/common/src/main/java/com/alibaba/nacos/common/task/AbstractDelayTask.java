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

package com.alibaba.nacos.common.task;

/**
 * 可延迟执行且支持合并的抽象任务基类：通过 {@link #taskInterval} 与
 * {@link #lastProcessTime} 控制两次处理之间的最小间隔，{@link #shouldProcess()}
 * 仅在间隔满足时返回 true。同 key 的新任务可通过 {@link #merge(AbstractDelayTask)}
 * 与队列中已有任务合并，减少重复调度开销。
 * Abstract task which can delay and merge.
 *
 * @author huali
 * @author xiweng.yy
 */
public abstract class AbstractDelayTask implements NacosTask {
    
    /** 两次任务处理之间的最小时间间隔，单位毫秒 */

    private long taskInterval;
    
    /** 上次成功处理该任务的时间戳，单位毫秒 */

    private long lastProcessTime;
    
    /** 默认任务间隔：1000 毫秒（1 秒） */

    protected static final long INTERVAL = 1000L;
    
    /**
     * 将传入任务合并到当前任务（如累加计数、合并 payload）。
     *
     * @param task 待合并的同类型延迟任务
     */
    public abstract void merge(AbstractDelayTask task);
    
    /** 设置任务处理间隔（毫秒） */
    public void setTaskInterval(long interval) {
        this.taskInterval = interval;
    }
    
    /** 获取任务处理间隔（毫秒） */
    public long getTaskInterval() {
        return this.taskInterval;
    }
    
    /** 更新上次处理时间戳，用于重试或延迟调度 */
    public void setLastProcessTime(long lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    
    /** 获取上次处理时间戳 */
    public long getLastProcessTime() {
        return this.lastProcessTime;
    }
    
    /** 当前时间与上次处理时间之差是否已达到 {@link #taskInterval} */
    @Override
    public boolean shouldProcess() {
        return (System.currentTimeMillis() - this.lastProcessTime >= this.taskInterval);
    }
    
}
