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

package com.alibaba.nacos.common.task;

/**
 * Nacos 任务处理器接口：按任务 key 注册到
 * {@link com.alibaba.nacos.common.task.engine.NacosTaskExecuteEngine}，
 * 引擎取出任务后委托 {@link #process(NacosTask)} 执行业务逻辑。
 * Task processor.
 *
 * @author Nacos
 */
public interface NacosTaskProcessor {
    
    /**
     * 处理单个 Nacos 任务。
     *
     * @param task 待处理任务实例
     * @return true 表示处理成功，false 时延迟引擎会重试入队
     */
    boolean process(NacosTask task);
}
