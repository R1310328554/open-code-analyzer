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

package org.keycloak.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Keycloak 通用 JSON 序列化/反序列化工具类。
 *
 * <p>
 * 提供预配置的 {@link ObjectMapper} 实例及常用的读写便捷方法。
 * </p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JsonSerialization {
    /** 默认 ObjectMapper（含 Jdk8、JavaTime 模块，忽略 null 属性）。 */
    public static final ObjectMapper mapper = createObjectMapperWithDefaults();
    /** 带缩进输出的 ObjectMapper。 */
    public static final ObjectMapper prettyMapper = createPrettyObjectMapperWithDefaults();

    /** 创建带默认配置的 ObjectMapper。 */
    public static ObjectMapper createObjectMapperWithDefaults() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    /** 创建带缩进输出配置的 ObjectMapper。 */
    public static ObjectMapper createPrettyObjectMapperWithDefaults() {
        ObjectMapper prettyMapper = new ObjectMapper();
        prettyMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        prettyMapper.enable(SerializationFeature.INDENT_OUTPUT);
        prettyMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        return prettyMapper;
    }

    /** 将对象序列化为 JSON 字符串；失败时包装为 IllegalArgumentException。 */
    public static String valueAsString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 将对象序列化为带缩进的 JSON 字符串。 */
    public static String valueAsPrettyString(Object obj) {
        try {
            return prettyMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 从 JSON 字符串反序列化为指定类型。 */
    public static <T> T valueFromString(String string, Class<T> type) {
        try {
            return mapper.readValue(string, type);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 将对象写入输出流。 */
    public static void writeValueToStream(OutputStream os, Object obj) throws IOException {
        mapper.writeValue(os, obj);
    }

    /** 以缩进格式将对象写入输出流。 */
    public static void writeValuePrettyToStream(OutputStream os, Object obj) throws IOException {
        prettyMapper.writeValue(os, obj);
    }

    public static String writeValueAsPrettyString(Object obj) throws IOException {
        return prettyMapper.writeValueAsString(obj);
    }
    public static String writeValueAsString(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    public static byte[] writeValueAsBytes(Object obj) throws IOException {
        return mapper.writeValueAsBytes(obj);
    }

    /** 将对象转换为 {@link JsonNode} 树结构。 */
    public static JsonNode writeValueAsNode(Object obj) {
        return mapper.valueToTree(obj);
    }

    public static <T> T readValue(byte[] bytes, Class<T> type) throws IOException {
        return mapper.readValue(bytes, type);
    }

    public static <T> T readValue(String bytes, Class<T> type) throws IOException {
        return mapper.readValue(bytes, type);
    }

    public static <T> T readValue(InputStream bytes, Class<T> type) throws IOException {
        return mapper.readValue(bytes, type);
    }

    public static <T> T readValue(byte[] bytes, TypeReference<T> type) throws IOException {
        return mapper.readValue(bytes, type);
    }

    public static <T> T readValue(String string, TypeReference<T> type) throws IOException {
        return mapper.readValue(string, type);
    }

    public static <T> T readValue(InputStream bytes, TypeReference<T> type) throws IOException {
        return mapper.readValue(bytes, type);
    }

    /**
     * 根据给定 POJO 创建 {@link ObjectNode}，将其全部属性复制到新节点。
     *
     * @param pojo 属性将被复制到结果 {@link ObjectNode} 的 POJO 对象
     * @return 包含 POJO 全部属性的 {@link ObjectNode}
     * @throws IOException 若无法创建结果节点
     */
    public static ObjectNode createObjectNode(Object pojo) throws IOException {
        if (pojo == null) {
            throw new IllegalArgumentException("Pojo can not be null.");
        }

        ObjectNode objectNode = createObjectNode();
        JsonNode jsonNode;
        try (JsonParser jsonParser = mapper.getFactory().createParser(writeValueAsBytes(pojo))) {
            jsonNode = jsonParser.readValueAsTree();
        }

        if (!jsonNode.isObject()) {
            throw new RuntimeException("JsonNode [" + jsonNode + "] is not a object.");
        }

        objectNode.setAll((ObjectNode) jsonNode);

        return objectNode;
    }

    /** 创建空的 {@link ObjectNode}。 */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

}
