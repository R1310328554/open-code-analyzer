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

package com.alibaba.nacos.sys.module;

import com.alibaba.nacos.sys.env.DeploymentType;

/**
 * 模块状态构建器 SPI 接口。
 *
 * <p>各子模块通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 注册实现， 由 {@link ModuleStateHolder} 在启动时聚合构建 {@link ModuleState}。</p>
 *
 * @author xiweng.yy
 */
public interface ModuleStateBuilder {
    
    /**
     * 构建当前模块的状态快照。
     *
     * @return 填充完毕的 {@link ModuleState}
     */
    ModuleState build();
    
    /**
     * 是否跳过该构建器（默认 false，即参与构建）。
     *
     * @return true 表示忽略
     */
    default boolean isIgnore() {
        return false;
    }
    
    /**
     * 构建结果是否可缓存（默认 true，仅启动时构建一次）。
     *
     * @return true 表示缓存
     */
    default boolean isCacheable() {
        return true;
    }
    
    /**
     * 当前部署类型是否适用该模块（默认 true，全部部署均构建）。
     *
     * @param type 部署类型枚举
     * @return true 表示匹配
     */
    default boolean isMatchDeployment(DeploymentType type) {
        return true;
    }
}
