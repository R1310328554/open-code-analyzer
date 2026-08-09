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
package org.apache.rocketmq.remoting.rpc;

import java.util.concurrent.Future;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 面向 Broker 的异步 RPC 客户端接口：支持通用请求与 MessageQueue 维度调用。
 */
public interface RpcClient {


    /**
     * 通用调用：目标 Broker 由请求头 bname 指定；oneway 标志写在请求内，无需单独方法。
     */
    Future<RpcResponse>  invoke(RpcRequest request, long timeoutMs) throws RpcException;

    /**
     * 按 MessageQueue 调用：逻辑队列使用 mock Broker 名，物理地址由 mq 解析。
     */
    Future<RpcResponse>  invoke(MessageQueue mq, RpcRequest request, long timeoutMs) throws RpcException;

}
