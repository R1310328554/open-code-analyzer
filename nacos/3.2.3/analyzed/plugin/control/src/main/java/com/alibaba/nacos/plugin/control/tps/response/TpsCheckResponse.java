/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.tps.response;

/**
 * TPS 限流校验响应。
 *
 * <p>封装校验是否通过、结果码与说明信息，对应 {@link TpsResultCode} 中的各类状态。</p>
 *
 * @author shiyiyue
 */
public class TpsCheckResponse {
    
    /** 是否通过限流校验。 */
    private boolean success;
    
    /** 结果码，参见 {@link TpsResultCode}。 */
    private int code;
    
    /** 结果说明信息。 */
    private String message;
    
    /**
     * 构造校验响应。
     *
     * @param success 是否通过
     * @param code    结果码
     * @param message 说明信息
     */
    public TpsCheckResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取结果码。
     *
     * @return 结果码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置结果码。
     *
     * @param code 结果码
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * 设置是否通过。
     *
     * @param success 是否通过
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * 设置说明信息。
     *
     * @param message 说明信息
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 是否通过限流校验。
     *
     * @return 通过返回 {@code true}
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 获取说明信息。
     *
     * @return 说明信息
     */
    public String getMessage() {
        return message;
    }
}
