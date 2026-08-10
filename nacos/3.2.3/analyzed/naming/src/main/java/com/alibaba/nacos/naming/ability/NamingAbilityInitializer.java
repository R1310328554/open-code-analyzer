/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.ability;

import com.alibaba.nacos.api.ability.ServerAbilities;
import com.alibaba.nacos.core.ability.ServerAbilityInitializer;

/**
 * 命名模块服务端能力初始化器：向集群宣告命名相关特性。
 *
 * <p>实现 {@link ServerAbilityInitializer}，在启动时将 JRaft 支持标记为开启。</p>
 *
 * @author xiweng.yy
 */
public class NamingAbilityInitializer implements ServerAbilityInitializer {
    
    /** 设置命名模块支持 JRaft 一致性存储。 */
    @Override
    public void initialize(ServerAbilities abilities) {
        abilities.getNamingAbility().setSupportJraft(true);
    }
}
