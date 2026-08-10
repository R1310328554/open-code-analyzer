/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.exception.runtime;

/**
 * Nacos 运行时异常基类。
 *
 * <p>携带整型 {@link #errCode} 与格式化错误消息，供客户端 SDK 统一处理。</p>
 *
 * @author yangyi
 */
public class NacosRuntimeException extends RuntimeException {
    
    private static final long serialVersionUID = 3513491993982293262L;
    
    /** 错误消息格式：{@code errCode: %d, errMsg: %s}。 */
    public static final String ERROR_MESSAGE_FORMAT = "errCode: %d, errMsg: %s ";
    
    /** Nacos 错误码。 */
    private final int errCode;
    
    /**
     * 构造仅含错误码的运行时异常。
     *
     * @param errCode Nacos 错误码
     */
    public NacosRuntimeException(int errCode) {
        super();
        this.errCode = errCode;
    }
    
    /**
     * 构造带错误码与消息的运行时异常。
     *
     * @param errCode Nacos 错误码
     * @param errMsg  错误消息
     */
    public NacosRuntimeException(int errCode, String errMsg) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg));
        this.errCode = errCode;
    }
    
    /**
     * 构造带错误码与根因的运行时异常。
     *
     * @param errCode   Nacos 错误码
     * @param throwable 根因异常
     */
    public NacosRuntimeException(int errCode, Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
    }
    
    /**
     * 构造带错误码、消息与根因的运行时异常。
     *
     * @param errCode   Nacos 错误码
     * @param errMsg    错误消息
     * @param throwable 根因异常
     */
    public NacosRuntimeException(int errCode, String errMsg, Throwable throwable) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg), throwable);
        this.errCode = errCode;
    }
    
    /** 获取 Nacos 错误码。 */
    public int getErrCode() {
        return errCode;
    }
}
