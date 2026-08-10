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

package com.alibaba.nacos.sys.env;

import com.alibaba.nacos.common.utils.VersionUtils;
import com.alibaba.nacos.sys.module.AbstractServerModuleStateBuilder;
import com.alibaba.nacos.sys.module.ModuleState;

/**
 * sys 环境模块状态构建器。
 *
 * <p>汇总启动模式、功能模式、版本号与服务端口等运行时信息，写入 {@link ModuleState} 供运维接口与模块发现使用。</p>
 *
 * @author xiweng.yy
 */
public class EnvModuleStateBuilder extends AbstractServerModuleStateBuilder {
    
    /** 构建 env 模块的 {@link ModuleState} 快照。 */
    @Override
    public ModuleState build() {
        ModuleState state = new ModuleState(Constants.SYS_MODULE);
        state.newState(Constants.STARTUP_MODE_STATE,
            EnvUtil.getStandaloneMode() ? EnvUtil.STANDALONE_MODE_ALONE
                : EnvUtil.STANDALONE_MODE_CLUSTER);
        state.newState(Constants.FUNCTION_MODE_STATE, EnvUtil.getFunctionMode());
        state.newState(Constants.NACOS_VERSION, VersionUtils.version);
        
        state.newState(Constants.SERVER_PORT_STATE, EnvUtil.getPort());
        return state;
    }
}
