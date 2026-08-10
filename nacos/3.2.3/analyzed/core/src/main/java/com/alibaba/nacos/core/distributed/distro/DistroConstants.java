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

package com.alibaba.nacos.core.distributed.distro;

/**
 * Distro 协议常量：定义模块标识、配置项键名、状态键及各类默认超时/间隔毫秒值。
 * Distro constants.
 *
 * @author xiweng.yy
 */
public class DistroConstants {
    
    /** Distro 模块标识。 */
    public static final String DISTRO_MODULE = "distro";
    
    /** 数据同步延迟配置键。 */
    public static final String DATA_SYNC_DELAY_MILLISECONDS =
        "nacos.core.protocol.distro.data.sync.delayMs";
    
    /** 同步延迟状态键（动态配置内部使用）。 */
    public static final String DATA_SYNC_DELAY_MILLISECONDS_STATE = "data_sync_delayMs";
    
    /** 默认同步延迟：1000 ms。 */
    public static final long DEFAULT_DATA_SYNC_DELAY_MILLISECONDS = 1000L;
    
    /** 数据同步超时配置键。 */
    public static final String DATA_SYNC_TIMEOUT_MILLISECONDS =
        "nacos.core.protocol.distro.data.sync.timeoutMs";
    
    /** 同步超时状态键。 */
    public static final String DATA_SYNC_TIMEOUT_MILLISECONDS_STATE = "data_sync_timeoutMs";
    
    /** 默认同步超时：3000 ms。 */
    public static final long DEFAULT_DATA_SYNC_TIMEOUT_MILLISECONDS = 3000L;
    
    /** 同步重试间隔配置键。 */
    public static final String DATA_SYNC_RETRY_DELAY_MILLISECONDS =
        "nacos.core.protocol.distro.data.sync.retryDelayMs";
    
    /** 同步重试间隔状态键。 */
    public static final String DATA_SYNC_RETRY_DELAY_MILLISECONDS_STATE = "data_sync_retryDelayMs";
    
    /** 默认同步重试间隔：3000 ms。 */
    public static final long DEFAULT_DATA_SYNC_RETRY_DELAY_MILLISECONDS = 3000L;
    
    /** 数据校验间隔配置键。 */
    public static final String DATA_VERIFY_INTERVAL_MILLISECONDS =
        "nacos.core.protocol.distro.data.verify.intervalMs";
    
    /** 校验间隔状态键。 */
    public static final String DATA_VERIFY_INTERVAL_MILLISECONDS_STATE = "data_verify_intervalMs";
    
    /** 默认校验间隔：5000 ms。 */
    public static final long DEFAULT_DATA_VERIFY_INTERVAL_MILLISECONDS = 5000L;
    
    /** 数据校验超时配置键。 */
    public static final String DATA_VERIFY_TIMEOUT_MILLISECONDS =
        "nacos.core.protocol.distro.data.verify.timeoutMs";
    
    /** 校验超时状态键。 */
    public static final String DATA_VERIFY_TIMEOUT_MILLISECONDS_STATE = "data_verify_timeoutMs";
    
    /** 默认校验超时：3000 ms。 */
    public static final long DEFAULT_DATA_VERIFY_TIMEOUT_MILLISECONDS = 3000L;
    
    /** 全量加载重试间隔配置键。 */
    public static final String DATA_LOAD_RETRY_DELAY_MILLISECONDS =
        "nacos.core.protocol.distro.data.load.retryDelayMs";
    
    /** 全量加载重试间隔状态键。 */
    public static final String DATA_LOAD_RETRY_DELAY_MILLISECONDS_STATE = "data_load_retryDelayMs";
    
    /** 默认全量加载重试间隔：30000 ms。 */
    public static final long DEFAULT_DATA_LOAD_RETRY_DELAY_MILLISECONDS = 30000L;
    
    /** 全量加载超时配置键。 */
    public static final String DATA_LOAD_TIMEOUT_MILLISECONDS =
        "nacos.core.protocol.distro.data.load.timeoutMs";
    
    /** 全量加载超时状态键。 */
    public static final String DATA_LOAD_TIMEOUT_MILLISECONDS_STATE = "data_load_timeoutMs";
    
    /** 默认全量加载超时：30000 ms。 */
    public static final long DEFAULT_DATA_LOAD_TIMEOUT_MILLISECONDS = 30000L;
    
}
