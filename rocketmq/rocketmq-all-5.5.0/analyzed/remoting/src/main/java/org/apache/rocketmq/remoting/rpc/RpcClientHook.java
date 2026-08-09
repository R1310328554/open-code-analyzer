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

/**
 * RPC 客户端钩子：在请求发出前或响应返回后可短路返回自定义结果。
 */
public abstract class RpcClientHook {

    /** 请求前拦截；非 null 则直接作为响应返回。 */
    public abstract RpcResponse beforeRequest(RpcRequest rpcRequest) throws RpcException;

    /** 响应后处理；非 null 则替换原响应返回。 */
    public abstract RpcResponse afterResponse(RpcResponse rpcResponse) throws RpcException;

}
