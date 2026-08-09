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
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.List;
import java.util.Set;

/**
 * 机器与应用发现 SPI，供 Dashboard 查询注册表及增删机器。
 */
public interface MachineDiscovery {

    /** 集群未启动时使用的占位应用名。 */
    String UNKNOWN_APP_NAME = "CLUSTER_NOT_STARTED";

    /** @return 全部已注册应用名称列表 */
    List<String> getAppNames();

    /** @return 应用摘要集合（含机器列表） */
    Set<AppInfo> getBriefApps();

    /** @param app 应用名
     * @return 应用详情，不存在时返回 null */
    AppInfo getDetailApp(String app);

    /**
     * 从注册表移除整个应用及其全部机器。
     *
     * @param app 应用名称
     * @since 1.5.0
     */
    void removeApp(String app);

    /** 注册或更新一台客户端机器心跳信息。 */
    long addMachine(MachineInfo machineInfo);

    /**
     * 从指定应用中移除一台机器。
     *
     * @param app 应用名
     * @param ip 机器 IP
     * @param port 机器端口
     * @return 移除成功返回 true
     * @since 1.5.0
     */
    boolean removeMachine(String app, String ip, int port);
}