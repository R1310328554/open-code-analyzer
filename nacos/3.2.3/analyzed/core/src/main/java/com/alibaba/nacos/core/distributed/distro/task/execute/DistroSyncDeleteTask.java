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

/**
 * Distro 删除同步任务：构造仅含键与 DELETE 标记的 {@link com.alibaba.nacos.core.distributed.distro.entity.DistroData}，通知目标节点删除对应条目。
 * Distro sync delete task.
 *
 * @author xiweng.yy
 */
public class DistroSyncDeleteTask extends AbstractDistroExecuteTask {
    
    /** 固定为 DELETE 操作类型。 */
    private static final DataOperation OPERATION = DataOperation.DELETE;
    
    /**
     * 构造删除同步任务。
     *
     * @param distroKey 待删除的同步键
     * @param distroComponentHolder 组件注册表
     */
    public DistroSyncDeleteTask(DistroKey distroKey, DistroComponentHolder distroComponentHolder) {
        super(distroKey, distroComponentHolder);
    }
    
    /** {@inheritDoc} 返回 DELETE 操作。 */
    @Override
    protected DataOperation getDataOperation() {
        return OPERATION;
    }
    
    /** 无回调模式下发送删除指令到目标节点。 */
    @Override
    protected boolean doExecute() {
        String type = getDistroKey().getResourceType();
        DistroData distroData = new DistroData();
        distroData.setDistroKey(getDistroKey());
        distroData.setType(OPERATION);
        return getDistroComponentHolder().findTransportAgent(type)
            .syncData(distroData, getDistroKey().getTargetServer());
    }
    
    /** 带回调模式下异步发送删除指令。 */
    @Override
    protected void doExecuteWithCallback(DistroCallback callback) {
        String type = getDistroKey().getResourceType();
        DistroData distroData = new DistroData();
        distroData.setDistroKey(getDistroKey());
        distroData.setType(OPERATION);
        getDistroComponentHolder().findTransportAgent(type)
            .syncData(distroData, getDistroKey().getTargetServer(), callback);
    }
    
    /** 返回便于日志追踪的任务描述。 */
    @Override
    public String toString() {
        return "DistroSyncDeleteTask for " + getDistroKey().toString();
    }
}
