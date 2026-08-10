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
 * 应立即执行的抽象任务基类：实现 {@link NacosTask} 与 {@link Runnable}，
 * {@link #shouldProcess()} 恒为 true，由 {@link com.alibaba.nacos.common.task.engine.NacosExecuteTaskExecuteEngine}
 * 分派到 {@link com.alibaba.nacos.common.task.engine.TaskExecuteWorker} 线程池即时运行。
 * Abstract task which should be executed immediately.
 *
 * @author xiweng.yy
 */
public abstract class AbstractExecuteTask implements NacosTask, Runnable {
    
    /** 部分子类复用的默认间隔常量：3000 毫秒 */
    protected static final long INTERVAL = 3000L;
    
    /** 即时任务始终允许处理 */
    @Override
    public boolean shouldProcess() {
        return true;
    }
}
