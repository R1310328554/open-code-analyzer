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

package org.apache.rocketmq.client.exception;

import org.apache.rocketmq.common.UtilAll;

/**
 * 请求超时异常：客户端向 Broker/NameServer 发起 Remoting 请求未在时限内收到响应时抛出。
 * 携带响应码与错误描述，便于定位网络或 Broker 侧超时。
 */
public class RequestTimeoutException extends Exception {
    private static final long serialVersionUID = -5758410930844185841L;
    /** Remoting 响应码，未知时为 -1。 */
    private int responseCode;
    /** 错误描述信息。 */
    private String errorMessage;

    /** 以自定义消息与根因构造超时异常，响应码默认为 -1。 */
    public RequestTimeoutException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.responseCode = -1;
        this.errorMessage = errorMessage;
    }

    /** 以响应码与错误描述构造；异常消息格式为 CODE/DESC。 */
    public RequestTimeoutException(int responseCode, String errorMessage) {
        super("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    /** 返回 Remoting 响应码。 */
    public int getResponseCode() {
        return responseCode;
    }

    /** 设置响应码并返回自身，便于链式调用。 */
    public RequestTimeoutException setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    /** 返回错误描述。 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 设置错误描述。 */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
