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

package com.alibaba.nacos.api.ability.initializer;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityMode;

import java.util.Map;

/**
 * Nacos 能力后处理器 SPI，在能力表加载前介入修改。
 *
 * <p>通过 Java SPI 发现，由 Ability Controller 在加载静态能力表前调用。</p>
 *
 * @author Daydreamer-ia
 */
public interface AbilityPostProcessor {
    
    /**
     * 在 Ability Controller 加载能力表之前执行后处理逻辑。
     *
     * @param mode      mode: sdk client, server or cluster client
     * @param abilities abilities
     */
    void process(AbilityMode mode, Map<AbilityKey, Boolean> abilities);
    
}
