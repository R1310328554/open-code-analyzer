/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.core.exception;

import com.alibaba.nacos.api.exception.NacosException;

/**
 * KV 存储（RocksDB 等）异常：封装 {@link ErrorCode} 与 {@link com.alibaba.nacos.api.exception.NacosException} 错误信息。
 * RocksDB Exception.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class KvStorageException extends NacosException {
    
    /** 无参构造。 */
    public KvStorageException() {
        super();
    }
    
    /**
     * 指定 {@link ErrorCode} 与错误消息。
     *
     * @param code 错误码枚举
     * @param errMsg 错误描述
     */
    public KvStorageException(ErrorCode code, String errMsg) {
        super(code.getCode(), errMsg);
    }
    
    /**
     * 指定 {@link ErrorCode} 与根因异常。
     *
     * @param errCode 错误码枚举
     * @param throwable 根因
     */
    public KvStorageException(ErrorCode errCode, Throwable throwable) {
        super(errCode.getCode(), throwable);
    }
    
    /**
     * 指定错误码、消息与根因。
     *
     * @param errCode 错误码枚举
     * @param errMsg 错误描述
     * @param throwable 根因
     */
    public KvStorageException(ErrorCode errCode, String errMsg, Throwable throwable) {
        super(errCode.getCode(), errMsg, throwable);
    }
    
    /**
     * 指定数值错误码与消息。
     *
     * @param errCode 错误码
     * @param errMsg 错误描述
     */
    public KvStorageException(int errCode, String errMsg) {
        super(errCode, errMsg);
    }
    
    /**
     * 指定数值错误码与根因。
     *
     * @param errCode 错误码
     * @param throwable 根因
     */
    public KvStorageException(int errCode, Throwable throwable) {
        super(errCode, throwable);
    }
    
    /**
     * 指定数值错误码、消息与根因。
     *
     * @param errCode 错误码
     * @param errMsg 错误描述
     * @param throwable 根因
     */
    public KvStorageException(int errCode, String errMsg, Throwable throwable) {
        super(errCode, errMsg, throwable);
    }
    
    /** {@inheritDoc} */
    @Override
    public int getErrCode() {
        return super.getErrCode();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getErrMsg() {
        return super.getErrMsg();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setErrCode(int errCode) {
        super.setErrCode(errCode);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setErrMsg(String errMsg) {
        super.setErrMsg(errMsg);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setCauseThrowable(Throwable throwable) {
        super.setCauseThrowable(throwable);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString();
    }
}
