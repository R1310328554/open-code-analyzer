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

package com.alibaba.nacos.naming.healthcheck;

import com.alibaba.nacos.naming.interceptor.Interceptable;

/**
 * Nacos 健康检查任务接口。
 *
 * <p>继承 {@link Runnable} 与 {@link Interceptable}，由健康检查调度器周期性触发 {@link #doHealthCheck()} 执行探测。</p>
 *
 * @author xiweng.yy
 */
public interface NacosHealthCheckTask extends Interceptable, Runnable {
    
    /**
     * 获取任务唯一标识。
     *
     * @return task id.
     */
    String getTaskId();
    
    /**
     * 执行一次健康检查逻辑。
     */
    void doHealthCheck();
}
