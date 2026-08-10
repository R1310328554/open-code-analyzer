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

import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.core.distributed.distro.component.DistroComponentHolder;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;
import com.alibaba.nacos.core.distributed.distro.task.DistroTaskEngineHolder;
import com.alibaba.nacos.core.distributed.distro.task.execute.DistroSyncChangeTask;
import com.alibaba.nacos.core.distributed.distro.task.execute.DistroSyncDeleteTask;

/**
 * Distro 延迟任务默认处理器：延迟到期后根据 {@link com.alibaba.nacos.consistency.DataOperation} 派发 {@link com.alibaba.nacos.core.distributed.distro.task.execute.DistroSyncChangeTask} 或 {@link com.alibaba.nacos.core.distributed.distro.task.execute.DistroSyncDeleteTask} 到同步引擎。
 * Distro delay task processor.
 *
 * @author xiweng.yy
 */
public class DistroDelayTaskProcessor implements NacosTaskProcessor {
    
    /** 任务引擎持有者，用于提交同步执行任务。 */
    private final DistroTaskEngineHolder distroTaskEngineHolder;
    
    /** Distro 组件注册表，供同步任务查找传输代理与存储。 */
    private final DistroComponentHolder distroComponentHolder;
    
    /**
     * 注入任务引擎与组件持有者。
     *
     * @param distroTaskEngineHolder 任务引擎持有者
     * @param distroComponentHolder 组件注册表
     */
    public DistroDelayTaskProcessor(DistroTaskEngineHolder distroTaskEngineHolder,
        DistroComponentHolder distroComponentHolder) {
        this.distroTaskEngineHolder = distroTaskEngineHolder;
        this.distroComponentHolder = distroComponentHolder;
    }
    
    /**
     * 处理延迟任务：DELETE 派发删除同步，ADD/CHANGE 派发变更同步。
     *
     * @param task 待处理的 Nacos 任务
     * @return 是否成功受理
     */
    @Override
    public boolean process(NacosTask task) {
        if (!(task instanceof DistroDelayTask)) {
            return true;
        }
        DistroDelayTask distroDelayTask = (DistroDelayTask) task;
        DistroKey distroKey = distroDelayTask.getDistroKey();
        switch (distroDelayTask.getAction()) {
            case DELETE:
                DistroSyncDeleteTask syncDeleteTask =
                    new DistroSyncDeleteTask(distroKey, distroComponentHolder);
                distroTaskEngineHolder.getExecuteWorkersManager().addTask(distroKey,
                    syncDeleteTask);
                return true;
            case CHANGE:
            case ADD:
                DistroSyncChangeTask syncChangeTask =
                    new DistroSyncChangeTask(distroKey, distroComponentHolder);
                distroTaskEngineHolder.getExecuteWorkersManager().addTask(distroKey,
                    syncChangeTask);
                return true;
            default:
                return false;
        }
    }
}
