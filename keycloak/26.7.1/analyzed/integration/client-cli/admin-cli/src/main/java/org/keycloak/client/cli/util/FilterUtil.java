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
package org.keycloak.client.cli.util;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.client.cli.util.OutputUtil.MAPPER;
import static org.keycloak.client.cli.util.OutputUtil.convertToJsonNode;

/**
 * 按 {@link ReturnFields} 定义递归过滤 JSON 对象/数组的工具。
 * <p>
 * 用于 CLI {@code --fields} 输出裁剪，仅保留请求返回的字段子集。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class FilterUtil {

    /**
     * 深拷贝并过滤对象，仅保留 {@code returnFields} 允许的字段。
     * <p>
     * 数组元素逐条递归过滤；对象按字段名匹配后递归处理子字段。
     *
     * @param object 源对象（POJO 或 {@link JsonNode}）
     * @param returnFields 字段白名单描述
     * @return 过滤后的 {@link JsonNode}
     * @throws IOException JSON 转换失败时抛出
     */
    public static JsonNode copyFilteredObject(Object object, ReturnFields returnFields) throws IOException {

        JsonNode node = convertToJsonNode(object);

        JsonNode r = node;
        if (node.isArray()) {
            ArrayNode ar = MAPPER.createArrayNode();
            for (JsonNode item: node) {
                ar.add(copyFilteredObject(item, returnFields));
            }
            r = ar;

        } else if (node.isObject()){
            r = MAPPER.createObjectNode();
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                if (returnFields.included(name)) {
                    JsonNode value = copyFilteredObject(node.get(name), returnFields.child(name));
                    ((ObjectNode) r).set(name, value);
                }
            }
        }
        return r;
    }
}
