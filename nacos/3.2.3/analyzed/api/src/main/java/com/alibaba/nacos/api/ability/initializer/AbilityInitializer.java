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

package com.alibaba.nacos.api.ability.initializer;

/**
 * Nacos 能力初始化器 SPI 接口（已废弃）。
 *
 * <p>在能力对象创建后填充各子模块默认能力位。</p>
 *
 * @author xiweng.yy
 */
@Deprecated
public interface AbilityInitializer<A> {
    
    /**
     * 初始化目标类型能力对象的内容。
     *
     * @param abilities abilities
     */
    void initialize(A abilities);
}
