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

package com.alibaba.nacos.cmdb.service;

import com.alibaba.nacos.api.cmdb.pojo.Entity;

import java.util.List;

/**
 * CMDB Reader.
 * <p>CMDB 只读查询接口：按实体或标签维度读取内存中的 CMDB 数据。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public interface CmdbReader {
    
    /**
     * Get entity.
     * <p>按名称与类型查询单个 {@link Entity}。</p>
     *
     * @param entityName name of entity
     * @param entityType type of entity
     * @return entity
     */
    Entity queryEntity(String entityName, String entityType);
    
    /**
     * Get label of entity.
     * <p>读取指定实体上某一标签的值。</p>
     *
     * @param entityName name of entity
     * @param entityType type of entity
     * @param labelName  label name
     * @return label value
     */
    String queryLabel(String entityName, String entityType, String labelName);
    
    /**
     * Get entities of selected label.
     * <p>按标签名与标签值反查匹配的实体列表。</p>
     *
     * @param labelName  name of label
     * @param labelValue value of label
     * @return list of entity
     */
    List<Entity> queryEntitiesByLabel(String labelName, String labelValue);
}
