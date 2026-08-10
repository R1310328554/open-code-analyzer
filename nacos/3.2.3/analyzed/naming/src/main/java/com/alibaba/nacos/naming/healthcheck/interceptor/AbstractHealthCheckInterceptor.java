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

package com.alibaba.nacos.naming.healthcheck.interceptor;

import com.alibaba.nacos.naming.healthcheck.NacosHealthCheckTask;
import com.alibaba.nacos.naming.interceptor.NacosNamingInterceptor;

/**
 * 健康检查拦截器抽象基类，限定拦截目标为 {@link NacosHealthCheckTask} 及其子类。
 *
 * <p>具体实现负责全局开关、Distro 责任节点判定等前置过滤逻辑。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractHealthCheckInterceptor
    implements NacosNamingInterceptor<NacosHealthCheckTask> {
    
    /** 仅拦截 NacosHealthCheckTask 类型任务。 */
    @Override
    public boolean isInterceptType(Class<?> type) {
        return NacosHealthCheckTask.class.isAssignableFrom(type);
    }
}
