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

package com.alibaba.nacos.api.ability.register.impl;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.register.AbstractAbilityRegistry;

import java.util.Map;

/**
 * 集群客户端静态能力注册表，声明集群内节点支持的功能集合。
 *
 * <p>单例模式，通过 {@link #getStaticAbilities()} 获取不可变能力映射。</p>
 *
 * @author Daydreamer
 **/
public class ClusterClientAbilities extends AbstractAbilityRegistry {
    
    private static final ClusterClientAbilities INSTANCE = new ClusterClientAbilities();
    
    {
        /*
         * 示例：新增能力 "compression"
         *   1. 在 AbilityKey 中增加枚举常量，如 DATA_COMPRESSION("compression", "描述")
         *   2. 在本实例块中声明：supportedAbilities.put(AbilityKey.DATA_COMPRESSION, true)
         *   键来自 AbilityKey，值为是否开启该能力。
         */
        // 在此注册当前端点支持的能力项
    }
    
    /**
     * 获取集群客户端当前静态支持的能力映射。
     *
     * @return static ability
     */
    public static Map<AbilityKey, Boolean> getStaticAbilities() {
        return INSTANCE.getSupportedAbilities();
    }
}
