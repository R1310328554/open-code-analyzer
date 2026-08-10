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

package com.alibaba.nacos.config.server.service.dump.task;

import com.alibaba.nacos.common.task.AbstractDelayTask;

/**
 * 全量配置 Dump 延迟任务：将数据库中全部正式配置加载至本地缓存，
 * 通常在节点启动或运维触发时使用；{@code startUp} 标记是否为启动阶段执行。
 * Dump all task.
 *
 * @author Nacos
 * @date 2020/7/5 12:17 PM
 */
public class DumpAllTask extends AbstractDelayTask {
    
    /** 是否为启动阶段触发的全量 Dump，用于区分冷启动与运维手动刷新。 */
    private boolean startUp;
    
    public DumpAllTask() {
    }
    
    /**
     * 构造全量 Dump 任务并指定是否启动场景。
     *
     * @param startUp 启动阶段为 true，否则为 false
     */
    public DumpAllTask(boolean startUp) {
        this.startUp = startUp;
    }
    
    /** 返回当前任务是否由节点启动流程触发。 */
    public boolean isStartUp() {
        return startUp;
    }
    
    @Override
    public void merge(AbstractDelayTask task) {
    }
    
    /** 全量 Dump 任务在延迟队列中的唯一 ID。 */
    public static final String TASK_ID = "dumpAllConfigTask";
}
