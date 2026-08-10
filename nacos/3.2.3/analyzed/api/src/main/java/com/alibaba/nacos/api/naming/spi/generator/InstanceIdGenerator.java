/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.spi.generator;

import com.alibaba.nacos.api.naming.pojo.Instance;

/**
 * 实例 ID 生成器 SPI。
 *
 * <p>允许插件自定义 {@link Instance} 唯一标识的生成策略；通过 {@link #type()} 区分不同实现。</p>
 *
 * @author xiweng.yy
 */
public interface InstanceIdGenerator {
    
    /**
     * 根据实例信息生成唯一 ID。
     *
     * @param instance 实例对象
     * @return 实例 ID
     */
    String generateInstanceId(Instance instance);
    
    /**
     * 返回生成器类型标识。
     *
     * @return 类型名称
     */
    String type();
}
