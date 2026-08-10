/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query.enums;

/**
 * 配置查询链 HTTP 风格响应码枚举，供 {@link com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse} 封装错误信息。
 * ResponseCode.
 *
 * @author Nacos
 */
public enum ResponseCode {
    
    /**
     * 请求成功（200）。
     * Request success.
     */
    SUCCESS(200, "Response ok"),
    
    /**
     * 请求失败（500）。
     * Request failed.
     */
    FAIL(500, "Response fail");
    
    /** HTTP 状态码数值 */
    int code;
    
    /** 响应描述文案 */
    String desc;
    
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
      * <p>配置查询响应码枚举；详见类级说明。</p>
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Getter method for property <tt>desc</tt>.
     *
     * @return property value of desc
      * <p>配置查询响应码枚举；详见类级说明。</p>
     */
    public String getDesc() {
        return desc;
    }
}
