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

package com.alibaba.nacos.naming.core.v2.metadata;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * V2 服务实例元数据模型。
 *
 * <p>存储实例权重、启用状态及扩展属性，与注册实例通过 metadataId 关联。</p>
 *
 * @author xiweng.yy
 */
public class InstanceMetadata implements Serializable {
    
    private static final long serialVersionUID = -8477858617353459226L;
    
    /** 实例负载权重，默认 1.0。 */
    private double weight = 1.0D;
    
    /** 实例是否启用，禁用后不再接收流量。 */
    private boolean enabled = true;
    
    /** 实例扩展键值属性。 */
    private Map<String, Object> extendData = new ConcurrentHashMap<>(1);
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, Object> getExtendData() {
        return extendData;
    }
    
    public void setExtendData(Map<String, Object> extendData) {
        this.extendData = extendData;
    }
}
