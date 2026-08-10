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

package com.alibaba.nacos.core.distributed.distro;

import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroDataProcessor;
import com.alibaba.nacos.core.distributed.distro.component.DistroDataStorage;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.distributed.distro.task.DistroTaskEngineHolder;
import com.alibaba.nacos.core.distributed.distro.task.delay.DistroDelayTask;
import com.alibaba.nacos.core.distributed.distro.task.load.DistroLoadDataTask;
import com.alibaba.nacos.core.distributed.distro.task.verify.DistroVerifyTimedTask;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Component;

/**
 * Distro 协议核心入口：协调数据同步、远程查询、接收处理与校验，并在集群模式下启动全量加载与定时校验任务。
 * Distro protocol.
 *
 * @author xiweng.yy
 */
@Component
public class DistroProtocol {
    
    /** 集群成员管理器。 */
    private final ServerMemberManager memberManager;
    
    /** Distro 组件注册表（传输、存储、处理器等）。 */
    private final DistroComponentHolder distroComponentHolder;
    
    /** Distro 延迟/执行类任务引擎持有者。 */
    private final DistroTaskEngineHolder distroTaskEngineHolder;
    
    /** 全量数据是否加载完成（单机模式直接为 true）。 */
    private volatile boolean isInitialized = false;
    
    /** 构造并启动 Distro 后台任务（校验与全量加载）。 */
    public DistroProtocol(ServerMemberManager memberManager,
        DistroComponentHolder distroComponentHolder,
        DistroTaskEngineHolder distroTaskEngineHolder) {
        this.memberManager = memberManager;
        this.distroComponentHolder = distroComponentHolder;
        this.distroTaskEngineHolder = distroTaskEngineHolder;
        startDistroTask();
    }
    
    /** 单机模式跳过任务；集群模式启动校验与加载。 */
    private void startDistroTask() {
        if (EnvUtil.getStandaloneMode()) {
            isInitialized = true;
            return;
        }
        startVerifyTask();
        startLoadTask();
    }
    
    /** 提交全量数据加载任务，成功后标记 initialized。 */
    private void startLoadTask() {
        DistroCallback loadCallback = new DistroCallback() {
            
            @Override
            public void onSuccess() {
                isInitialized = true;
            }
            
            @Override
            public void onFailed(Throwable throwable) {
                isInitialized = false;
            }
        };
        GlobalExecutor.submitLoadDataTask(
            new DistroLoadDataTask(memberManager, distroComponentHolder, DistroConfig.getInstance(),
                loadCallback));
    }
    
    /** 按配置间隔调度分区数据校验任务。 */
    private void startVerifyTask() {
        GlobalExecutor.schedulePartitionDataTimedSync(
            new DistroVerifyTimedTask(memberManager, distroComponentHolder,
                distroTaskEngineHolder.getExecuteWorkersManager()),
            DistroConfig.getInstance().getVerifyIntervalMillis());
    }
    
    /** 返回 Distro 是否已完成初始化（全量加载成功）。 */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 使用 {@link DistroConfig} 默认延迟，向所有远程节点发起同步。
     *
     * @param distroKey distro key of sync data
     * @param action    the action of data operation
     */
    public void sync(DistroKey distroKey, DataOperation action) {
        sync(distroKey, action, DistroConfig.getInstance().getSyncDelayMillis());
    }
    
    /**
     * 向除本机外的所有集群成员同步指定数据。
     *
     * @param distroKey distro key of sync data
     * @param action    the action of data operation
     * @param delay     delay time for sync
     */
    public void sync(DistroKey distroKey, DataOperation action, long delay) {
        for (Member each : memberManager.allMembersWithoutSelf()) {
            syncToTarget(distroKey, action, each.getAddress(), delay);
        }
    }
    
    /**
     * 向指定目标节点投递延迟同步任务。
     *
     * @param distroKey    distro key of sync data
     * @param action       the action of data operation
     * @param targetServer target server
     * @param delay        delay time for sync
     */
    public void syncToTarget(DistroKey distroKey, DataOperation action, String targetServer,
        long delay) {
        DistroKey distroKeyWithTarget =
            new DistroKey(distroKey.getResourceKey(), distroKey.getResourceType(),
                targetServer);
        DistroDelayTask distroDelayTask = new DistroDelayTask(distroKeyWithTarget, action, delay);
        distroTaskEngineHolder.getDelayTaskExecuteEngine().addTask(distroKeyWithTarget,
            distroDelayTask);
        if (Loggers.DISTRO.isDebugEnabled()) {
            Loggers.DISTRO.debug("[DISTRO-SCHEDULE] {} to {}", distroKey, targetServer);
        }
    }
    
    /**
     * 通过传输代理从目标服务器拉取 Distro 数据。
     *
     * @param distroKey data key
     * @return data
     */
    public DistroData queryFromRemote(DistroKey distroKey) {
        if (null == distroKey.getTargetServer()) {
            Loggers.DISTRO.warn("[DISTRO] Can't query data from empty server");
            return null;
        }
        String resourceType = distroKey.getResourceType();
        DistroTransportAgent transportAgent =
            distroComponentHolder.findTransportAgent(resourceType);
        if (null == transportAgent) {
            Loggers.DISTRO.warn("[DISTRO] Can't find transport agent for key {}", resourceType);
            return null;
        }
        return transportAgent.getData(distroKey, distroKey.getTargetServer());
    }
    
    /**
     * 接收远端同步数据，按资源类型路由到对应 {@link DistroDataProcessor} 处理。
     *
     * @param distroData Received data
     * @return true if handle receive data successfully, otherwise false
     */
    public boolean onReceive(DistroData distroData) {
        Loggers.DISTRO.info("[DISTRO] Receive distro data type: {}, key: {}", distroData.getType(),
            distroData.getDistroKey());
        String resourceType = distroData.getDistroKey().getResourceType();
        DistroDataProcessor dataProcessor = distroComponentHolder.findDataProcessor(resourceType);
        if (null == dataProcessor) {
            Loggers.DISTRO.warn("[DISTRO] Can't find data process for received data {}",
                resourceType);
            return false;
        }
        return dataProcessor.processData(distroData);
    }
    
    /**
     * 接收校验数据并交由处理器比对，必要时从源节点拉取完整数据。
     *
     * @param distroData    verify data
     * @param sourceAddress source server address, might be get data from source server
     * @return true if verify data successfully, otherwise false
     */
    public boolean onVerify(DistroData distroData, String sourceAddress) {
        if (Loggers.DISTRO.isDebugEnabled()) {
            Loggers.DISTRO.debug("[DISTRO] Receive verify data type: {}, key: {}",
                distroData.getType(),
                distroData.getDistroKey());
        }
        String resourceType = distroData.getDistroKey().getResourceType();
        DistroDataProcessor dataProcessor = distroComponentHolder.findDataProcessor(resourceType);
        if (null == dataProcessor) {
            Loggers.DISTRO.warn("[DISTRO] Can't find verify data process for received data {}",
                resourceType);
            return false;
        }
        return dataProcessor.processVerifyData(distroData, sourceAddress);
    }
    
    /**
     * 本地查询：从对应 {@link DistroDataStorage} 读取单条 Distro 数据。
     *
     * @param distroKey key of data
     * @return data
     */
    public DistroData onQuery(DistroKey distroKey) {
        String resourceType = distroKey.getResourceType();
        DistroDataStorage distroDataStorage = distroComponentHolder.findDataStorage(resourceType);
        if (null == distroDataStorage) {
            Loggers.DISTRO.warn("[DISTRO] Can't find data storage for received key {}",
                resourceType);
            return new DistroData(distroKey, new byte[0]);
        }
        return distroDataStorage.getDistroData(distroKey);
    }
    
    /**
     * 本地查询：返回指定类型的全量数据快照。
     *
     * @param type datum type
     * @return all datum snapshot
     */
    public DistroData onSnapshot(String type) {
        DistroDataStorage distroDataStorage = distroComponentHolder.findDataStorage(type);
        if (null == distroDataStorage) {
            Loggers.DISTRO.warn("[DISTRO] Can't find data storage for received key {}", type);
            return new DistroData(new DistroKey("snapshot", type), new byte[0]);
        }
        return distroDataStorage.getDatumSnapshot();
    }
}
