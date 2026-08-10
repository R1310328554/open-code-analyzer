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

package com.alibaba.nacos.core.ability.control;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityMode;
import com.alibaba.nacos.api.ability.register.impl.ClusterClientAbilities;
import com.alibaba.nacos.api.ability.register.impl.SdkClientAbilities;
import com.alibaba.nacos.api.ability.register.impl.ServerAbilities;
import com.alibaba.nacos.common.ability.AbstractAbilityControlManager;
import com.alibaba.nacos.core.ability.config.AbilityConfigs;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Nacos 服务端能力控制管理器：初始化当前节点对集群客户端、SDK 客户端与服务端的能力开关表。
 * {@link AbstractAbilityControlManager} for nacos-server.
 *
 * @author Daydreamer
 * @description {@link AbstractAbilityControlManager} for nacos-server.
 * @date 2022/7/13 21:14
 */
public class ServerAbilityControlManager extends AbstractAbilityControlManager {
    
    public ServerAbilityControlManager() {
    }
    
    @Override
    protected Map<AbilityMode, Map<AbilityKey, Boolean>> initCurrentNodeAbilities() {
        // 初始化客户端侧能力表
        Map<AbilityMode, Map<AbilityKey, Boolean>> res = new HashMap<>(2);
        res.put(AbilityMode.CLUSTER_CLIENT, initClusterClientAbilities());
        res.put(AbilityMode.SDK_CLIENT, initSdkClientAbilities());
        
        // 初始化服务端能力：先加载静态能力定义
        Map<AbilityKey, Boolean> staticAbilities = ServerAbilities.getStaticAbilities();
        // 服务端可声明的全部能力键
        Set<AbilityKey> abilityKeys = staticAbilities.keySet();
        Map<AbilityKey, Boolean> abilityTable = new HashMap<>(abilityKeys.size());
        // 配置未显式定义时，回退到 ServerAbilities 静态默认值
        Set<AbilityKey> unIncludedInConfig = new HashSet<>();
        abilityKeys.forEach(abilityKey -> {
            String key = AbilityConfigs.PREFIX + abilityKey.getName();
            try {
                Boolean property = EnvUtil.getProperty(key, Boolean.class);
                // 环境配置存在则优先采用
                if (property != null) {
                    abilityTable.put(abilityKey, property);
                } else {
                    unIncludedInConfig.add(abilityKey);
                }
            } catch (Exception e) {
                // 读取失败时回退静态能力表
                unIncludedInConfig.add(abilityKey);
            }
        });
        // 将未在配置中出现的能力键写入结果表
        unIncludedInConfig
            .forEach(abilityKey -> abilityTable.put(abilityKey, staticAbilities.get(abilityKey)));
        
        res.put(AbilityMode.SERVER, abilityTable);
        return res;
    }
    
    /**
     * 初始化集群客户端（CLUSTER_CLIENT）静态能力映射。
     */
    private Map<AbilityKey, Boolean> initClusterClientAbilities() {
        // 直接返回 ClusterClientAbilities 预定义能力
        return ClusterClientAbilities.getStaticAbilities();
    }
    
    /**
     * 初始化 SDK 客户端（SDK_CLIENT）静态能力映射。
     */
    private Map<AbilityKey, Boolean> initSdkClientAbilities() {
        // static abilities
        return SdkClientAbilities.getStaticAbilities();
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
    
}
