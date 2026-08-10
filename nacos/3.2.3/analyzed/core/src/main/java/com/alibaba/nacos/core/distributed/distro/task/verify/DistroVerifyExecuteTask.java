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

package com.alibaba.nacos.core.distributed.distro.task.verify;

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord;
import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecordsHolder;
import com.alibaba.nacos.core.utils.Loggers;

import java.util.List;

/**
 * Distro 校验执行任务：将本地 {@link com.alibaba.nacos.core.distributed.distro.entity.DistroData} 校验摘要发送到目标节点，不一致时由对端触发修复。
 * Execute distro verify task.
 *
 * @author xiweng.yy
 */
public class DistroVerifyExecuteTask extends AbstractExecuteTask {
    
    /** 传输代理，负责发送校验数据。 */
    private final DistroTransportAgent transportAgent;
    
    /** 待校验的数据列表。 */
    private final List<DistroData> verifyData;
    
    /** 目标节点地址。 */
    private final String targetServer;
    
    /** 资源类型，用于日志与监控。 */
    private final String resourceType;
    
    /**
     * 构造校验执行任务。
     *
     * @param transportAgent 传输代理
     * @param verifyData 校验数据列表
     * @param targetServer 目标节点地址
     * @param resourceType 资源类型
     */
    public DistroVerifyExecuteTask(DistroTransportAgent transportAgent, List<DistroData> verifyData,
        String targetServer, String resourceType) {
        this.transportAgent = transportAgent;
        this.verifyData = verifyData;
        this.targetServer = targetServer;
        this.resourceType = resourceType;
    }
    
    /** 逐条发送校验数据，支持回调与非回调传输模式。 */
    @Override
    public void run() {
        for (DistroData each : verifyData) {
            try {
                if (transportAgent.supportCallbackTransport()) {
                    doSyncVerifyDataWithCallback(each);
                } else {
                    doSyncVerifyData(each);
                }
            } catch (Exception e) {
                Loggers.DISTRO
                    .error("[DISTRO-FAILED] verify data for type {} to {} failed.", resourceType,
                        targetServer, e);
            }
        }
    }
    
    /** 带回调模式发送单条校验数据。 */
    private void doSyncVerifyDataWithCallback(DistroData data) {
        transportAgent.syncVerifyData(data, targetServer, new DistroVerifyCallback());
    }
    
    /** 同步模式发送单条校验数据。 */
    private void doSyncVerifyData(DistroData data) {
        transportAgent.syncVerifyData(data, targetServer);
    }
    
    /** 校验回调：失败时递增 {@link com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord} 校验失败计数。 */
    private class DistroVerifyCallback implements DistroCallback {
        
        /** 校验成功，debug 级别记录日志。 */
        @Override
        public void onSuccess() {
            if (Loggers.DISTRO.isDebugEnabled()) {
                Loggers.DISTRO.debug("[DISTRO] verify data for type {} to {} success", resourceType,
                    targetServer);
            }
        }
        
        /** 校验失败，更新监控计数并记录 debug 日志。 */
        @Override
        public void onFailed(Throwable throwable) {
            DistroRecord distroRecord = DistroRecordsHolder.getInstance().getRecord(resourceType);
            distroRecord.verifyFail();
            if (Loggers.DISTRO.isDebugEnabled()) {
                Loggers.DISTRO
                    .debug("[DISTRO-FAILED] verify data for type {} to {} failed.", resourceType,
                        targetServer,
                        throwable);
            }
        }
    }
}
