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

import com.alibaba.nacos.naming.core.DistroMapper;
import com.alibaba.nacos.naming.healthcheck.NacosHealthCheckTask;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

/**
 * Distro 责任节点拦截器：非本节点负责的客户端健康检查任务将被跳过。
 *
 * <p>通过 {@link DistroMapper#responsible(String)} 判定 taskId 是否归属当前节点。</p>
 *
 * @author xiweng.yy
 */
public class HealthCheckResponsibleInterceptor extends AbstractHealthCheckInterceptor {
    
    /** 非责任节点时返回 true 以拦截任务。 */
    @Override
    public boolean intercept(NacosHealthCheckTask object) {
        return !ApplicationUtils.getBean(DistroMapper.class).responsible(object.getTaskId());
    }
    
    /** 优先级仅次于全局开关拦截器。 */
    @Override
    public int order() {
        return Integer.MIN_VALUE + 1;
    }
}
