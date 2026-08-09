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
 * RPC 请求封装：RequestCode、自定义 Header 与可选 Body。
 */
public class RpcRequest {
    /** Remoting RequestCode。 */
    int code;
    /** 请求自定义头。 */
    private RpcRequestHeader header;
    /** 请求体（可为 null）。 */
    private Object body;

    /** 构造 RPC 请求。 */
    public RpcRequest(int code, RpcRequestHeader header, Object body) {
        this.code = code;
        this.header = header;
        this.body = body;
    }

    /** 返回请求头。 */
    public RpcRequestHeader getHeader() {
        return header;
    }

    /** 返回请求体。 */
    public Object getBody() {
        return body;
    }

    /** 返回 RequestCode。 */
    public int getCode() {
        return code;
    }
}
