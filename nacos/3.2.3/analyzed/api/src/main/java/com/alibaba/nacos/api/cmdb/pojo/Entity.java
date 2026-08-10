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

package com.alibaba.nacos.api.cmdb.pojo;

import java.util.Map;

/**
 * CMDB 实体，表示配置管理数据库中的一条记录。
 *
 * <p>由类型 {@link #type}、名称 {@link #name} 及标签映射 {@link #labels} 组成，
 * 用于服务发现与路由策略中的元数据关联。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public class Entity {
    
    /** 实体类型（如 ip、service）。 */
    private String type;
    
    /** 实体名称（唯一标识）。 */
    private String name;
    
    /** 标签键值对集合。 */
    private Map<String, String> labels;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
