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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.common.task.engine.NacosExecuteTaskExecuteEngine;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 命名模块异步执行任务调度器单例。
 *
 * <p>封装 {@link NacosExecuteTaskExecuteEngine}，将命名侧 {@link AbstractExecuteTask} 按 dispatchTag 投递到命名模式线程池执行。</p>
 *
 * @author xiweng.yy
 */
public class NamingExecuteTaskDispatcher {
    
    /** 全局唯一调度器实例。 */
    private static final NamingExecuteTaskDispatcher INSTANCE = new NamingExecuteTaskDispatcher();
    
    private final NacosExecuteTaskExecuteEngine executeEngine;
    
    /** 私有构造，以命名功能模式初始化执行引擎。 */
    private NamingExecuteTaskDispatcher() {
        executeEngine =
            new NacosExecuteTaskExecuteEngine(EnvUtil.FUNCTION_MODE_NAMING, Loggers.SRV_LOG);
    }
    
    /** 获取命名任务调度器单例。 */
    public static NamingExecuteTaskDispatcher getInstance() {
        return INSTANCE;
    }
    
    /** 将任务加入执行引擎队列，由 worker 异步执行。 */
    public void dispatchAndExecuteTask(Object dispatchTag, AbstractExecuteTask task) {
        executeEngine.addTask(dispatchTag, task);
    }
    
    /** 返回执行引擎各 worker 线程运行状态摘要。 */
    public String workersStatus() {
        return executeEngine.workersStatus();
    }
    
    /** 关闭执行引擎，释放线程池资源。 */
    public void destroy() throws Exception {
        executeEngine.shutdown();
    }
}
