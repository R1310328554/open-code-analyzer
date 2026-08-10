/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.context.addition;

import com.alibaba.nacos.api.common.Constants;

/**
 * 基础请求上下文：封装协议类型、请求目标、客户端 UA、应用名、编码及嵌套的 {@link AddressContext}。
 * Nacos request basic information context.
 *
 * @author xiweng.yy
 */
public class BasicContext {
    
    /** 未上报应用名时的默认值。 */
    private static final String DEFAULT_APP = "unknown";
    
    /** HTTP 协议标识常量。 */
    public static final String HTTP_PROTOCOL = "HTTP";
    
    /** gRPC 协议标识常量。 */
    public static final String GRPC_PROTOCOL = "GRPC";
    
    /** 请求源/连接地址信息。 */
    private final AddressContext addressContext;
    
    /** 客户端 User-Agent，例如 Nacos-Java-client:v2.4.0。 */
    private String userAgent;
    
    /** 请求协议类型：HTTP、gRPC 等。 */
    private String requestProtocol;
    
    /**
     * 请求目标描述。
     * <ul>
     *     <li>HTTP：`${Method} ${URI}`，如 {@code POST /v2/ns/instance}</li>
     *     <li>gRPC：请求类名，如 {@code InstanceRequest}</li>
     * </ul>
     */
    private String requestTarget;
    
    /** 可选，客户端上报的应用名，默认 {@code unknown}。 */
    private String app;
    
    /** 可选，请求体字符编码，默认 {@code UTF-8}。 */
    private String encoding;
    
    /** 初始化地址上下文并设置默认 app 与 encoding。 */
    public BasicContext() {
        this.addressContext = new AddressContext();
        this.app = DEFAULT_APP;
        this.encoding = Constants.ENCODE;
    }
    
    /** 返回地址子上下文。 */
    public AddressContext getAddressContext() {
        return addressContext;
    }
    
    /** 返回 User-Agent。 */
    public String getUserAgent() {
        return userAgent;
    }
    
    /** 设置 User-Agent。 */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /** 返回协议类型。 */
    public String getRequestProtocol() {
        return requestProtocol;
    }
    
    /** 设置协议类型。 */
    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }
    
    /** 返回请求目标字符串。 */
    public String getRequestTarget() {
        return requestTarget;
    }
    
    /** 设置请求目标字符串。 */
    public void setRequestTarget(String requestTarget) {
        this.requestTarget = requestTarget;
    }
    
    /** 返回应用名。 */
    public String getApp() {
        return app;
    }
    
    /** 设置应用名。 */
    public void setApp(String app) {
        this.app = app;
    }
    
    /** 返回字符编码。 */
    public String getEncoding() {
        return encoding;
    }
    
    /** 设置字符编码。 */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
