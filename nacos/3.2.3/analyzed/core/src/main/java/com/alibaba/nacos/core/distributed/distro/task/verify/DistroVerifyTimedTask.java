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

package com.alibaba.nacos.core.distributed.distro.task.verify;

import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroDataStorage;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.task.execute.DistroExecuteTaskExecuteEngine;
import com.alibaba.nacos.core.utils.Loggers;

import java.util.List;

/**
 * Distro 定时校验调度任务：遍历各资源类型，向除自身外的集群成员提交 {@link DistroVerifyExecuteTask} 进行数据一致性校验。
 * Timed to start distro verify task.
 *
 * @author xiweng.yy
 */
public class DistroVerifyTimedTask implements Runnable {
    
    /** 集群成员管理器。 */
    private final ServerMemberManager serverMemberManager;
    
    /** Distro 组件注册表。 */
    private final DistroComponentHolder distroComponentHolder;
    
    /** 同步任务引擎，用于提交校验执行任务。 */
    private final DistroExecuteTaskExecuteEngine executeTaskExecuteEngine;
    
    /**
     * 注入成员管理、组件与执行引擎依赖。
     *
     * @param serverMemberManager 集群成员管理器
     * @param distroComponentHolder 组件注册表
     * @param executeTaskExecuteEngine 同步任务引擎
     */
    public DistroVerifyTimedTask(ServerMemberManager serverMemberManager,
        DistroComponentHolder distroComponentHolder,
        DistroExecuteTaskExecuteEngine executeTaskExecuteEngine) {
        this.serverMemberManager = serverMemberManager;
        this.distroComponentHolder = distroComponentHolder;
        this.executeTaskExecuteEngine = executeTaskExecuteEngine;
    }
    
    /** 获取目标节点列表，对每个资源类型触发校验。 */
    @Override
    public void run() {
        try {
            List<Member> targetServer = serverMemberManager.allMembersWithoutSelf();
            if (Loggers.DISTRO.isDebugEnabled()) {
                Loggers.DISTRO.debug("server list is: {}", targetServer);
            }
            for (String each : distroComponentHolder.getDataStorageTypes()) {
                verifyForDataStorage(each, targetServer);
            }
        } catch (Exception e) {
            Loggers.DISTRO.error("[DISTRO-FAILED] verify task failed.", e);
        }
    }
    
    /**
     * 对指定资源类型向各目标节点提交校验任务；未完成初始化的存储跳过。
     *
     * @param type 资源类型
     * @param targetServer 目标节点列表
     */
    private void verifyForDataStorage(String type, List<Member> targetServer) {
        DistroDataStorage dataStorage = distroComponentHolder.findDataStorage(type);
        if (!dataStorage.isFinishInitial()) {
            Loggers.DISTRO.warn(
                "data storage {} has not finished initial step, do not send verify data",
                dataStorage.getClass().getSimpleName());
            return;
        }
        List<DistroData> verifyData = dataStorage.getVerifyData();
        if (null == verifyData || verifyData.isEmpty()) {
            return;
        }
        for (Member member : targetServer) {
            DistroTransportAgent agent = distroComponentHolder.findTransportAgent(type);
            if (null == agent) {
                continue;
            }
            executeTaskExecuteEngine.addTask(member.getAddress() + type,
                new DistroVerifyExecuteTask(agent, verifyData, member.getAddress(), type));
        }
    }
}
