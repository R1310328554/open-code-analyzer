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

package com.alibaba.nacos.api.cmdb.spi;

import com.alibaba.nacos.api.cmdb.pojo.Entity;
import com.alibaba.nacos.api.cmdb.pojo.EntityEvent;
import com.alibaba.nacos.api.cmdb.pojo.Label;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 访问 CMDB 存储的 SPI 服务接口。
 *
 * <p>由 CMDB 插件实现，供 Nacos 命名模块查询标签、实体及变更事件。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
public interface CmdbService {
    
    /**
     * 获取 CMDB 中全部标签名称。
     *
     * @return 标签名集合
     */
    Set<String> getLabelNames();
    
    /**
     * 获取 CMDB 支持的全部实体类型。
     *
     * @return 实体类型集合
     */
    Set<String> getEntityTypes();
    
    /**
     * 按名称查询标签元数据。
     *
     * @param labelName 标签名
     * @return 标签定义
     */
    Label getLabel(String labelName);
    
    /**
     * 获取指定实体上某标签的取值。
     *
     * @param entityName 实体名称
     * @param entityType 实体类型
     * @param labelName  目标标签名
     * @return 标签值
     */
    String getLabelValue(String entityName, String entityType, String labelName);
    
    /**
     * 获取指定实体的全部标签键值对。
     *
     * @param entityName 实体名称
     * @param entityType 实体类型
     * @return 标签名到取值的映射
     */
    Map<String, String> getLabelValues(String entityName, String entityType);
    
    /**
     * 导出 CMDB 中全部实体。
     *
     * @return 外层键为实体类型、内层为实体映射
     */
    Map<String, Map<String, Entity>> getAllEntities();
    
    /**
     * 获取自指定时间戳以来的实体变更事件。
     *
     * @param timestamp 起始时间戳（毫秒）
     * @return 实体事件列表
     */
    List<EntityEvent> getEntityEvents(long timestamp);
    
    /**
     * 按名称与类型查询单个实体。
     *
     * @param entityName 实体名称
     * @param entityType 实体类型
     * @return 实体对象
     */
    Entity getEntity(String entityName, String entityType);
}
