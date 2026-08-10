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
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * CLI 结果序列化与 CSV 输出工具。
 * <p>
 * 共享 Jackson {@link #MAPPER} 实例，支持对象转 {@link JsonNode} 及按
 * {@link ReturnFields} 扁平化 CSV 行输出。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class OutputUtil {

    /** 全局 JSON 映射器（缩进输出、忽略 null、尾随 token 校验）。 */
    public static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 将任意对象转为 {@link JsonNode}；已是 JsonNode 则直接返回。
     *
     * @param object 源对象
     * @return JSON 树节点
     * @throws IOException 转换失败时抛出
     */
    public static JsonNode convertToJsonNode(Object object) throws IOException {
        if (object instanceof JsonNode) {
            return (JsonNode) object;
        }

        return MAPPER.convertValue(object, JsonNode.class);
    }

    /** 以 CSV 格式打印到标准输出。 */
    public static void printAsCsv(Object object, ReturnFields fields, boolean unquoted) throws IOException {
        printAsCsv(object, fields, unquoted, IoUtil::printOut);
    }

    /**
     * 将对象序列化为 CSV 行并通过指定打印器输出。
     * <p>
     * 非数组输入自动包装为单元素数组；每行以逗号分隔扁平字段值。
     *
     * @param object 待输出对象
     * @param fields 字段白名单，{@code null} 表示输出全部字段
     * @param unquoted 文本节点是否省略 JSON 引号
     * @param printer 行输出回调
     * @throws IOException 序列化失败时抛出
     */
    public static void printAsCsv(Object object, ReturnFields fields, boolean unquoted, Consumer<String> printer) throws IOException {

        JsonNode node = convertToJsonNode(object);
        if (!node.isArray()) {
            ArrayNode listNode = MAPPER.createArrayNode();
            listNode.add(node);
            node = listNode;
        }

        for (JsonNode item: node) {
            StringBuilder buffer = new StringBuilder();
            printObjectAsCsv(buffer, item, fields, unquoted);

            printer.accept(buffer.length() > 0 ? buffer.substring(1) : "");
        }
    }

    /** 无字段过滤的 CSV 扁平化（内部使用）。 */
    static void printObjectAsCsv(StringBuilder out, JsonNode node, boolean unquoted) {
        printObjectAsCsv(out, node, null, unquoted);
    }

    /**
     * 递归将 JSON 节点扁平化为 CSV 逗号分隔片段。
     * <p>
     * 对象按字段迭代，数组逐元素递归，标量前加逗号分隔符。
     */
    static void printObjectAsCsv(StringBuilder out, JsonNode node, ReturnFields fields, boolean unquoted) {

        if (node == null) {
            out.append(",");
        } else if (node.isObject()) {
            if (fields == null) {
                Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    printObjectAsCsv(out, it.next().getValue(), unquoted);
                }
            } else {
                Iterator<String> it = fields.iterator();
                while (it.hasNext()) {
                    String field = it.next();
                    JsonNode attr = node.get(field);
                    printObjectAsCsv(out, attr, fields.child(field), unquoted);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item: node) {
                printObjectAsCsv(out, item, fields, unquoted);
            }
        } else {
            out.append(",");
            if (unquoted && node instanceof TextNode) {
                out.append(node.asText());
            } else {
                out.append(node.toString());
            }
        }
    }
}
