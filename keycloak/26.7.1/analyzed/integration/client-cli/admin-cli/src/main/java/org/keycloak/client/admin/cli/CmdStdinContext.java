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

import java.io.IOException;
import java.util.List;

import org.keycloak.client.cli.common.AttributeOperation;
import org.keycloak.client.cli.util.AttributeException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.client.admin.cli.ReflectionUtil.setAttributes;
import static org.keycloak.client.cli.util.IoUtil.readFileOrStdin;

/**
 * 命令行 stdin/文件输入的解析上下文。
 * <p>
 * 保存原始 JSON 文本及其反序列化结果，供 create/update 等命令
 * 在合并属性或构造请求体时使用。
 *
 * @param <T> 解析结果的类型，通常为 {@link JsonNode}
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class CmdStdinContext<T> {

    /** 解析后的 JSON 对象。 */
    private T result;
    /** 原始 JSON 字符串内容。 */
    private String content;

    public CmdStdinContext() {}

    /** 返回解析结果。 */
    public T getResult() {
        return result;
    }

    /** 设置解析结果。 */
    public void setResult(T result) {
        this.result = result;
    }

    /** 返回原始 JSON 文本。 */
    public String getContent() {
        return content;
    }

    /** 设置原始 JSON 文本。 */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 从文件或标准输入读取并解析 JSON 文档。
     *
     * @param file 文件路径，{@code "-"} 表示从 stdin 读取
     * @return 包含解析结果与原始内容的上下文
     */
    public static CmdStdinContext<JsonNode> parseFileOrStdin(String file) {
    
        String content = readFileOrStdin(file).trim();
        JsonNode result = null;
    
        if (content.length() == 0) {
            throw new RuntimeException("Document provided by --file option is empty");
        }
    
        try {
            result = JsonSerialization.readValue(content, JsonNode.class);
        } catch (JsonParseException e) {
            throw new RuntimeException("Not a valid JSON document - " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the input document as JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Not a valid JSON document", e);
        }
    
        CmdStdinContext<JsonNode> ctx = new CmdStdinContext<>();
        ctx.setContent(content);
        ctx.setResult(result);
        return ctx;
    }

    /**
     * 将属性操作（{@code --set}/{@code --delete}）合并到现有 JSON 对象中。
     *
     * @param ctx 当前上下文
     * @param newObject 当 ctx 无结果时使用的初始对象，可为 null
     * @param attrs 待应用的属性操作列表
     * @return 合并后的上下文
     */
    public static <T> CmdStdinContext<JsonNode> mergeAttributes(CmdStdinContext<JsonNode> ctx, ObjectNode newObject, List<AttributeOperation> attrs) {
    
        JsonNode node = ctx.getResult();
        if (node != null && !node.isObject()) {
            throw new RuntimeException("Not a JSON object: " + node);
        }
        ObjectNode result = (ObjectNode) node;
        try {
    
            if (result == null) {
                result = newObject;
            }
    
            if (result == null) {
                throw new RuntimeException("Failed to set attribute(s) - no target object");
            }
    
            try {
                setAttributes(result, attrs);
            } catch (AttributeException e) {
                throw new RuntimeException("Failed to set attribute '" + e.getAttributeName() + "' on document type '" + result.getClass().getName() + "'", e);
            }
            ctx.setContent(JsonSerialization.writeValueAsString(result));
    
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge attributes with configuration from file", e);
        }
    
        ctx.setResult(result);
        return ctx;
    }
}
