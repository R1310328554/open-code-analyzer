/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.exception.api;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.utils.StringUtils;

/**
 * 开放 API（v2.0）专用异常。
 *
 * <p>字段映射关系：</p>
 * <ul>
 *   <li>{@code errCode} — HTTP 状态码，继承自 {@link NacosException}</li>
 *   <li>{@code errMsg} — 详细错误消息，继承自 {@link NacosException}</li>
 *   <li>{@code detailErrCode} — API v2.0 业务错误码</li>
 *   <li>{@code errAbstract} — API v2.0 错误摘要</li>
 * </ul>
 *
 * @author dongyafei
 * @date 2022/7/22
 */
public class NacosApiException extends NacosException {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 2245627968556056573L;
    
    /** API v2.0 业务错误码。 */
    private int detailErrCode;
    
    /** API v2.0 错误摘要描述。 */
    private String errAbstract;
    
    /** 无参构造。 */
    public NacosApiException() {
    }
    
    /**
     * 构造带 HTTP 状态码、ErrorCode 与根因的 API 异常。
     *
     * @param statusCode HTTP 状态码
     * @param errorCode  API v2.0 错误码枚举
     * @param throwable  根因异常
     * @param message    详细错误消息
     */
    public NacosApiException(int statusCode, ErrorCode errorCode, Throwable throwable,
        String message) {
        super(statusCode, message, throwable);
        this.detailErrCode = errorCode.getCode();
        this.errAbstract = errorCode.getMsg();
    }
    
    /**
     * 构造带 HTTP 状态码与 ErrorCode 的 API 异常。
     *
     * @param statusCode HTTP 状态码
     * @param errorCode  API v2.0 错误码枚举
     * @param message    详细错误消息
     */
    public NacosApiException(int statusCode, ErrorCode errorCode, String message) {
        super(statusCode, message);
        this.detailErrCode = errorCode.getCode();
        this.errAbstract = errorCode.getMsg();
    }
    
    /** 获取 API v2.0 业务错误码。 */
    public int getDetailErrCode() {
        return detailErrCode;
    }
    
    /** 获取 API v2.0 错误摘要（为空时返回 {@link Constants#NULL}）。 */
    public String getErrAbstract() {
        if (!StringUtils.isBlank(this.errAbstract)) {
            return this.errAbstract;
        }
        return Constants.NULL;
    }
}
