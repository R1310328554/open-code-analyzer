/*
 * Copyright 2020 The Netty Project
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

import java.util.LinkedHashMap;
import java.util.Map;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * Builder that allows to build {@link Mapping}s that support
 * <a href="https://tools.ietf.org/search/rfc6125#section-6.4">DNS wildcard</a> matching.
 * @param <V> the type of the value that we map to.
 * <p>构建支持 RFC 6125 DNS 通配符匹配的不可变 {@link Mapping}；
 * 通配符仅匹配最左侧标签且仅一层子域（如 {@code *.netty.io} 匹配 {@code xyz.netty.io}，不匹配 {@code abc.xyz.netty.io}）。</p>
 */
public class DomainWildcardMappingBuilder<V> {

    private final V defaultValue;
    private final Map<String, V> map;

    /**
     * Constructor with default initial capacity of the map holding the mappings
     *
     * @param defaultValue the default value for {@link Mapping#map(Object)} )} to return
     *                     when nothing matches the input
     * <p>默认容量创建构建器，指定无匹配时的默认返回值。</p>
     */
    public DomainWildcardMappingBuilder(V defaultValue) {
        this(4, defaultValue);
    }

    /**
     * Constructor with initial capacity of the map holding the mappings
     *
     * @param initialCapacity initial capacity for the internal map
     * @param defaultValue    the default value for {@link Mapping#map(Object)} to return
     *                        when nothing matches the input
     * <p>指定内部映射初始容量与默认值。</p>
     */
    public DomainWildcardMappingBuilder(int initialCapacity, V defaultValue) {
        this.defaultValue = checkNotNull(defaultValue, "defaultValue");
        map = new LinkedHashMap<String, V>(initialCapacity);
    }

    /**
     * Adds a mapping that maps the specified (optionally wildcard) host name to the specified output value.
     * {@code null} values are forbidden for both hostnames and values.
     * <p>
     * <a href="https://tools.ietf.org/search/rfc6125#section-6.4">DNS wildcard</a> is supported as hostname. The
     * wildcard will only match one sub-domain deep and only when wildcard is used as the most-left label.
     *
     * For example:
     *
     * <p>
     *  *.netty.io will match xyz.netty.io but NOT abc.xyz.netty.io
     * </p>
     *
     * @param hostname the host name (optionally wildcard)
     * @param output   the output value that will be returned by {@link Mapping#map(Object)}
     *                 when the specified host name matches the specified input host name
     * <p>添加主机名映射；通配符 {@code *.domain} 在内部存为 {@code .domain} 以便后缀匹配。</p>
     */
    public DomainWildcardMappingBuilder<V> add(String hostname, V output) {
        map.put(normalizeHostName(hostname),
                checkNotNull(output, "output"));
        return this;
    }

    /** 校验并规范化主机名：通配符转为 {@code .suffix} 形式。 */
    private String normalizeHostName(String hostname) {
        checkNotNull(hostname, "hostname");
        if (hostname.isEmpty() || hostname.charAt(0) == '.') {
            throw new IllegalArgumentException("Hostname '" + hostname + "' not valid");
        }
        hostname = ImmutableDomainWildcardMapping.normalize(checkNotNull(hostname, "hostname"));
        if (hostname.charAt(0) == '*') {
            if (hostname.length() < 3 || hostname.charAt(1) != '.') {
                throw new IllegalArgumentException("Wildcard Hostname '" + hostname + "'not valid");
            }
            return hostname.substring(1);
        }
        return hostname;
    }
    /**
     * Creates a new instance of an immutable {@link Mapping}.
     *
     * @return new {@link Mapping} instance
     * <p>构建不可变 {@link Mapping} 实例。</p>
     */
    public Mapping<String, V> build() {
        return new ImmutableDomainWildcardMapping<V>(defaultValue, map);
    }

    /** 不可变 RFC 6125 风格通配符映射实现。 */
    private static final class ImmutableDomainWildcardMapping<V> implements Mapping<String, V> {
        private static final String REPR_HEADER = "ImmutableDomainWildcardMapping(default: ";
        private static final String REPR_MAP_OPENING = ", map: ";
        private static final String REPR_MAP_CLOSING = ")";

        private final V defaultValue;
        private final Map<String, V> map;

        ImmutableDomainWildcardMapping(V defaultValue, Map<String, V> map) {
            this.defaultValue = defaultValue;
            this.map = new LinkedHashMap<String, V>(map);
        }

        @Override
        public V map(String hostname) {
            if (hostname != null) {
                hostname = normalize(hostname);

                // Let's try an exact match first
                // 先精确匹配完整主机名
                V value = map.get(hostname);
                if (value != null) {
                    return value;
                }

                // No exact match, let's try a wildcard match.
                // 再尝试通配符：取第一个 '.' 之后的后缀（如 xyz.netty.io -> .netty.io）
                int idx = hostname.indexOf('.');
                if (idx != -1) {
                    value = map.get(hostname.substring(idx));
                    if (value != null) {
                        return value;
                    }
                }
            }

            return defaultValue;
        }

        @SuppressWarnings("deprecation")
        static String normalize(String hostname) {
            return DomainNameMapping.normalizeHostname(hostname);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(REPR_HEADER).append(defaultValue).append(REPR_MAP_OPENING).append('{');

            for (Map.Entry<String, V> entry : map.entrySet()) {
                String hostname = entry.getKey();
                if (hostname.charAt(0) == '.') {
                    hostname = '*' + hostname;
                }
                sb.append(hostname).append('=').append(entry.getValue()).append(", ");
            }
            sb.setLength(sb.length() - 2);
            return sb.append('}').append(REPR_MAP_CLOSING).toString();
        }
    }
}
