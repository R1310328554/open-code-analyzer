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

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.distributed.distro.component.DistroCallback;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.component.DistroFailedTaskHandler;
import com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord;
import com.alibaba.nacos.core.distributed.distro.monitor.DistroRecordsHolder;
import com.alibaba.nacos.core.utils.Loggers;

/**
 * Distro 同步执行任务抽象基类：查找 {@link com.alibaba.nacos.core.distributed.distro.component.DistroTransportAgent} 执行同步，支持回调与非回调两种传输模式，失败时委托 {@link com.alibaba.nacos.core.distributed.distro.component.DistroFailedTaskHandler} 重试。
 * Abstract distro execute task.
 *
 * @author xiweng.yy
 */
public abstract class AbstractDistroExecuteTask extends AbstractExecuteTask {
    
    /** 本次同步对应的 Distro 键。 */
    private final DistroKey distroKey;
    
    /** Distro 组件注册表。 */
    private final DistroComponentHolder distroComponentHolder;
    
    /**
     * 保存同步键与组件持有者。
     *
     * @param distroKey 同步键
     * @param distroComponentHolder 组件注册表
     */
    protected AbstractDistroExecuteTask(DistroKey distroKey,
        DistroComponentHolder distroComponentHolder) {
        this.distroKey = distroKey;
        this.distroComponentHolder = distroComponentHolder;
    }
    
    /** 返回 Distro 同步键。 */
    protected DistroKey getDistroKey() {
        return distroKey;
    }
    
    /** 返回组件注册表。 */
    protected DistroComponentHolder getDistroComponentHolder() {
        return distroComponentHolder;
    }
    
    /**
     * 执行同步：按资源类型查找传输代理，支持回调时走异步回调路径，否则同步执行并记录 {@link com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord}。
     */
    @Override
    public void run() {
        String type = getDistroKey().getResourceType();
        DistroTransportAgent transportAgent = distroComponentHolder.findTransportAgent(type);
        if (null == transportAgent) {
            Loggers.DISTRO.warn("No found transport agent for type [{}]", type);
            return;
        }
        Loggers.DISTRO.info("[DISTRO-START] {}", toString());
        if (transportAgent.supportCallbackTransport()) {
            doExecuteWithCallback(new DistroExecuteCallback());
        } else {
            executeDistroTask();
        }
    }
    
    /** 非回调模式下执行同步并处理失败重试。 */
    private void executeDistroTask() {
        try {
            boolean result = doExecute();
            if (!result) {
                handleFailedTask();
            }
            Loggers.DISTRO.info("[DISTRO-END] {} result: {}", toString(), result);
        } catch (Exception e) {
            Loggers.DISTRO.warn("[DISTRO] Sync data change failed.", e);
            handleFailedTask();
        }
    }
    
    /**
     * 返回当前任务对应的数据操作类型。
     *
     * @return data operation
     */
    protected abstract DataOperation getDataOperation();
    
    /**
     * 子类实现的具体同步逻辑（无回调）。
     *
     * @return result of execute
     */
    protected abstract boolean doExecute();
    
    /**
     * 子类实现的带回调同步逻辑。
     *
     * @param callback callback
     */
    protected abstract void doExecuteWithCallback(DistroCallback callback);
    
    /**
     * 同步失败时查找 {@link com.alibaba.nacos.core.distributed.distro.component.DistroFailedTaskHandler} 触发重试。
     */
    protected void handleFailedTask() {
        String type = getDistroKey().getResourceType();
        DistroFailedTaskHandler failedTaskHandler =
            distroComponentHolder.findFailedTaskHandler(type);
        if (null == failedTaskHandler) {
            Loggers.DISTRO.warn("[DISTRO] Can't find failed task for type {}, so discarded", type);
            return;
        }
        failedTaskHandler.retry(getDistroKey(), getDataOperation());
    }
    
    /** 同步回调：成功/失败时更新 {@link com.alibaba.nacos.core.distributed.distro.monitor.DistroRecord} 并处理重试。 */
    private class DistroExecuteCallback implements DistroCallback {
        
        /** 同步成功，递增成功计数。 */
        @Override
        public void onSuccess() {
            DistroRecord distroRecord =
                DistroRecordsHolder.getInstance().getRecord(getDistroKey().getResourceType());
            distroRecord.syncSuccess();
            Loggers.DISTRO.info("[DISTRO-END] {} result: true", getDistroKey().toString());
        }
        
        /** 同步失败，递增失败计数并触发重试。 */
        @Override
        public void onFailed(Throwable throwable) {
            DistroRecord distroRecord =
                DistroRecordsHolder.getInstance().getRecord(getDistroKey().getResourceType());
            distroRecord.syncFail();
            if (null == throwable) {
                Loggers.DISTRO.info("[DISTRO-END] {} result: false", getDistroKey().toString());
            } else {
                Loggers.DISTRO.warn("[DISTRO] Sync data change failed. key: {}",
                    getDistroKey().toString(), throwable);
            }
            handleFailedTask();
        }
    }
}
