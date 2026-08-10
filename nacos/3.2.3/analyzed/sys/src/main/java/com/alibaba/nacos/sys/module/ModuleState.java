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

package com.alibaba.nacos.sys.module;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个 Nacos 模块的运行时状态快照。
 *
 * <p>以模块名为键、状态名-值对为内容，供运维接口与内部组件查询模块能力开关、 版本信息等元数据。</p>
 *
 * @author xiweng.yy
 */
public class ModuleState {
    
    /** 模块标识名（如 naming、config）。 */
    private final String moduleName;
    
    /** 模块内各状态项键值表。 */
    private final Map<String, Object> states;
    
    /** 创建指定模块名的空状态容器。 */
    public ModuleState(String moduleName) {
        this.moduleName = moduleName;
        this.states = new HashMap<>();
    }
    
    /** 返回模块名。 */
    public String getModuleName() {
        return moduleName;
    }
    
    /** 链式写入一项状态并返回自身。 */
    public ModuleState newState(String stateName, Object stateValue) {
        this.states.put(stateName, stateValue);
        return this;
    }
    
    /** 返回全部状态项只读视图。 */
    public Map<String, Object> getStates() {
        return states;
    }
    
    @SuppressWarnings("all")
    /** 按状态名取值，缺失时返回默认值。 */
    public <T> T getState(String stateName, T defaultValue) {
        return (T) states.getOrDefault(stateName, defaultValue);
    }
}
