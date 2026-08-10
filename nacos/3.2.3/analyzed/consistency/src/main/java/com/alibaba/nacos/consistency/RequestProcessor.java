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

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.consistency.entity.ReadRequest;
import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.consistency.entity.WriteRequest;

/**
 * 一致性请求处理器抽象基类：定义读请求、写日志应用、错误回调及业务 group 标识。
 * 可通过 SPI 或 Spring 发现；不同协议有各自的 LogDispatcher，不建议直接实现本接口。
 *
 * Can be discovered through SPI or Spring, This interface is just a function definition interface. Different
 * consistency protocols have their pwd
 * LogDispatcher. It is not recommended to directly implement this interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class RequestProcessor {
    
    /**
     * 处理同步读请求，按 key/group 返回业务数据。
     * get data by key.
     *
     * @param request request {@link com.alibaba.nacos.consistency.entity.ReadRequest}
     * @return target type data
     */
    public abstract Response onRequest(ReadRequest request);
    
    /**
     * 处理已提交的写日志（状态机 apply 阶段）。
     * Process Submitted Log.
     *
     * @param log {@link WriteRequest}
     * @return {@link boolean}
     */
    public abstract Response onApply(WriteRequest log);
    
    /**
     * 不可恢复错误回调，子类可触发业务降级等处理。
     * Irremediable errors that need to trigger business price cuts.
     *
     * @param error {@link Throwable}
     */
    public void onError(Throwable error) {
    }
    
    /**
     * 返回业务唯一 group 名，供状态机将 Log 路由到正确的 Processor。
     * In order for the state machine that handles the transaction to be able to route the Log to the correct
     * LogProcessor, the LogProcessor needs to have an identity information.
     *
     * @return Business unique identification name
     */
    public abstract String group();
    
}
