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

package com.alibaba.nacos.core.distributed.distro.task.execute;

import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.utils.Loggers;

/**
 * Distro 变更同步任务：从本地 {@link com.alibaba.nacos.core.distributed.distro.component.DistroDataStorage} 读取数据并通过 {@link com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent} 推送到目标节点。
 * Distro sync change task.
 *
 * @author xiweng.yy
 */
public class DistroSyncChangeTask extends AbstractDistroExecuteTask {
    
    /** 固定为 CHANGE 操作类型。 */
    private static final DataOperation OPERATION = DataOperation.CHANGE;
    
    /**
     * 构造变更同步任务。
     *
     * @param distroKey 同步键
     * @param distroComponentHolder 组件注册表
     */
    public DistroSyncChangeTask(DistroKey distroKey, DistroComponentHolder distroComponentHolder) {
        super(distroKey, distroComponentHolder);
    }
    
    /** {@inheritDoc} 返回 CHANGE 操作。 */
    @Override
    protected DataOperation getDataOperation() {
        return OPERATION;
    }
    
    /** 无回调模式下读取本地数据并同步到目标节点；无数据时跳过。 */
    @Override
    protected boolean doExecute() {
        String type = getDistroKey().getResourceType();
        DistroData distroData = getDistroData(type);
        if (null == distroData) {
            Loggers.DISTRO.warn("[DISTRO] {} with null data to sync, skip", toString());
            return true;
        }
        return getDistroComponentHolder().findTransportAgent(type)
            .syncData(distroData, getDistroKey().getTargetServer());
    }
    
    /** 带回调模式下异步推送变更数据。 */
    @Override
    protected void doExecuteWithCallback(DistroCallback callback) {
        String type = getDistroKey().getResourceType();
        DistroData distroData = getDistroData(type);
        if (null == distroData) {
            Loggers.DISTRO.warn("[DISTRO] {} with null data to sync, skip", toString());
            return;
        }
        getDistroComponentHolder().findTransportAgent(type)
            .syncData(distroData, getDistroKey().getTargetServer(), callback);
    }
    
    /** 返回便于日志追踪的任务描述。 */
    @Override
    public String toString() {
        return "DistroSyncChangeTask for " + getDistroKey().toString();
    }
    
    /** 从本地存储读取 Distro 数据并标记为 CHANGE 操作。 */
    private DistroData getDistroData(String type) {
        DistroData result =
            getDistroComponentHolder().findDataStorage(type).getDistroData(getDistroKey());
        if (null != result) {
            result.setType(OPERATION);
        }
        return result;
    }
}
