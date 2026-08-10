/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
 */

package org.keycloak.protocol.oidc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.protocol.oidc.OIDCLoginProtocol;

/**
 * OIDC response_type 解析与校验：支持 code、token、id_token、none 及其组合。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OIDCResponseType {

    /** 授权码响应类型 */
    public static final String CODE = OIDCLoginProtocol.CODE_PARAM;
    /** 隐式 flow access token 响应类型 */
    public static final String TOKEN = "token";
    /** ID Token 响应类型 */
    public static final String ID_TOKEN = "id_token";
    /** 无令牌响应（须单独使用） */
    public static final String NONE = "none";

    private static final List<String> ALLOWED_RESPONSE_TYPES = Arrays.asList(CODE, TOKEN, ID_TOKEN, NONE);

    private final List<String> responseTypes;


    private OIDCResponseType(List<String> responseTypes) {
        this.responseTypes = responseTypes;
    }


    /**
     * 解析空格分隔的 response_type 参数并校验合法组合。
     * @param responseTypeParam 原始 response_type 字符串
     * @throws IllegalArgumentException 含不支持类型或非法组合
     */
        if (responseTypeParam == null) {
            throw new IllegalArgumentException("response_type is null");
        }

        String[] responseTypes = responseTypeParam.trim().split(" ");
        List<String> allowedTypes = new ArrayList<>();
        for (String current : responseTypes) {
            if (ALLOWED_RESPONSE_TYPES.contains(current)) {
                allowedTypes.add(current);
            } else {
                throw new IllegalArgumentException("Unsupported response_type");
            }
        }

        validateAllowedTypes(allowedTypes);

        return new OIDCResponseType(allowedTypes);
    }

    /** 合并多个 response_type 字符串的解析结果 */
    public static OIDCResponseType parse(List<String> responseTypes) {
        OIDCResponseType result = new OIDCResponseType(new ArrayList<String>());
        for (String respType : responseTypes) {
            OIDCResponseType responseType = parse(respType);
            result.responseTypes.addAll(responseType.responseTypes);
        }

        return result;
    }

    private static void validateAllowedTypes(List<String> responseTypes) {
        if (responseTypes.size() == 0) {
            throw new IllegalStateException("No responseType provided");
        }
        if (responseTypes.contains(NONE) && responseTypes.size() > 1) {
            throw new IllegalArgumentException("'None' not allowed with some other response_type");
        }

        // 单独 response_type=token 非 OIDC 规范但 OAuth2 支持，保留以兼容 swagger.ui 等纯 OAuth2 客户端
//        if (responseTypes.contains(TOKEN) && responseTypes.size() == 1) {
//            throw new IllegalArgumentException("Not supported to use response_type=token alone");
//        }
    }


    /** @return 是否包含指定 response_type */
    public boolean hasResponseType(String responseType) {
        return responseTypes.contains(responseType);
    }

    /**
     * 判断给定 response_type 是否为唯一请求类型。
     *
     * @param responseType 响应类型
     * @return 列表仅含该类型时 true
     */
    public boolean hasSingleResponseType(String responseType) {
        if (responseTypes.size() > 1) {
            return false;
        }
        return responseTypes.contains(responseType);
    }


    /** @return 是否含 token 或 id_token（隐式/混合 flow） */
    public boolean isImplicitOrHybridFlow() {
        return hasResponseType(TOKEN) || hasResponseType(ID_TOKEN);
    }

    /** @return 是否为纯隐式 flow（含 token/id_token 但不含 code） */
    public boolean isImplicitFlow() {
        return (hasResponseType(TOKEN) || hasResponseType(ID_TOKEN)) && !hasResponseType(CODE);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String responseType : responseTypes) {
            if (!first) {
                builder.append(" ");
            } else {
                first = false;
            }
            builder.append(responseType);
        }
        return builder.toString();
    }
}
