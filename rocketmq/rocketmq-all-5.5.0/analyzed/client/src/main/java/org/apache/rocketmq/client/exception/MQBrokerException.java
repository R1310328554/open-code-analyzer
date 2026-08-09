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
 * Broker 端返回错误时的客户端异常，携带响应码与 Broker 地址等信息。
 */
public class MQBrokerException extends Exception {
    private static final long serialVersionUID = 5975020272601250368L;
    /** Broker 响应码。 */
    private final int responseCode;
    /** 错误描述信息。 */
    private final String errorMessage;
    /** 发生错误的 Broker 地址。 */
    private final String brokerAddr;

    MQBrokerException() {
        this.responseCode = 0;
        this.errorMessage = null;
        this.brokerAddr = null;
    }

    public MQBrokerException(int responseCode, String errorMessage) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
                + errorMessage));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.brokerAddr = null;
    }

    public MQBrokerException(int responseCode, String errorMessage, String brokerAddr) {
        super(FAQUrl.attachDefaultURL("CODE: " + UtilAll.responseCode2String(responseCode) + "  DESC: "
            + errorMessage + (brokerAddr != null ? " BROKER: " + brokerAddr : "")));
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.brokerAddr = brokerAddr;
    }

    /** 获取响应码。 */
    public int getResponseCode() {
        return responseCode;
    }

    /** 获取错误描述。 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 获取 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }
}
