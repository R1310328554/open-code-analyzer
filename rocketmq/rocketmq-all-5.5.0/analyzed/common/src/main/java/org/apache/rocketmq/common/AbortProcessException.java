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
package org.apache.rocketmq.common;

import org.apache.rocketmq.common.help.FAQUrl;

/**
 * Broker Hook 专用异常（SendMessageHook、ConsumeMessageHook、RPCHook）。
 * 执行 Hook 时不会被忽略，表示处理器应立即向客户端返回错误响应；
 * 错误码封装在本异常中。抛出后会改变 Broker 控制流，使 RemotingCommand 立即返回错误，使用前需了解副作用。
 */
public class AbortProcessException extends RuntimeException {
    private static final long serialVersionUID = -5728810933841185841L;
    /** 返回给客户端的响应码。 */
    private int responseCode;
    /** 错误描述信息。 */
    private String errorMessage;

    /** 携带错误信息与根因构造异常（responseCode 默认为 -1）。 */
    public AbortProcessException(String errorMessage, Throwable cause) {
        super(FAQUrl.attachDefaultURL(errorMessage), cause);
        this.responseCode = -1;
        this.errorMessage = errorMessage;
    }

    /** 按响应码与错误描述构造异常。 */
    public AbortProcessException(int responseCode, String errorMessage) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    /** 获取响应码。 */
    public int getResponseCode() {
        return responseCode;
    }

    /** 设置响应码并返回自身（链式调用）。 */
    public AbortProcessException setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    /** 获取错误描述。 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 设置错误描述。 */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}