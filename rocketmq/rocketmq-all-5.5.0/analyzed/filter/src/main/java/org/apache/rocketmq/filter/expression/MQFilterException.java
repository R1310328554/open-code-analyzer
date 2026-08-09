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

package org.apache.rocketmq.filter.expression;

/**
 * 消息过滤异常：封装过滤表达式解析或求值失败时的错误信息。
 * <p>可携带响应码与可读错误描述，供上层统一处理。</p>
 */
public class MQFilterException extends Exception {
    private static final long serialVersionUID = 1L;
    /** 响应码，-1 表示未指定。 */
    private final int responseCode;
    /** 面向调用方的错误描述。 */
    private final String errorMessage;

    /**
     * 由底层异常构造，响应码默认为 -1。
     *
     * @param errorMessage 错误描述
     * @param cause 原始异常
     */
    public MQFilterException(String errorMessage, Throwable cause) {
        super(cause);
        this.responseCode = -1;
        this.errorMessage = errorMessage;
    }

    /**
     * 指定响应码与错误描述构造。
     *
     * @param responseCode 业务响应码
     * @param errorMessage 错误描述
     */
    public MQFilterException(int responseCode, String errorMessage) {
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
    }

    /** @return 响应码 */
    public int getResponseCode() {
        return responseCode;
    }

    /** @return 错误描述文本 */
    public String getErrorMessage() {
        return errorMessage;
    }
}
