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
import com.alibaba.nacos.naming.interceptor.AbstractNamingInterceptorChain;

/**
 * 健康检查拦截器链单例，自动收集所有 {@link AbstractHealthCheckInterceptor} 实现。
 *
 * <p>按 order 排序后依次执行，任一拦截器返回 true 则跳过本次健康检查。</p>
 *
 * @author xiweng.yy
 */
public class HealthCheckInterceptorChain
    extends AbstractNamingInterceptorChain<NacosHealthCheckTask> {
    
    /** 全局唯一拦截链实例。 */
    private static final HealthCheckInterceptorChain INSTANCE = new HealthCheckInterceptorChain();
    
    /** 私有构造，注册 AbstractHealthCheckInterceptor 子类为链成员。 */
    private HealthCheckInterceptorChain() {
        super(AbstractHealthCheckInterceptor.class);
    }
    
    /** 获取健康检查拦截链单例。 */
    public static HealthCheckInterceptorChain getInstance() {
        return INSTANCE;
    }
}
