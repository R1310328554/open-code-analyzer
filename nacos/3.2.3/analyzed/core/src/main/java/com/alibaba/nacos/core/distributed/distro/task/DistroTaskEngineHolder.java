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

package com.alibaba.nacos.core.distributed.distro.task;

import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.task.delay.DistroDelayTaskExecuteEngine;
import com.alibaba.nacos.core.distributed.distro.task.delay.DistroDelayTaskProcessor;
import com.alibaba.nacos.core.distributed.distro.task.execute.DistroExecuteTaskExecuteEngine;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Distro 任务引擎持有者：统一管理延迟任务引擎与同步执行引擎，并在 Spring 容器销毁时关闭两者。
 * Distro task engine holder.
 *
 * @author xiweng.yy
 */
@Component
public class DistroTaskEngineHolder implements DisposableBean {
    
    /** 延迟任务执行引擎，负责合并与调度 {@link com.alibaba.nacos.core.distributed.distro.task.delay.DistroDelayTask}。 */
    private final DistroDelayTaskExecuteEngine delayTaskExecuteEngine =
        new DistroDelayTaskExecuteEngine();
    
    /** 同步执行任务引擎，承载变更/删除等即时 Distro 同步任务。 */
    private final DistroExecuteTaskExecuteEngine executeWorkersManager =
        new DistroExecuteTaskExecuteEngine();
    
    /**
     * 构造时注册默认延迟任务处理器，将延迟任务路由到同步执行引擎。
     *
     * @param distroComponentHolder Distro 组件注册表
     */
    public DistroTaskEngineHolder(DistroComponentHolder distroComponentHolder) {
        DistroDelayTaskProcessor defaultDelayTaskProcessor =
            new DistroDelayTaskProcessor(this, distroComponentHolder);
        delayTaskExecuteEngine.setDefaultTaskProcessor(defaultDelayTaskProcessor);
    }
    
    /** 返回延迟任务执行引擎。 */
    public DistroDelayTaskExecuteEngine getDelayTaskExecuteEngine() {
        return delayTaskExecuteEngine;
    }
    
    /** 返回同步执行任务引擎。 */
    public DistroExecuteTaskExecuteEngine getExecuteWorkersManager() {
        return executeWorkersManager;
    }
    
    /**
     * 按资源类型注册自定义延迟任务处理器。
     *
     * @param key 处理器键（通常为 {@link com.alibaba.nacos.core.distributed.distro.entity.DistroKey} 或资源类型）
     * @param nacosTaskProcessor 任务处理器实现
     */
    public void registerNacosTaskProcessor(Object key, NacosTaskProcessor nacosTaskProcessor) {
        this.delayTaskExecuteEngine.addProcessor(key, nacosTaskProcessor);
    }
    
    /** {@inheritDoc} 关闭延迟与同步两套任务引擎。 */
    @Override
    public void destroy() throws Exception {
        this.delayTaskExecuteEngine.shutdown();
        this.executeWorkersManager.shutdown();
    }
}
