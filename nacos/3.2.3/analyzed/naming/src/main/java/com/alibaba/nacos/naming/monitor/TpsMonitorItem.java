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

package com.alibaba.nacos.naming.monitor;

/**
 * Naming 模块 TPS 监控点枚举。
 *
 * <p>每个枚举值对应 {@link TpsControlManager} 中注册的一个 TPS 采样点，覆盖 RPC 推送与 Distro 同步/校验的成功与失败路径。</p>
 *
 * @author xiweng.yy
 */
public enum TpsMonitorItem {
    
    /**
     * RPC 推送总次数 TPS 监控点。
     */
    NAMING_RPC_PUSH,
    
    /**
     * RPC 推送成功次数 TPS 监控点。
     */
    NAMING_RPC_PUSH_SUCCESS,
    
    /**
     * RPC 推送失败次数 TPS 监控点。
     */
    NAMING_RPC_PUSH_FAIL,
    
    /**
     * Distro 数据同步总次数 TPS 监控点。
     */
    NAMING_DISTRO_SYNC,
    
    /**
     * Distro 数据同步成功次数 TPS 监控点。
     */
    NAMING_DISTRO_SYNC_SUCCESS,
    
    /**
     * Distro 数据同步失败次数 TPS 监控点。
     */
    NAMING_DISTRO_SYNC_FAIL,
    
    /**
     * Distro 数据校验总次数 TPS 监控点。
     */
    NAMING_DISTRO_VERIFY,
    
    /**
     * Distro 数据校验成功次数 TPS 监控点。
     */
    NAMING_DISTRO_VERIFY_SUCCESS,
    
    /**
     * Distro 数据校验失败次数 TPS 监控点。
     */
    NAMING_DISTRO_VERIFY_FAIL,
}
