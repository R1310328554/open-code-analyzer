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

/**
 * CMDB 实体变更事件。
 *
 * <p>描述某实体在指定时间点发生的增删改通知，
 * 供 {@link com.alibaba.nacos.api.cmdb.spi.CmdbService#getEntityEvents(long)} 增量拉取。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public class EntityEvent {
    
    /** 事件类型（新增/更新或删除）。 */
    private EntityEventType type;
    
    /** 受影响实体名称。 */
    private String entityName;
    
    /** 受影响实体类型。 */
    private String entityType;
    
    public EntityEventType getType() {
        return type;
    }
    
    public void setType(EntityEventType type) {
        this.type = type;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
