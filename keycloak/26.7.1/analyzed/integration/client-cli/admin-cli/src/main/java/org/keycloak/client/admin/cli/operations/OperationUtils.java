/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.client.admin.cli.operations;

import java.util.List;
import java.util.function.Supplier;

import org.keycloak.client.cli.util.HttpUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.common.util.ObjectUtil.capitalize;

/**
 * Admin REST 资源查询的通用工具类。
 * <p>
 * 封装按属性搜索资源并返回 ID 或其他字段值的逻辑，供 Client/User/Group/Role 等 Operations 复用。
 */
public class OperationUtils {

    /** 默认分页查询参数（最多返回 2 条以检测歧义）。 */
    private static final String[] DEFAULT_QUERY_PARAMS = { "first", "0", "max", "2" };

    /** 按属性值搜索资源并返回其 {@code id} 字段。 */
    public static String getIdForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue, String inputAttrName) {

        return getAttrForType(rootUrl, realm, auth, resourceEndpoint, attrName, attrValue, inputAttrName, "id", null);
    }

    /** 按属性值搜索资源并返回 {@code id}，支持自定义查询参数。 */
    public static String getIdForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue, String inputAttrName, Supplier<String[]> endpointParams) {
        return getAttrForType(rootUrl, realm, auth, resourceEndpoint, attrName, attrValue, inputAttrName, "id", endpointParams);
    }

    /** 按属性值搜索资源并返回指定属性字段。 */
    public static String getAttrForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue, String inputAttrName, String returnAttrName) {
        return getAttrForType(rootUrl, realm, auth, resourceEndpoint, attrName, attrValue, inputAttrName, returnAttrName, null);
    }

    /**
     * 通用资源属性查询：发起 GET 请求后在结果中精确匹配并提取指定字段。
     *
     * @param rootUrl           Admin REST 根 URL
     * @param realm             目标领域
     * @param auth              Bearer 令牌
     * @param resourceEndpoint  资源端点路径（如 {@code users}、{@code clients}）
     * @param attrName          查询参数名
     * @param attrValue         查询参数值
     * @param inputAttrName     结果 JSON 中用于精确匹配的属性名
     * @param returnAttrName    需返回的属性名（通常为 {@code id}）
     * @param endpointParams    额外查询参数供应器；null 时使用默认分页
     * @return 匹配资源的指定属性值
     */
    public static String getAttrForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue, String inputAttrName, String returnAttrName, Supplier<String[]> endpointParams) {
        String resourceUrl = HttpUtil.composeResourceUrl(rootUrl, realm, resourceEndpoint);
        String[] defaultParams;

        if (endpointParams == null) {
            defaultParams = DEFAULT_QUERY_PARAMS;
        } else {
            defaultParams = endpointParams.get();
        }

        resourceUrl = HttpUtil.addQueryParamsToUri(resourceUrl, attrName, attrValue);
        resourceUrl = HttpUtil.addQueryParamsToUri(resourceUrl, defaultParams);

        List<ObjectNode> results = HttpUtil.doGetJSON(RoleOperations.LIST_OF_NODES.class, resourceUrl, auth);

        ObjectNode match;
        try {
            match = new LocalSearch(results).exactMatchOne(attrValue, inputAttrName);
        } catch (Exception e) {
            throw new RuntimeException("Multiple " + resourceEndpoint + " found for " + inputAttrName + ": " + attrValue, e);
        }

        String typeName = HttpUtil.singularize(resourceEndpoint);
        if (match == null) {
            if (results.size() > 1) {
                throw new RuntimeException("Some matches, but not an exact match, found for " + capitalize(typeName) + " with " + inputAttrName + ": " + attrValue + ". Try using a more unique search, such as an id.");
            }
            throw new RuntimeException(capitalize(typeName) + " not found for " + inputAttrName + ": " + attrValue);
        }

        JsonNode attr = match.get(returnAttrName);
        if (attr == null) {
            throw new RuntimeException("Returned " + typeName + " info has no '" + returnAttrName + "' attribute");
        }
        return attr.asText();
    }

}
