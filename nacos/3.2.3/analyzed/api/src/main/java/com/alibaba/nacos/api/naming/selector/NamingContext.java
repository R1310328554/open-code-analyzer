/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.selector;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 命名服务选择器上下文。
 *
 * <p>为 {@link NamingSelector} 提供当前服务标识、集群范围及候选 {@link Instance} 列表，供自定义路由/负载策略使用。</p>
 *
 * @author lideyou
 */
public interface NamingContext {
    
    /**
     * 获取服务名。
     *
     * @return 服务名
     */
    String getServiceName();
    
    /**
     * 获取分组名。
     *
     * @return 分组名
     */
    String getGroupName();
    
    /**
     * 获取集群列表（逗号分隔）。
     *
     * @return 集群标识
     */
    String getClusters();
    
    /**
     * 获取当前候选实例列表。
     *
     * @return 实例列表
     */
    List<Instance> getInstances();
}
