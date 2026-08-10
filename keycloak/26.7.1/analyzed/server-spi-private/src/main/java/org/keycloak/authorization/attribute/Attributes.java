/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.attribute;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 属性容器：持有属性名与多值集合，并提供查询与类型转换工具。
 * <p>未来可对接不同策略信息点（PIP）实现。</p>
 *
 * <p>Holds attributes, their values and provides utility methods to manage them.
 *
 * <p>In the future, it may be useful to provide different implementations for this interface in order to plug or integrate with different
 * Policy Information Point (PIP).</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Attributes {

    /** 由 Map 构造 {@link Attributes} 实例。 */
    static Attributes from(Map<String, Collection<String>> attributes) {
        return () -> attributes;
    }

    /**
     * 转换为 {@link Map} 表示。
     *
     * Converts to a {@link Map}.
     *
     * @return
     */
    Map<String, Collection<String>> toMap();

    /**
     * 是否存在指定名称的属性。
     *
     * Checks if there is an attribute with the given <code>name</code>.
     *
     * @param name the attribute name
     * @return true if any attribute with <code>name</code> exist. Otherwise, returns false.
     */
    default boolean exists(String name) {
        return toMap().containsKey(name);
    }

    /**
     * 是否存在指定名称且包含给定值的属性。
     *
     * Checks if there is an attribute with the given <code>name</code> and <code>value</code>.
     *
     * @param name the attribute name
     * @param value the attribute value
     * @return true if any attribute with <code>name</code> and <code>value</code> exist. Otherwise, returns false.
     */
    default boolean containsValue(String name, String value) {
        Collection<String> values = toMap().get(name);
        return values != null && values.stream().anyMatch(value::equals);
    }

    /**
     * 返回可解析多值的 {@link Entry}。
     *
     * Returns a {@link Entry} from where values can be obtained and parsed accordingly.
     *
     * @param name the attribute name
     * @return an {@link Entry} holding the values for an attribute
     */
    default Entry getValue(String name) {
        Collection<String> value = toMap().get(name);

        if (value != null) {
            return new Entry(name, value);
        }

        return null;
    }

    /**
     * 单个属性及其多值条目，提供字符串/数值/日期等解析方法，便于编写规则型策略。
     *
     * Holds an attribute and its values, providing useful methods for obtaining and formatting values. Specially useful
     * for writing rule-based policies.
     */
    class Entry {

        private final String[] values;
        private final String name;

        public Entry(String name, Collection<String> values) {
            this.name = name;
            this.values = values.toArray(new String[values.size()]);
        }

        private String getName() {
            return this.name;
        }

        /** 属性值个数。 */
        public int size() {
            return values.length;
        }

        /** 是否无属性值。 */
        public boolean isEmpty() {
            return values.length == 0;
        }

        /** 按索引读取字符串值。 */
        public String asString(int idx) {
            if (idx >= values.length) {
                throw new IllegalArgumentException("Invalid index [" + idx + "]. Values are [" + values + "].");
            }

            return values[idx];
        }

        /** 按索引解析整型值。 */
        public int asInt(int idx) {
            return Integer.parseInt(asString(idx));
        }

        /** 按指定格式解析日期值。 */
        public Date asDate(int idx, String pattern) {
            try {
                return new SimpleDateFormat(pattern).parse(asString(idx));
            } catch (ParseException e) {
                throw new RuntimeException("Error parsing date.", e);
            }
        }

        /** 按索引解析 IP 地址。 */
        public InetAddress asInetAddress(int idx) {
            try {
                return InetAddress.getByName(asString(idx));
            } catch (UnknownHostException e) {
                throw new RuntimeException("Error parsing address.", e);
            }
        }

        /** 按索引解析长整型值。 */
        public long asLong(int idx) {
            return Long.parseLong(asString(idx));
        }

        /** 按索引解析双精度浮点值。 */
        public double asDouble(int idx) {
            return Double.parseDouble(asString(idx));
        }
    }
}
