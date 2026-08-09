/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.attribute;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Topic 属性修改串解析器：支持 {@code +key=value} 增改、{@code -key} 删除，
 * 键值对以逗号分隔，例如 {@code +key1=value1,+key2=value2,-key3}。
 */
public class AttributeParser {

    /** 属性键值对之间的分隔符。 */
    public static final String ATTR_ARRAY_SEPARATOR_COMMA = ",";

    /** 键与值之间的等号分隔符。 */
    public static final String ATTR_KEY_VALUE_EQUAL_SIGN = "=";

    /** 新增或修改属性时的前缀符号。 */
    public static final String ATTR_ADD_PLUS_SIGN = "+";

    /** 删除属性时的前缀符号。 */
    private static final String ATTR_DELETE_MINUS_SIGN = "-";

    /**
     * 将属性修改串解析为键值 Map。
     *
     * @param attributesModification 属性修改串，空串返回空 Map
     * @return 解析后的属性 Map
     */
    public static Map<String, String> parseToMap(String attributesModification) {
        if (Strings.isNullOrEmpty(attributesModification)) {
            return new HashMap<>();
        }

        // 格式：+key1=value1,+key2=value2,-key3,+key4=value4
        Map<String, String> attributes = new HashMap<>();
        String[] kvs = attributesModification.split(ATTR_ARRAY_SEPARATOR_COMMA);
        for (String kv : kvs) {
            String key;
            String value;
            if (kv.contains(ATTR_KEY_VALUE_EQUAL_SIGN)) {
                String[] splits = kv.split(ATTR_KEY_VALUE_EQUAL_SIGN);
                key = splits[0];
                value = splits[1];
                if (!key.contains(ATTR_ADD_PLUS_SIGN)) {
                    throw new RuntimeException("add/alter attribute format is wrong: " + key);
                }
            } else {
                key = kv;
                value = "";
                if (!key.contains(ATTR_DELETE_MINUS_SIGN)) {
                    throw new RuntimeException("delete attribute format is wrong: " + key);
                }
            }
            String old = attributes.put(key, value);
            if (old != null) {
                throw new RuntimeException("key duplication: " + key);
            }
        }
        return attributes;
    }

    /**
     * 将属性 Map 序列化为修改串。
     *
     * @param attributes 属性 Map，null 或空 Map 返回空串
     * @return 逗号分隔的属性修改串
     */
    public static String parseToString(Map<String, String> attributes) {
        if (attributes == null || attributes.size() == 0) {
            return "";
        }

        List<String> kvs = new ArrayList<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {

            String value = entry.getValue();
            if (Strings.isNullOrEmpty(value)) {
                kvs.add(entry.getKey());
            } else {
                kvs.add(entry.getKey() + ATTR_KEY_VALUE_EQUAL_SIGN + entry.getValue());
            }
        }
        return String.join(ATTR_ARRAY_SEPARATOR_COMMA, kvs);
    }
}
