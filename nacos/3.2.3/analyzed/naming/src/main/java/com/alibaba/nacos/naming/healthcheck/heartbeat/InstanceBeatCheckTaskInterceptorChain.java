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

import com.alibaba.nacos.naming.interceptor.AbstractNamingInterceptorChain;

/**
 * 实例心跳检查拦截器链（单例）。
 *
 * <p>自动收集 {@link AbstractBeatCheckInterceptor} 子类，按 order 对 {@link InstanceBeatCheckTask} 执行前置过滤。</p>
 *
 * @author xiweng.yy
 */
public class InstanceBeatCheckTaskInterceptorChain
    extends AbstractNamingInterceptorChain<InstanceBeatCheckTask> {
    
    /** 全局单例。 */
    private static final InstanceBeatCheckTaskInterceptorChain INSTANCE =
        new InstanceBeatCheckTaskInterceptorChain();
    
    /** 私有构造，扫描 AbstractBeatCheckInterceptor 实现类。 */
    private InstanceBeatCheckTaskInterceptorChain() {
        super(AbstractBeatCheckInterceptor.class);
    }
    
    /** 获取拦截链单例。 */
    public static InstanceBeatCheckTaskInterceptorChain getInstance() {
        return INSTANCE;
    }
}
