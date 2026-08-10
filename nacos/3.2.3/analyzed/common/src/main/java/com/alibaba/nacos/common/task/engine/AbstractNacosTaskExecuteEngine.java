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

import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos 任务执行引擎抽象基类：维护 key → {@link com.alibaba.nacos.common.task.NacosTaskProcessor}
 * 的并发映射及默认处理器，子类 {@link NacosDelayTaskExecuteEngine} 与
 * {@link NacosExecuteTaskExecuteEngine} 分别实现延迟队列与即时 worker 池调度。
 * Abstract nacos task execute engine.
 *
 * @author xiweng.yy
 */
public abstract class AbstractNacosTaskExecuteEngine<T extends NacosTask>
    implements NacosTaskExecuteEngine<T> {
    
    /** 引擎日志记录器 */
    private final Logger log;
    
    /** 任务 key 到处理器的并发注册表 */
    private final ConcurrentHashMap<Object, NacosTaskProcessor> taskProcessors =
        new ConcurrentHashMap<>();
    
    /** 未匹配 key 时使用的默认处理器 */
    private NacosTaskProcessor defaultTaskProcessor;
    
    /**
     * 构造引擎并绑定日志；logger 为 null 时使用类名默认 Logger。
     *
     * @param logger SLF4J 日志实例，可为 null
     */
        this.log = null != logger ? logger
            : LoggerFactory.getLogger(AbstractNacosTaskExecuteEngine.class.getName());
    }
    
    /** 注册处理器；key 已存在时不覆盖（putIfAbsent） */
    @Override
    public void addProcessor(Object key, NacosTaskProcessor taskProcessor) {
        taskProcessors.putIfAbsent(key, taskProcessor);
    }
    
    /** 移除指定 key 的处理器 */
    @Override
    public void removeProcessor(Object key) {
        taskProcessors.remove(key);
    }
    
    /** 获取 key 对应处理器，不存在则返回默认处理器 */
    @Override
    public NacosTaskProcessor getProcessor(Object key) {
        return taskProcessors.containsKey(key) ? taskProcessors.get(key) : defaultTaskProcessor;
    }
    
    /** 返回所有已注册处理器的 key 集合 */
    @Override
    public Collection<Object> getAllProcessorKey() {
        return taskProcessors.keySet();
    }
    
    /** 设置默认任务处理器 */
    @Override
    public void setDefaultTaskProcessor(NacosTaskProcessor defaultTaskProcessor) {
        this.defaultTaskProcessor = defaultTaskProcessor;
    }
    
    /** 供子类访问引擎日志 */
    protected Logger getEngineLog() {
        return log;
    }
}
