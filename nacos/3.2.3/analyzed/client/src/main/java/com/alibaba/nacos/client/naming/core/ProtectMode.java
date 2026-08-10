/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.naming.core;

/**
 * 服务保护模式配置。
 *
 * <p>表示当健康实例比例低于保护阈值时，Naming 服务端是否触发保护逻辑（返回全部实例含不健康）。</p>
 *
 * @author nkorange
 */
public class ProtectMode {
    
    /** 触发保护的健康实例比例阈值（默认 0.8）。 */
    private float protectThreshold;
    
    /** 默认构造，保护阈值设为 0.8。 */
    public ProtectMode() {
        this.protectThreshold = 0.8F;
    }
    
    /** 获取保护阈值。 */
    public float getProtectThreshold() {
        return protectThreshold;
    }
    
    /** 设置保护阈值。 */
    public void setProtectThreshold(float protectThreshold) {
        this.protectThreshold = protectThreshold;
    }
}
