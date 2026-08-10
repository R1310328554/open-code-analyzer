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
package org.keycloak.client.admin.cli;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.keycloak.client.cli.common.AttributeKey;
import org.keycloak.client.cli.common.AttributeOperation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import static org.keycloak.client.cli.common.AttributeOperation.Type.SET;
import static org.keycloak.client.cli.util.OutputUtil.MAPPER;

/**
 * JSON 文档属性读写与深度合并工具类。
 * <p>
 * 支持通过点分路径、数组索引及 {@code +=} 追加语法，
 * 为 kcadm 命令的 {@code --set}/{@code --delete} 选项提供底层实现。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ReflectionUtil {

    /**
     * 在 JSON 节点上批量应用属性操作（设置或删除）。
     *
     * @param client 目标 JSON 根节点
     * @param attrs 属性操作列表
     */
    public static void setAttributes(JsonNode client, List<AttributeOperation> attrs) {
        for (AttributeOperation item: attrs) {
            AttributeKey attr = item.getKey();
            JsonNode nested = client;

            List<AttributeKey.Component> cs = attr.getComponents();
            for (int i = 0; i < cs.size(); i++) {
                AttributeKey.Component c = cs.get(i);

                // 若为属性名的最后一个分量：
                //   SET 时在 nested 上赋值（已存在则覆盖，数组则追加或按索引更新）
                //   DELETE 时删除字段或从数组中移除元素
                // 否则向下遍历子节点，不存在则按需创建空对象或数组
                if (i == cs.size() - 1) {
                    String val = item.getValue();
                    ObjectNode obj = (ObjectNode) nested;

                    if (SET == item.getType()) {
                        JsonNode valNode = valueToJsonNode(val);
                        if (c.isArray() || attr.isAppend()) {
                            JsonNode list = obj.get(c.getName());
                            // 期望子节点为数组类型
                            if ( ! (list instanceof ArrayNode)) {
                                // 替换为新数组
                                list = MAPPER.createArrayNode();
                                obj.set(c.getName(), list);
                            }
                            setArrayItem((ArrayNode) list, c.getIndex(), valNode);
                        } else {
                            ((ObjectNode) nested).set(c.getName(), valNode);
                        }
                    } else {
                        // 操作类型为 DELETE
                        if (c.isArray()) {
                            JsonNode list = obj.get(c.getName());
                            // 期望子节点为数组类型
                            if (list instanceof ArrayNode) {
                                removeArrayItem((ArrayNode) list, c.getIndex());
                            }
                        } else {
                            obj.remove(c.getName());
                        }
                    }
                } else {
                    // 获取或创建中间子节点
                    JsonNode node = nested.get(c.getName());
                    if (node == null) {
                        if (c.isArray()) {
                            node = MAPPER.createArrayNode();
                        } else {
                            node = MAPPER.createObjectNode();
                        }
                        ((ObjectNode) nested).set(c.getName(), node);
                    }
                    nested = node;
                }
            }
        }
    }

    /** 在数组指定索引处设置元素，索引为 -1 时追加到末尾。 */
    private static void setArrayItem(ArrayNode list, int index, JsonNode valNode) {
        if (index == -1) {
            // 追加到数组末尾
            list.add(valNode);
            return;
        }
        // 确保 index 之前的槽位均已填充
        for (int i = list.size(); i < index+1; i++) {
            list.add(NullNode.instance);
        }
        list.set(index, valNode);
    }

    /** 从数组中移除指定索引的元素。 */
    private static void removeArrayItem(ArrayNode list, int index) {
        if (index == -1) {
            throw new IllegalArgumentException("Internal error - should never be called with index == -1");
        }
        list.remove(index);
    }

    /** 将字符串值转换为 {@link JsonNode}，优先按 JSON 解析，失败则作为文本节点。 */
    static JsonNode valueToJsonNode(String val) {
        // 尝试将值解析为 JSON
        try {
            return MAPPER.readTree(val);
        } catch (Exception ignored) {
        }

        // 兼容旧行为：处理带引号的字符串（后续版本可能移除）
        if (isQuoted(val)) {
            return TextNode.valueOf(unquote(val));
        }

        return TextNode.valueOf(val);
    }
    
    private static boolean isQuoted(String val) {
        return val.startsWith("'") || val.startsWith("\"");
    }

    private static String unquote(String val) {
        if (!(val.startsWith("'") || val.startsWith("\"")) || !(val.endsWith("'") || val.endsWith("\""))) {
            throw new RuntimeException("Invalid string value: " + val);
        }
        return val.substring(1, val.length()-1);
    }

    /**
     * 将源 JSON 对象递归合并到目标对象中。
     * <p>
     * 同名字段若双方均为对象则深度合并；类型不兼容时抛出异常。
     *
     * @param source 源 JSON 对象
     * @param dest 目标 JSON 对象
     */
    public static void merge(JsonNode source, ObjectNode dest) {
        // 遍历源对象：目标已存在则递归合并，否则直接复制

        if (!source.isObject()) {
            throw new RuntimeException("Not a JSON object: " + source);
        }

        Iterator<Map.Entry<String, JsonNode>> it = ((ObjectNode) source).fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> item = it.next();
            String name = item.getKey();
            JsonNode node = item.getValue();

            JsonNode destNode = dest.get(name);
            if (destNode != null) {
                if (destNode.isObject()) {
                    if (node.isObject()) {
                        merge(node, (ObjectNode) destNode);
                    } else {
                        throw new RuntimeException("Attribute is of incompatible type - " + name + ": " + node);
                    }
                } else if (destNode.isArray()) {
                    if (node.isArray()) {
                        dest.set(name, node);
                    } else {
                        throw new RuntimeException("Attribute is of incompatible type - " + name + ": " + node);
                    }
                } else {
                    dest.set(name, node);
                }
            } else {
                dest.set(name, node);
            }
        }
    }
}
