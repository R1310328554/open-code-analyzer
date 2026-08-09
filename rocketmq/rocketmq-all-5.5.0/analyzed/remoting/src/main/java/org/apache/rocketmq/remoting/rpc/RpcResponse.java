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

import org.apache.rocketmq.remoting.CommandCustomHeader;

/**
 * RPC 响应封装：响应码、Header、Body 或内嵌 RpcException。
 */
public class RpcResponse   {
    /** Remoting 响应码。 */
    private int code;
    /** 响应自定义头。 */
    private CommandCustomHeader header;
    /** 响应体。 */
    private Object body;
    /** 失败时的 RPC 异常（成功时为 null）。 */
    public RpcException exception;

    /** 默认构造。 */
    public RpcResponse() {

    }

    /** 构造成功响应。 */
    public RpcResponse(int code, CommandCustomHeader header, Object body) {
        this.code = code;
        this.header = header;
        this.body = body;
    }

    /** 由异常构造失败响应。 */
    public RpcResponse(RpcException rpcException) {
        this.code = rpcException.getErrorCode();
        this.exception = rpcException;
    }

    /** 返回响应码。 */
    public int getCode() {
        return code;
    }

    /** 返回响应头。 */
    public CommandCustomHeader getHeader() {
        return header;
    }

    /** 设置响应头。 */
    public void setHeader(CommandCustomHeader header) {
        this.header = header;
    }

    /** 返回响应体。 */
    public Object getBody() {
        return body;
    }

    /** 设置响应体。 */
    public void setBody(Object body) {
        this.body = body;
    }

    /** 返回内嵌异常。 */
    public RpcException getException() {
        return exception;
    }

    /** 设置内嵌异常。 */
    public void setException(RpcException exception) {
        this.exception = exception;
    }

}
