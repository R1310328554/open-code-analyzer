/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * Builder for immutable {@link DomainNameMapping} instances.
 *
 * @param <V> concrete type of value objects
 * @deprecated Use {@link DomainWildcardMappingBuilder}
 * <p>构建不可变 {@link DomainNameMapping}；已废弃，推荐 {@link DomainWildcardMappingBuilder}。</p>
 */
@Deprecated
public final class DomainNameMappingBuilder<V> {

    private final V defaultValue;
    private final Map<String, V> map;

    /**
     * Constructor with default initial capacity of the map holding the mappings
     *
     * @param defaultValue the default value for {@link DomainNameMapping#map(String)} to return
     *                     when nothing matches the input
     * <p>默认容量 4，指定无匹配时的默认返回值。</p>
     */
    public DomainNameMappingBuilder(V defaultValue) {
        this(4, defaultValue);
    }

    /**
     * Constructor with initial capacity of the map holding the mappings
     *
     * @param initialCapacity initial capacity for the internal map
     * @param defaultValue    the default value for {@link DomainNameMapping#map(String)} to return
     *                        when nothing matches the input
     * <p>指定内部 LinkedHashMap 初始容量与默认值。</p>
     */
    public DomainNameMappingBuilder(int initialCapacity, V defaultValue) {
        this.defaultValue = checkNotNull(defaultValue, "defaultValue");
        map = new LinkedHashMap<String, V>(initialCapacity);
    }

    /**
     * Adds a mapping that maps the specified (optionally wildcard) host name to the specified output value.
     * Null values are forbidden for both hostnames and values.
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Wildcard_DNS_record">DNS wildcard</a> is supported as hostname.
     * For example, you can use {@code *.netty.io} to match {@code netty.io} and {@code downloads.netty.io}.
     * </p>
     *
     * @param hostname the host name (optionally wildcard)
     * @param output   the output value that will be returned by {@link DomainNameMapping#map(String)}
     *                 when the specified host name matches the specified input host name
     * <p>添加主机名到值的映射，支持 DNS 通配符模式。</p>
     */
    public DomainNameMappingBuilder<V> add(String hostname, V output) {
        map.put(checkNotNull(hostname, "hostname"), checkNotNull(output, "output"));
        return this;
    }

    /**
     * Creates a new instance of immutable {@link DomainNameMapping}
     * Attempts to add new mappings to the result object will cause {@link UnsupportedOperationException} to be thrown
     *
     * @return new {@link DomainNameMapping} instance
     * <p>生成不可变映射实例，内部使用数组存储以优化 {@link #map(String)} 性能。</p>
     */
    public DomainNameMapping<V> build() {
        return new ImmutableDomainNameMapping<V>(defaultValue, map);
    }

    /**
     * Immutable mapping from domain name pattern to its associated value object.
     * Mapping is represented by two arrays: keys and values. Key domainNamePatterns[i] is associated with values[i].
     *
     * @param <V> concrete type of value objects
     * <p>不可变域名映射：用平行数组 {@code domainNamePatterns} 与 {@code values} 存储规则。</p>
     */
    private static final class ImmutableDomainNameMapping<V> extends DomainNameMapping<V> {
        private static final String REPR_HEADER = "ImmutableDomainNameMapping(default: ";
        private static final String REPR_MAP_OPENING = ", map: {";
        private static final String REPR_MAP_CLOSING = "})";
        private static final int REPR_CONST_PART_LENGTH =
            REPR_HEADER.length() + REPR_MAP_OPENING.length() + REPR_MAP_CLOSING.length();

        /** 规范化后的域名模式数组。 */
        private final String[] domainNamePatterns;
        /** 与模式一一对应的值数组。 */
        private final V[] values;
        private final Map<String, V> map;

        @SuppressWarnings("unchecked")
        private ImmutableDomainNameMapping(V defaultValue, Map<String, V> map) {
            super(null, defaultValue);

            Set<Map.Entry<String, V>> mappings = map.entrySet();
            int numberOfMappings = mappings.size();
            domainNamePatterns = new String[numberOfMappings];
            values = (V[]) new Object[numberOfMappings];

            final Map<String, V> mapCopy = new LinkedHashMap<String, V>(map.size());
            int index = 0;
            for (Map.Entry<String, V> mapping : mappings) {
                final String hostname = normalizeHostname(mapping.getKey());
                final V value = mapping.getValue();
                domainNamePatterns[index] = hostname;
                values[index] = value;
                mapCopy.put(hostname, value);
                ++index;
            }

            this.map = Collections.unmodifiableMap(mapCopy);
        }

        @Override
        @Deprecated
        public DomainNameMapping<V> add(String hostname, V output) {
            throw new UnsupportedOperationException(
                "Immutable DomainNameMapping does not support modification after initial creation");
        }

        @Override
        public V map(String hostname) {
            if (hostname != null) {
                hostname = normalizeHostname(hostname);

                int length = domainNamePatterns.length;
                for (int index = 0; index < length; ++index) {
                    if (matches(domainNamePatterns[index], hostname)) {
                        return values[index];
                    }
                }
            }

            return defaultValue;
        }

        @Override
        public Map<String, V> asMap() {
            return map;
        }

        @Override
        public String toString() {
            String defaultValueStr = defaultValue.toString();

            int numberOfMappings = domainNamePatterns.length;
            if (numberOfMappings == 0) {
                return REPR_HEADER + defaultValueStr + REPR_MAP_OPENING + REPR_MAP_CLOSING;
            }

            String pattern0 = domainNamePatterns[0];
            String value0 = values[0].toString();
            int oneMappingLength = pattern0.length() + value0.length() + 3; // 2 for separator ", " and 1 for '='
            int estimatedBufferSize = estimateBufferSize(defaultValueStr.length(), numberOfMappings, oneMappingLength);

            StringBuilder sb = new StringBuilder(estimatedBufferSize)
                .append(REPR_HEADER).append(defaultValueStr).append(REPR_MAP_OPENING);

            appendMapping(sb, pattern0, value0);
            for (int index = 1; index < numberOfMappings; ++index) {
                sb.append(", ");
                appendMapping(sb, index);
            }

            return sb.append(REPR_MAP_CLOSING).toString();
        }

        /**
         * Estimates the length of string representation of the given instance:
         * est = lengthOfConstantComponents + defaultValueLength + (estimatedMappingLength * numOfMappings) * 1.10
         *
         * @param defaultValueLength     length of string representation of {@link #defaultValue}
         * @param numberOfMappings       number of mappings the given instance holds,
         *                               e.g. {@link #domainNamePatterns#length}
         * @param estimatedMappingLength estimated size taken by one mapping
         * @return estimated length of string returned by {@link #toString()}
         * <p>预估 {@link #toString()} 所需缓冲区大小，避免多次扩容。</p>
         */
        private static int estimateBufferSize(int defaultValueLength,
                                              int numberOfMappings,
                                              int estimatedMappingLength) {
            return REPR_CONST_PART_LENGTH + defaultValueLength
                + (int) (estimatedMappingLength * numberOfMappings * 1.10);
        }

        private StringBuilder appendMapping(StringBuilder sb, int mappingIndex) {
            return appendMapping(sb, domainNamePatterns[mappingIndex], values[mappingIndex].toString());
        }

        private static StringBuilder appendMapping(StringBuilder sb, String domainNamePattern, String value) {
            return sb.append(domainNamePattern).append('=').append(value);
        }
    }
}
