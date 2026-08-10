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

import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;

import java.util.Collection;

/**
 * Nacos 任务执行引擎接口：统一管理处理器注册、任务入队/出队及生命周期。
 * 实现类包括 {@link NacosDelayTaskExecuteEngine}（延迟合并）与
 * {@link NacosExecuteTaskExecuteEngine}（即时分片）。继承 {@link com.alibaba.nacos.common.lifecycle.Closeable} 支持 shutdown。
 * Nacos task execute engine.
 *
 * @author xiweng.yy
 */
public interface NacosTaskExecuteEngine<T extends NacosTask> extends Closeable {
    
    /**
     * 获取引擎中当前待处理（或排队）任务数量。
     *
     * @return 任务数量
     */
    int size();
    
    /**
     * 判断引擎是否无待处理任务。
     *
     * @return 无任务为 true
     */
    boolean isEmpty();
    
    /**
     * 为指定任务 key 注册 {@link NacosTaskProcessor}。
     *
     * @param key           任务标识
     * @param taskProcessor 处理器实例
     */
    void addProcessor(Object key, NacosTaskProcessor taskProcessor);
    
    /**
     * 移除指定 key 的任务处理器。
     *
     * @param key 任务标识
     */
    void removeProcessor(Object key);
    
    /**
     * 按 key 获取处理器；不存在时返回默认处理器。
     *
     * @param key 任务标识
     * @return 匹配的处理器或默认处理器
     */
    NacosTaskProcessor getProcessor(Object key);
    
    /**
     * 返回所有已注册处理器的 key 集合。
     *
     * @return 处理器 key 集合
     */
    Collection<Object> getAllProcessorKey();
    
    /**
     * 设置默认处理器：key 无专属处理器时使用。
     *
     * @param defaultTaskProcessor 默认处理器
     */
    void setDefaultTaskProcessor(NacosTaskProcessor defaultTaskProcessor);
    
    /**
     * 向引擎提交任务。
     *
     * @param key  任务标识
     * @param task 任务实例
     */
    void addTask(Object key, T task);
    
    /**
     * 移除并返回指定 key 的任务（延迟引擎支持，即时引擎可能抛异常）。
     *
     * @param key 任务标识
     * @return 被移除的任务，或 null
     */
    T removeTask(Object key);
    
    /**
     * 获取当前所有任务 key（部分实现不支持）。
     *
     * @return 任务 key 集合
     */
    Collection<Object> getAllTaskKeys();
}
