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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.naming.interceptor.NacosNamingInterceptor;

/**
 * 实例心跳检查拦截器抽象基类。
 *
 * <p>限定拦截目标为 {@link InstanceBeatCheckTask} 及其子类，供责任链在检查前过滤非本节点负责的实例。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractBeatCheckInterceptor
    implements NacosNamingInterceptor<InstanceBeatCheckTask> {
    
    /** 仅拦截 {@link InstanceBeatCheckTask} 类型任务。 */
    @Override
    public boolean isInterceptType(Class<?> type) {
        return InstanceBeatCheckTask.class.isAssignableFrom(type);
    }
}
