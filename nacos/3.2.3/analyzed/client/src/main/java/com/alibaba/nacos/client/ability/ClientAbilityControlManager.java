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

package com.alibaba.nacos.client.ability;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityMode;
import com.alibaba.nacos.api.ability.register.impl.SdkClientAbilities;
import com.alibaba.nacos.common.ability.AbstractAbilityControlManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Nacos 客户端能力控制管理器。
 *
 * <p>继承 {@link AbstractAbilityControlManager}，注册 SDK 客户端静态能力集，
 * 供客户端与服务端协商功能开关时使用。</p>
 *
 * @author Daydreamer
 * @description {@link AbstractAbilityControlManager} for nacos-client.
 * @date 2022/7/13 13:38
 **/
public class ClientAbilityControlManager extends AbstractAbilityControlManager {
    
    /** 默认构造，由客户端初始化流程实例化。 */
    public ClientAbilityControlManager() {
    }
    
    @Override
    /** 初始化当前 SDK 客户端节点支持的能力映射表。 */
    protected Map<AbilityMode, Map<AbilityKey, Boolean>> initCurrentNodeAbilities() {
        Map<AbilityMode, Map<AbilityKey, Boolean>> abilities = new HashMap<>(1);
        abilities.put(AbilityMode.SDK_CLIENT, SdkClientAbilities.getStaticAbilities());
        return abilities;
    }
    
    @Override
    /** 返回优先级；客户端管理器优先级为 0，低于服务端。 */
    public int getPriority() {
        // 若存在服务端能力管理器，应优先采用服务端能力配置
        return 0;
    }
    
}
