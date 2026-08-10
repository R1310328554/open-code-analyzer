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
package org.keycloak.client.admin.cli.commands;

import java.io.IOException;

import org.keycloak.client.cli.util.FilterUtil;
import org.keycloak.client.cli.util.ReturnFields;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.keycloak.client.cli.util.HttpUtil.normalize;

/**
 * 全局选项命令的辅助接口，提供 Admin REST 根路径拼接、URI 解析及响应字段过滤等默认实现。
 * <p>
 * 由 {@code create}、{@code get}、{@code update} 等子命令通过 mixin 方式复用。
 */
public interface GlobalOptionsCmdHelper {

    /** 根据服务器基址拼接 Admin REST 根路径（{@code .../admin}）。 */
    default String composeAdminRoot(String server) {
        return normalize(server) + "admin";
    }

    /** 从资源 URI 末段提取类型名，并去掉复数后缀 {@code s}。 */
    default String extractTypeNameFromUri(String resourceUrl) {
        String type = extractLastComponentOfUri(resourceUrl);
        if (type.endsWith("s")) {
            type = type.substring(0, type.length()-1);
        }
        return type;
    }

    /** 提取 URI 路径的最后一段（忽略末尾斜杠）。 */
    default String extractLastComponentOfUri(String resourceUrl) {
        int endPos = resourceUrl.endsWith("/") ? resourceUrl.length()-2 : resourceUrl.length()-1;
        int pos = resourceUrl.lastIndexOf("/", endPos);
        pos = pos == -1 ? 0 : pos;
        return resourceUrl.substring(pos+1, endPos+1);
    }

    /**
     * 按 {@link ReturnFields} 规则过滤 JSON 响应，仅保留指定字段。
     *
     * @param mapper       Jackson 对象映射器
     * @param rootNode     原始 JSON 根节点
     * @param returnFields 字段过滤模式
     * @return 过滤后的 JSON 节点
     */
    default JsonNode applyFieldFilter(ObjectMapper mapper, JsonNode rootNode, ReturnFields returnFields) {
        // 构造满足 returnFields 过滤条件的新 JsonNode
        try {
            return FilterUtil.copyFilteredObject(rootNode, returnFields);
        } catch (IOException e) {
            throw new RuntimeException("Failed to apply fields filter", e);
        }
    }

}
