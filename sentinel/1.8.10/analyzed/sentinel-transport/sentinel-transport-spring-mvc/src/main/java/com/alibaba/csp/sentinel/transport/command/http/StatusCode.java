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
package com.alibaba.csp.sentinel.transport.command.http;

/**
 * Spring MVC 命令 API 使用的 HTTP 状态码枚举。
 *
 * @author Jason Joo
 */
public enum StatusCode {
    /** 200 成功。 */
    OK(200, "OK"),
    /** 400 请求格式或参数错误。 */
    BAD_REQUEST(400, "Bad Request"),
    /** 408 请求超时。 */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /** 411 缺少 Content-Length。 */
    LENGTH_REQUIRED(411, "Length Required"),
    /** 415 不支持的 Content-Type。 */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /** 500 服务端内部错误。 */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
    
    /** HTTP 数字状态码。 */
    private int code;
    /** 状态描述短语。 */
    private String desc;
    /** 形如 "200 OK" 的完整状态行片段。 */
    private String representation;
    
    StatusCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
        this.representation = code + " " + desc;
    }
    
    /** @return HTTP 数字状态码。 */
    public int getCode() {
        return code;
    }
    
    /** @return 状态描述。 */
    public String getDesc() {
        return desc;
    }
    
    @Override
    public String toString() {
        return representation;
    }
}
