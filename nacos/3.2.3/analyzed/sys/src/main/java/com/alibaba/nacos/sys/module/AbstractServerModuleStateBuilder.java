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
 * 服务端部署类型的模块状态构建器抽象基类。
 *
 * <p>默认仅匹配 {@link com.alibaba.nacos.sys.env.DeploymentType#MERGED} 与 {@link com.alibaba.nacos.sys.env.DeploymentType#SERVER} 两种部署模式， 供各业务模块在 Server 进程中注册 {@link ModuleState}。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractServerModuleStateBuilder implements ModuleStateBuilder {
    
    /** 判断当前部署类型是否为合并或独立 Server 模式。 */
    @Override
    public boolean isMatchDeployment(DeploymentType type) {
        return DeploymentType.MERGED.equals(type) || DeploymentType.SERVER.equals(type);
    }
}
