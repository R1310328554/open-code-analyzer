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

package com.alibaba.nacos.naming.constants;

/**
 * 命名模块全局常量定义。
 *
 * <p>命名模块错误码以 20001 起；本类集中存放元数据键、清理策略、实例权重与发布相关配置常量。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class Constants {
    
    /** 服务级元数据存储键。 */
    public static final String SERVICE_METADATA = "naming_service_metadata";
    
    /** 实例级元数据存储键。 */
    public static final String INSTANCE_METADATA = "naming_instance_metadata";
    
    /** 持久化服务 Raft 分组名（v1）。 */
    public static final String NAMING_PERSISTENT_SERVICE_GROUP = "naming_persistent_service";
    
    /** 持久化服务 Raft 分组名（v2）。 */
    public static final String NAMING_PERSISTENT_SERVICE_GROUP_V2 = "naming_persistent_service_v2";
    
    /** 是否优先使用新版 Raft 实现的配置键。 */
    public static final String NACOS_NAMING_USE_NEW_RAFT_FIRST = "nacos.naming.use-new-raft.first";
    
    /** 清理空服务的扫描间隔配置键，单位毫秒，默认 60000。 */
    public static final String EMPTY_SERVICE_CLEAN_INTERVAL =
        "nacos.naming.clean.empty-service.interval";
    
    /** 空服务过期判定时间配置键，单位毫秒，默认 60000。 */
    public static final String EMPTY_SERVICE_EXPIRED_TIME =
        "nacos.naming.clean.empty-service.expired-time";
    
    /** 清理过期元数据的扫描间隔配置键，单位毫秒，默认 5000。 */
    public static final String EXPIRED_METADATA_CLEAN_INTERVAL =
        "nacos.naming.clean.expired-metadata.interval";
    
    /** 元数据过期判定时间配置键，单位毫秒，默认 60000。 */
    public static final String EXPIRED_METADATA_EXPIRED_TIME =
        "nacos.naming.clean.expired-metadata.expired-time";
    
    /** 是否启用数据预热，默认 false。 */
    public static final String DATA_WARMUP = "nacos.naming.data.warmup";
    
    /** 是否启用实例过期清理，默认 true。 */
    public static final String EXPIRE_INSTANCE = "nacos.naming.expireInstance";
    
    /** 自定义实例 ID 的元数据键。 */
    public static final String CUSTOM_INSTANCE_ID = "customInstanceId";
    
    /** 实例自发布时携带的权重元数据键。 */
    public static final String PUBLISH_INSTANCE_WEIGHT = "publishInstanceWeight";
    
    /** 默认实例权重值。 */
    public static final double DEFAULT_INSTANCE_WEIGHT = 1.0D;
    
    /** 实例自发布时携带的启用状态元数据键。 */
    public static final String PUBLISH_INSTANCE_ENABLE = "publishInstanceEnable";
    
    /** 实例权重上限。 */
    public static final double MAX_WEIGHT_VALUE = 10000.0D;
    
    /** 实例权重最小正值。 */
    public static final double MIN_POSITIVE_WEIGHT_VALUE = 0.01D;
    
    /** 实例权重最小值（含零）。 */
    public static final double MIN_WEIGHT_VALUE = 0.00D;
    
}
