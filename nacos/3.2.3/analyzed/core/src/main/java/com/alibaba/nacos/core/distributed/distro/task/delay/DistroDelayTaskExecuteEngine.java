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

package com.alibaba.nacos.core.distributed.distro.task.delay;

import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.common.task.engine.NacosDelayTaskExecuteEngine;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.utils.Loggers;

/**
 * Distro 延迟任务执行引擎：继承 {@link com.alibaba.nacos.common.task.engine.NacosDelayTaskExecuteEngine}，按资源类型归一化处理器键。
 * Distro delay task execute engine.
 *
 * @author xiweng.yy
 */
public class DistroDelayTaskExecuteEngine extends NacosDelayTaskExecuteEngine {
    
    /** 使用 Distro 日志记录器初始化延迟引擎。 */
    public DistroDelayTaskExecuteEngine() {
        super(DistroDelayTaskExecuteEngine.class.getName(), Loggers.DISTRO);
    }
    
    /**
     * 注册任务处理器，{@link com.alibaba.nacos.core.distributed.distro.entity.DistroKey} 键会被映射为其 resourceType。
     *
     * @param key 处理器键
     * @param taskProcessor 处理器实例
     */
    @Override
    public void addProcessor(Object key, NacosTaskProcessor taskProcessor) {
        Object actualKey = getActualKey(key);
        super.addProcessor(actualKey, taskProcessor);
    }
    
    /**
     * 按归一化后的键获取任务处理器。
     *
     * @param key 处理器键
     * @return 匹配的处理器，未注册时返回 null
     */
    @Override
    public NacosTaskProcessor getProcessor(Object key) {
        Object actualKey = getActualKey(key);
        return super.getProcessor(actualKey);
    }
    
    /** 将 {@link com.alibaba.nacos.core.distributed.distro.entity.DistroKey} 转为 resourceType，否则原样返回。 */
    private Object getActualKey(Object key) {
        return key instanceof DistroKey ? ((DistroKey) key).getResourceType() : key;
    }
}
