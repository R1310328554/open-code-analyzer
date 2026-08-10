/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ability.register.impl;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.register.AbstractAbilityRegistry;

import java.util.Map;

/**
 * Nacos 服务端静态能力注册表，声明服务端支持的功能集合。
 *
 * <p>包含 gRPC 持久实例、模糊 Watch、分布式锁及 AI 模块 MCP/Agent 等能力。</p>
 *
 * @author Daydreamer
 * @date 2022/8/31 12:32
 **/
public class ServerAbilities extends AbstractAbilityRegistry {
    
    private static final ServerAbilities INSTANCE = new ServerAbilities();
    
    {
        /*
         * 示例：新增能力 "compression"
         *   1. 在 AbilityKey 中增加枚举常量，如 DATA_COMPRESSION("compression", "描述")
         *   2. 在本实例块中声明：supportedAbilities.put(AbilityKey.DATA_COMPRESSION, true)
         *   键来自 AbilityKey，值为是否开启该能力。
         */
        // 在此注册当前服务端支持的能力项
        supportedAbilities.put(AbilityKey.SERVER_PERSISTENT_INSTANCE_BY_GRPC, true);
        supportedAbilities.put(AbilityKey.SERVER_FUZZY_WATCH, true);
        supportedAbilities.put(AbilityKey.SERVER_DISTRIBUTED_LOCK, true);
        supportedAbilities.put(AbilityKey.SERVER_MCP_REGISTRY, true);
        supportedAbilities.put(AbilityKey.SERVER_AGENT_REGISTRY, true);
        supportedAbilities.put(AbilityKey.SERVER_AGENT_CARD_V1, true);
    }
    
    /**.
     * 获取服务端当前静态支持的能力映射。
     *
     * @return static ability
     */
    public static Map<AbilityKey, Boolean> getStaticAbilities() {
        return INSTANCE.getSupportedAbilities();
    }
    
}
