/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.broker.plugin;

import java.util.Map;

/**
 * Broker 附属插件生命周期接口：加载、启停、元数据同步及运行时信息扩展。
 */
public interface BrokerAttachedPlugin {

    /**
     * 插件名称。
     */
    String pluginName();

    /**
     * 加载插件资源。
     *
     * @return 是否加载成功
     */
    boolean load();

    /** 启动插件后台任务。 */
    void start();

    /** 关闭插件并释放资源。 */
    void shutdown();

    /** 从 Master Broker 同步元数据。 */
    void syncMetadata();

    /**
     * 反向从 Slave 拉取元数据（主从角色切换场景）。
     *
     * @param brokerAddr 目标 Broker 地址
     */
    void syncMetadataReverse(String brokerAddr) throws Exception;

    /**
     * 向 Broker 运行时信息 Map 注入插件状态字段。
     */
    void buildRuntimeInfo(Map<String, String> runtimeInfo);

    /**
     * Broker 角色/状态变更回调（如升主、降从）。
     *
     * @param shouldStart 是否应处于运行态
     */
    void statusChanged(boolean shouldStart);

}
