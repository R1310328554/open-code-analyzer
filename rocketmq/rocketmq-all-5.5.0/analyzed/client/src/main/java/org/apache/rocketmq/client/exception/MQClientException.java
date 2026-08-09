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
import org.apache.rocketmq.common.help.FAQUrl;

/**
 * RocketMQ 客户端通用异常，封装响应码与错误信息。
 */
public class MQClientException extends Exception {
    private static final long serialVersionUID = -5758410930844185841L;
    /** 响应码，-1 表示未设置。 */
    private int responseCode;
    /** 错误描述信息。 */
    private String errorMessage;

    public MQClientException(String errorMessage, Throwable cause) {
        super(FAQUrl.attachDefaultURL(errorMessage), cause);
        this.responseCode = -1;
        this.errorMessage = errorMessage;
    }

    public MQClientException(int responseCode, String errorMessage) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    public MQClientException(int responseCode, String errorMessage, Throwable cause) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage), cause);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    /** 获取响应码。 */
    public int getResponseCode() {
        return responseCode;
    }

    /** 设置响应码并返回自身（链式调用）。 */
    public MQClientException setResponseCode(final int responseCode) {
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
