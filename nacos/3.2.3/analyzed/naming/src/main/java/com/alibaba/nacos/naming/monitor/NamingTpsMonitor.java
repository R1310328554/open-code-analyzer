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

import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;

/**
 * Naming 模块 TPS 与流控监控单例。
 *
 * <p>封装 {@link TpsControlManager}，在 RPC 推送与 Distro 同步/校验成功或失败时上报 TPS 采样点，供插件化流控策略统计与限流。</p>
 *
 * @author xiweng.yy
 */
public class NamingTpsMonitor {
    
    /** 全局单例。 */
    private static final NamingTpsMonitor INSTANCE = new NamingTpsMonitor();
    
    /** 插件中心提供的 TPS 流控管理器。 */
    private final TpsControlManager tpsControlManager =
        ControlManagerCenter.getInstance().getTpsControlManager();
    
    private NamingTpsMonitor() {
        registerPushMonitorPoint();
        registerDistroMonitorPoint();
    }
    
    /** 注册 RPC 推送相关 TPS 监控点。 */
    private void registerPushMonitorPoint() {
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_RPC_PUSH.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_RPC_PUSH_SUCCESS.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_RPC_PUSH_FAIL.name());
    }
    
    /** 注册 Distro 同步与校验相关 TPS 监控点。 */
    private void registerDistroMonitorPoint() {
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_SYNC.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_SYNC_SUCCESS.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_SYNC_FAIL.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_VERIFY.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_VERIFY_SUCCESS.name());
        tpsControlManager.registerTpsPoint(TpsMonitorItem.NAMING_DISTRO_VERIFY_FAIL.name());
    }
    
    public static NamingTpsMonitor getInstance() {
        return INSTANCE;
    }
    
    /**
     * 上报 RPC 推送成功 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void rpcPushSuccess(String clientId, String clientIp) {
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_RPC_PUSH.name(), clientId, clientIp));
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_RPC_PUSH_SUCCESS.name(), clientId, clientIp));
    }
    
    /**
     * 上报 RPC 推送失败 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void rpcPushFail(String clientId, String clientIp) {
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_RPC_PUSH.name(), clientId, clientIp));
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_RPC_PUSH_FAIL.name(), clientId, clientIp));
    }
    
    /**
     * 上报 Distro 数据同步成功 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void distroSyncSuccess(String clientId, String clientIp) {
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_SYNC.name(), clientId, clientIp));
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_SYNC_SUCCESS.name(), clientId,
                clientIp));
    }
    
    /**
     * 上报 Distro 数据同步失败 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void distroSyncFail(String clientId, String clientIp) {
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_SYNC.name(), clientId, clientIp));
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_SYNC_FAIL.name(), clientId,
                clientIp));
    }
    
    /**
     * 上报 Distro 数据校验成功 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void distroVerifySuccess(String clientId, String clientIp) {
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_VERIFY.name(), clientId, clientIp));
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_VERIFY_SUCCESS.name(), clientId,
                clientIp));
    }
    
    /**
     * 上报 Distro 数据校验失败 TPS 采样。
     *
     * @param clientId 客户端 ID
     * @param clientIp 客户端 IP
     */
    public static void distroVerifyFail(String clientId, String clientIp) {
        INSTANCE.tpsControlManager.check(
            new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_VERIFY.name(), clientId, clientIp));
        INSTANCE.tpsControlManager
            .check(new TpsCheckRequest(TpsMonitorItem.NAMING_DISTRO_VERIFY_FAIL.name(), clientId,
                clientIp));
    }
    
}
