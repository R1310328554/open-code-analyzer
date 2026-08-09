/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting;

import org.apache.rocketmq.remoting.pipeline.RequestPipeline;

/**
 * Remoting 服务基础接口：生命周期管理与 RPC 钩子注册。
 */
public interface RemotingService {
    /** 启动 Remoting 服务（客户端或服务端）。 */
    void start();

    /** 关闭 Remoting 服务并释放资源。 */
    void shutdown();

    /** 注册 {@link RPCHook} 拦截器。 */
    void registerRPCHook(RPCHook rpcHook);

    /** 设置请求处理流水线。 */
    void setRequestPipeline(RequestPipeline pipeline);

    /** 移除全部已注册的 RPC 钩子。 */
    void clearRPCHook();
}
