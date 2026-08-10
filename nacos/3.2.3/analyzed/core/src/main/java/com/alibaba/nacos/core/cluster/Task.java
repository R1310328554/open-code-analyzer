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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.core.utils.Loggers;

/**
 * 集群后台任务抽象基类：封装执行体、异常日志与完成后调度逻辑。
 * task.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class Task implements Runnable {
    
    /** 任务是否已请求停止。 */
    protected volatile boolean shutdown = false;
    
    @Override
    public void run() {
        if (shutdown) {
            return;
        }
        try {
            executeBody();
        } catch (Throwable t) {
            Loggers.CORE.error("this task execute has error : {}", ExceptionUtil.getStackTrace(t));
        } finally {
            if (!shutdown) {
                after();
            }
        }
    }
    
    /**
     * 任务核心执行逻辑，由子类实现。
     */
    protected abstract void executeBody();
    
    /**
     * {@link #executeBody()} 完成后回调，通常用于再次调度。
     */
    protected void after() {
        
    }
    
    /** 标记任务停止，后续 {@link #run()} 将不再调度。 */
    public void shutdown() {
        shutdown = true;
    }
    
}
