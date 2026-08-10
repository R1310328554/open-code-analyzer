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

package com.alibaba.nacos.core.distributed.distro.task.load;

import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.distro.DistroConfig;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroDataProcessor;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.core.utils.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Distro 启动全量加载任务：等待集群成员与数据存储就绪后，从远端节点拉取各资源类型快照并写入本地 {@link com.alibaba.nacos.core.distributed.distro.component.DistroDataProcessor}，未完成时按配置间隔重试。
 * Distro load data task.
 *
 * @author xiweng.yy
 */
public class DistroLoadDataTask implements Runnable {
    
    /** 集群成员管理器，提供除自身外的节点列表。 */
    private final ServerMemberManager memberManager;
    
    /** Distro 组件注册表。 */
    private final DistroComponentHolder distroComponentHolder;
    
    /** Distro 配置，含加载重试间隔等参数。 */
    private final DistroConfig distroConfig;
    
    /** 全量加载完成或失败时的回调。 */
    private final DistroCallback loadCallback;
    
    /** 各资源类型的加载完成状态。 */
    private final Map<String, Boolean> loadCompletedMap;
    
    /**
     * 注入成员管理、组件、配置与回调依赖。
     *
     * @param memberManager 集群成员管理器
     * @param distroComponentHolder 组件注册表
     * @param distroConfig Distro 配置
     * @param loadCallback 加载结果回调
     */
    public DistroLoadDataTask(ServerMemberManager memberManager,
        DistroComponentHolder distroComponentHolder,
        DistroConfig distroConfig, DistroCallback loadCallback) {
        this.memberManager = memberManager;
        this.distroComponentHolder = distroComponentHolder;
        this.distroConfig = distroConfig;
        this.loadCallback = loadCallback;
        loadCompletedMap = new HashMap<>(1);
    }
    
    /**
     * 执行加载：全部资源类型成功后回调 onSuccess，否则按配置延迟重试；异常时回调 onFailed。
     */
    @Override
    public void run() {
        try {
            load();
            if (!checkCompleted()) {
                GlobalExecutor.submitLoadDataTask(this, distroConfig.getLoadDataRetryDelayMillis());
            } else {
                loadCallback.onSuccess();
                Loggers.DISTRO.info("[DISTRO-INIT] load snapshot data success");
            }
        } catch (Exception e) {
            loadCallback.onFailed(e);
            Loggers.DISTRO.error("[DISTRO-INIT] load snapshot data failed. ", e);
        }
    }
    
    /** 等待集群与存储注册就绪，逐资源类型从远端拉取快照。 */
    private void load() throws Exception {
        while (memberManager.allMembersWithoutSelf().isEmpty()) {
            Loggers.DISTRO.info("[DISTRO-INIT] waiting server list init...");
            TimeUnit.SECONDS.sleep(1);
        }
        while (distroComponentHolder.getDataStorageTypes().isEmpty()) {
            Loggers.DISTRO.info("[DISTRO-INIT] waiting distro data storage register...");
            TimeUnit.SECONDS.sleep(1);
        }
        for (String each : distroComponentHolder.getDataStorageTypes()) {
            if (!loadCompletedMap.containsKey(each) || !loadCompletedMap.get(each)) {
                loadCompletedMap.put(each, loadAllDataSnapshotFromRemote(each));
            }
        }
    }
    
    /**
     * 从各远端成员拉取指定资源类型的全量快照，成功则标记存储初始化完成。
     *
     * @param resourceType 资源类型
     * @return 是否至少从一个节点成功加载
     */
    private boolean loadAllDataSnapshotFromRemote(String resourceType) {
        DistroTransportAgent transportAgent =
            distroComponentHolder.findTransportAgent(resourceType);
        DistroDataProcessor dataProcessor = distroComponentHolder.findDataProcessor(resourceType);
        if (null == transportAgent || null == dataProcessor) {
            Loggers.DISTRO.warn(
                "[DISTRO-INIT] Can't find component for type {}, transportAgent: {}, dataProcessor: {}",
                resourceType, transportAgent, dataProcessor);
            return false;
        }
        for (Member each : memberManager.allMembersWithoutSelf()) {
            long startTime = System.currentTimeMillis();
            try {
                Loggers.DISTRO.info("[DISTRO-INIT] load snapshot {} from {}", resourceType,
                    each.getAddress());
                DistroData distroData = transportAgent.getDatumSnapshot(each.getAddress());
                Loggers.DISTRO.info(
                    "[DISTRO-INIT] it took {} ms to load snapshot {} from {} and snapshot size is {}.",
                    System.currentTimeMillis() - startTime, resourceType, each.getAddress(),
                    getDistroDataLength(distroData));
                boolean result = dataProcessor.processSnapshot(distroData);
                Loggers.DISTRO
                    .info("[DISTRO-INIT] load snapshot {} from {} result: {}", resourceType,
                        each.getAddress(),
                        result);
                if (result) {
                    distroComponentHolder.findDataStorage(resourceType).finishInitial();
                    return true;
                }
            } catch (Exception e) {
                Loggers.DISTRO.error("[DISTRO-INIT] load snapshot {} from {} failed.", resourceType,
                    each.getAddress(), e);
            }
        }
        return false;
    }
    
    /** 返回快照内容字节长度，用于日志统计。 */
    private static int getDistroDataLength(DistroData distroData) {
        return distroData != null && distroData.getContent() != null
            ? distroData.getContent().length : 0;
    }
    
    /** 检查所有已注册资源类型是否均加载成功。 */
    private boolean checkCompleted() {
        if (distroComponentHolder.getDataStorageTypes().size() != loadCompletedMap.size()) {
            return false;
        }
        for (Boolean each : loadCompletedMap.values()) {
            if (!each) {
                return false;
            }
        }
        return true;
    }
}
