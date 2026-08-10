/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.constants;

/**
 * Nacos 持久化表名常量。
 *
 * <p>Mapper 实现通过 {@link com.alibaba.nacos.plugin.datasource.mapper.Mapper#getTableName()} 引用，保证 SQL 表名一致。</p>
 *
 * @author hyx
 **/

public class TableConstant {
    
    /** 核心配置表。 */
    public static final String CONFIG_INFO = "config_info";
    
    /** Beta 发布配置表。 */
    public static final String CONFIG_INFO_BETA = "config_info_beta";
    
    /** 配置标签表。 */
    public static final String CONFIG_INFO_TAG = "config_info_tag";
    
    /** 灰度配置表。 */
    public static final String CONFIG_INFO_GRAY = "config_info_gray";
    
    /** 配置与标签关联表。 */
    public static final String CONFIG_TAGS_RELATION = "config_tags_relation";
    
    /** Group 容量配额表。 */
    public static final String GROUP_CAPACITY = "group_capacity";
    
    /** 配置变更历史表。 */
    public static final String HIS_CONFIG_INFO = "his_config_info";
    
    /** 租户容量配额表。 */
    public static final String TENANT_CAPACITY = "tenant_capacity";
    
    /** 租户信息表。 */
    public static final String TENANT_INFO = "tenant_info";
    
    /** 配置命名空间迁移任务表。 */
    public static final String MIGRATE_CONFIG = "migrate_config";
    
    /** AI 资源元数据表。 */
    public static final String AI_RESOURCE = "ai_resource";
    
    /** AI 资源版本表。 */
    public static final String AI_RESOURCE_VERSION = "ai_resource_version";
}
