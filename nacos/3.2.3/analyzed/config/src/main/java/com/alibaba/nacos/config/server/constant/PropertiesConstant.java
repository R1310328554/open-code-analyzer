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

package com.alibaba.nacos.config.server.constant;

/**
 * Config 模块 {@link ModuleState} 与运行时属性键名常量：通知超时、健康检查、
 * 容量配额、搜索线程池、Dump 变更及命名空间兼容等配置的 state 字段标识。
 * PropertiesConstant.
 *
 * @author lixiaoshuang
 */
public class PropertiesConstant {
    
    /** 配置变更通知连接超时（毫秒）状态键 */
    public static final String NOTIFY_CONNECT_TIMEOUT = "notifyConnectTimeout";
    
    public static final String NOTIFY_SOCKET_TIMEOUT = "notifySocketTimeout";
    
    public static final String IS_HEALTH_CHECK = "isHealthCheck";
    
    public static final String MAX_HEALTH_CHECK_FAIL_COUNT = "maxHealthCheckFailCount";
    
    public static final String MAX_CONTENT = "maxContent";
    
    /** 是否启用容量管理功能的状态键 */
    public static final String IS_MANAGE_CAPACITY = "isManageCapacity";
    
    public static final String IS_CAPACITY_LIMIT_CHECK = "isCapacityLimitCheck";
    
    public static final String DEFAULT_CLUSTER_QUOTA = "defaultClusterQuota";
    
    public static final String DEFAULT_GROUP_QUOTA = "defaultGroupQuota";
    
    public static final String DEFAULT_TENANT_QUOTA = "defaultTenantQuota";
    
    /** 单条配置默认最大字节数配额状态键 */
    public static final String DEFAULT_MAX_SIZE = "defaultMaxSize";
    
    public static final String DEFAULT_MAX_AGGR_COUNT = "defaultMaxAggrCount";
    
    public static final String DEFAULT_MAX_AGGR_SIZE = "defaultMaxAggrSize";
    
    public static final String CORRECT_USAGE_DELAY = "correctUsageDelay";
    
    public static final String INITIAL_EXPANSION_PERCENT = "initialExpansionPercent";
    
    public static final String SEARCH_MAX_CAPACITY = "nacos.config.search.max_capacity";
    
    public static final String SEARCH_MAX_THREAD = "nacos.config.search.max_thread";
    
    public static final String SEARCH_WAIT_TIMEOUT = "nacos.config.search.wait_timeout";
    
    public static final String DUMP_CHANGE_ON = "dumpChangeOn";
    
    public static final String DUMP_CHANGE_WORKER_INTERVAL = "dumpChangeWorkerInterval";
    
    /** 配置历史保留天数环境属性键 */
    public static final String CONFIG_RENTENTION_DAYS = "nacos.config.retention.days";
    
    public static final String GRAY_CAPATIBEL_MODEL = "nacos.config.gray.compatible.model";
    
    /** 命名空间 ID 兼容模式环境属性键 */
    public static final String NAMESPACE_COMPATIBLE_MODE = "nacos.config.namespace.compatible.mode";
    
}
