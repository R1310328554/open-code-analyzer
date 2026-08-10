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
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

/**
 * 全局健康检查开关拦截器。
 *
 * <p>当 {@link SwitchDomain#isHealthCheckEnabled()} 为 false 时拦截任务；获取 Bean 异常时保守拦截（返回 true）。</p>
 *
 * @author xiweng.yy
 */
public class HealthCheckEnableInterceptor extends AbstractHealthCheckInterceptor {
    
    /** 全局健康检查关闭时拦截，阻止后续探测执行。 */
    @Override
    public boolean intercept(NacosHealthCheckTask object) {
        try {
            return !ApplicationUtils.getBean(SwitchDomain.class).isHealthCheckEnabled();
        } catch (Exception e) {
            return true;
        }
    }
    
    /** 健康检查拦截链最高优先级。 */
    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
}
