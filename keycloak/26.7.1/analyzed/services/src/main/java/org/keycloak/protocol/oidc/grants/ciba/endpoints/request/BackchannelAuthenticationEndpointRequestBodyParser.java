/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.keycloak.protocol.oidc.grants.ciba.endpoints.request;

import java.util.Set;

import jakarta.ws.rs.core.MultivaluedMap;

/**
 * 从表单请求体解析后台认证端点参数。
 * <p>检测重复参数并填充 {@link BackchannelAuthenticationEndpointRequest}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
class BackchannelAuthenticationEndpointRequestBodyParser extends BackchannelAuthenticationEndpointRequestParser {

    /** 表单参数映射 */
    private final MultivaluedMap<String, String> requestParams;

    /** 无效请求消息缓存 */
    private String invalidRequestMessage = null;

    /** @param requestParams 解码后的表单参数 */
    public BackchannelAuthenticationEndpointRequestBodyParser(MultivaluedMap<String, String> requestParams) {
        this.requestParams = requestParams;
    }

    /** @param paramName 参数名 @return 首个参数值 */
    @Override
    protected String getParameter(String paramName) {
        checkDuplicated(requestParams, paramName);
        return requestParams.getFirst(paramName);
    }

    /** @param paramName 参数名 @return 整型参数值，缺失时返回 null */
    @Override
    protected Integer getIntParameter(String paramName) {
        checkDuplicated(requestParams, paramName);
        String paramVal = requestParams.getFirst(paramName);
        return paramVal==null ? null : Integer.valueOf(paramVal);
    }

    /** @return 无效请求消息（如 duplicated parameter） */
    public String getInvalidRequestMessage() {
        return invalidRequestMessage;
    }

    @Override
    protected Set<String> keySet() {
        return requestParams.keySet();
    }

    /** 检测参数是否重复出现 */
    private void checkDuplicated(MultivaluedMap<String, String> requestParams, String paramName) {
        if (invalidRequestMessage == null) {
            if (requestParams.get(paramName) != null && requestParams.get(paramName).size() != 1) {
                invalidRequestMessage = "duplicated parameter";
            }
        }
    }

}
