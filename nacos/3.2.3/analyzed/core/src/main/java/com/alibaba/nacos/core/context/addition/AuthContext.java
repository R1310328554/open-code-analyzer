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

import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Resource;

/**
 * 鉴权上下文：在请求链路中承载身份凭证、访问资源、校验结果与 API 类型，供鉴权插件、处理器及追踪日志读取。
 * Nacos auth context, store and transport some auth plugin information to handler or trace log.
 *
 * @author xiweng.yy
 */
public class AuthContext {
    
    /** 调用方身份上下文（token、用户名等）。 */
    private IdentityContext identityContext;
    
    /** 本次请求对应的受保护资源描述。 */
    private Resource resource;
    
    /** 鉴权结论，见 {@link AuthResult}。 */
    private AuthResult authResult;
    
    /** 自 {@link com.alibaba.nacos.auth.annotation.Secured#apiType()} 解析的 API 类型。 */
    private String apiType;
    
    /** 返回身份上下文。 */
    public IdentityContext getIdentityContext() {
        return identityContext;
    }
    
    /** 设置身份上下文。 */
    public void setIdentityContext(IdentityContext identityContext) {
        this.identityContext = identityContext;
    }
    
    /** 返回访问资源。 */
    public Resource getResource() {
        return resource;
    }
    
    /** 设置访问资源。 */
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    /** 返回鉴权结果。 */
    public AuthResult getAuthResult() {
        return authResult;
    }
    
    /** 设置鉴权结果。 */
    public void setAuthResult(AuthResult authResult) {
        this.authResult = authResult;
    }
    
    /** 返回 API 类型标识。 */
    public String getApiType() {
        return apiType;
    }
    
    /** 设置 API 类型标识。 */
    public void setApiType(String apiType) {
        this.apiType = apiType;
    }
}
