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
 * 全量标签配置 Dump 延迟任务：触发将持久层中全部带标签（Tag）的配置
 * 同步到本地内存缓存，供配置服务快速读取。
 * Dump all tag task.
 *
 * @author Nacos
 * @date 2020/7/5 12:19 PM
 */
public class DumpAllTagTask extends AbstractDelayTask {
    
    /** 合并同类型延迟任务；本任务无需合并逻辑，空实现。 */
    @Override
    public void merge(AbstractDelayTask task) {
    }
    
    /** 任务队列唯一标识，用于 {@link com.alibaba.nacos.common.task.NacosTaskProcessor} 路由。 */
    public static final String TASK_ID = "dumpAllTagConfigTask";
}
