/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.redo.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.task.AbstractExecuteTask;
import org.slf4j.Logger;

/**
 * 客户端 Redo 定时任务抽象基类。
 *
 * <p>由 {@link AbstractRedoService} 调度；连接断开时跳过执行，连接正常时调用子类 {@link #redoData()}。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractRedoTask<S extends AbstractRedoService> extends AbstractExecuteTask {
    
    /** 模块日志器。 */
    private final Logger logger;
    
    /** 关联的 redo 服务实例。 */
    private final S redoService;
    
    /** 绑定日志器与 redo 服务。 */
    public AbstractRedoTask(Logger logger, S redoService) {
        this.logger = logger;
        this.redoService = redoService;
    }
    
    /** 未连接时跳过；否则执行 redo 并捕获异常避免任务终止。 */
    @Override
    public void run() {
        if (!redoService.isConnected()) {
            logger.warn("Grpc Connection is disconnect, skip current redo task");
            return;
        }
        try {
            redoData();
        } catch (Exception e) {
            logger.warn("Redo task run with unexpected exception: ", e);
        }
    }
    
    /**
     * 子类实现具体 redo 逻辑（注册/注销重试等）。
     *
     * @throws NacosException redo 失败时抛出
     */
    protected abstract void redoData() throws NacosException;
    
    /** 返回关联的 redo 服务。 */
    protected S getRedoService() {
        return redoService;
    }
}
