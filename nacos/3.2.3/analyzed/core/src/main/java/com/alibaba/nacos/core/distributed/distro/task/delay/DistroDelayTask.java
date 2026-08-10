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

package com.alibaba.nacos.core.distributed.distro.task.delay;

import com.alibaba.nacos.common.task.AbstractDelayTask;
import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;

/**
 * Distro 延迟同步任务：封装 {@link com.alibaba.nacos.core.distributed.distro.entity.DistroKey}、数据操作类型与延迟间隔，支持同键任务合并。
 * Distro delay task.
 *
 * @author xiweng.yy
 */
public class DistroDelayTask extends AbstractDelayTask {
    
    /** 待同步的 Distro 键（资源类型、键值、目标节点）。 */
    private final DistroKey distroKey;
    
    /** 数据操作类型：ADD/CHANGE/DELETE。 */
    private DataOperation action;
    
    /** 任务创建时间戳，用于合并时比较新旧操作。 */
    private long createTime;
    
    /**
     * 构造默认 CHANGE 操作的延迟任务。
     *
     * @param distroKey 同步键
     * @param delayTime 延迟执行间隔（毫秒）
     */
    public DistroDelayTask(DistroKey distroKey, long delayTime) {
        this(distroKey, DataOperation.CHANGE, delayTime);
    }
    
    /**
     * 构造指定操作类型的延迟任务。
     *
     * @param distroKey 同步键
     * @param action 数据操作类型
     * @param delayTime 延迟执行间隔（毫秒）
     */
    public DistroDelayTask(DistroKey distroKey, DataOperation action, long delayTime) {
        this.distroKey = distroKey;
        this.action = action;
        this.createTime = System.currentTimeMillis();
        setLastProcessTime(createTime);
        setTaskInterval(delayTime);
    }
    
    /** 返回 Distro 同步键。 */
    public DistroKey getDistroKey() {
        return distroKey;
    }
    
    /** 返回数据操作类型。 */
    public DataOperation getAction() {
        return action;
    }
    
    /** 返回任务创建时间。 */
    public long getCreateTime() {
        return createTime;
    }
    
    /**
     * 合并同键延迟任务：操作类型冲突时保留较新创建时间的操作，并继承上次处理时间以重置延迟窗口。
     *
     * @param task 待合并的延迟任务
     */
    @Override
    public void merge(AbstractDelayTask task) {
        if (!(task instanceof DistroDelayTask)) {
            return;
        }
        DistroDelayTask oldTask = (DistroDelayTask) task;
        if (!action.equals(oldTask.getAction()) && createTime < oldTask.getCreateTime()) {
            action = oldTask.getAction();
            createTime = oldTask.getCreateTime();
        }
        setLastProcessTime(oldTask.getLastProcessTime());
    }
}
