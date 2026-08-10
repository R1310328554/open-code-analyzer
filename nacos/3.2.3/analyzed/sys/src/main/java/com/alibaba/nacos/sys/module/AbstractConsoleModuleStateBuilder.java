/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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
 * Console 相关模块状态构建器抽象基类。
 *
 * <p>仅在 {@link DeploymentType#MERGED} 或 {@link DeploymentType#CONSOLE} 部署形态下参与模块状态上报，Server 独立部署时跳过 Console 模块。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractConsoleModuleStateBuilder implements ModuleStateBuilder {
    
    /** 合并部署或纯 Console 部署时返回 {@code true}。 */
    @Override
    public boolean isMatchDeployment(DeploymentType type) {
        return DeploymentType.MERGED.equals(type) || DeploymentType.CONSOLE.equals(type);
    }
}
