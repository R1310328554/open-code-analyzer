/*
 * Copyright 2014 The Netty Project
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

import io.netty.util.internal.StringUtil;

import java.net.IDN;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.StringUtil.commonSuffixOfLength;

/**
 * Maps a domain name to its associated value object.
 * <p>
 * DNS wildcard is supported as hostname, so you can use {@code *.netty.io} to match both {@code netty.io}
 * and {@code downloads.netty.io}.
 * </p>
 * @deprecated Use {@link DomainWildcardMappingBuilder}}
 * <p>域名到值的映射，支持 DNS 通配符；已废弃，请改用 {@link DomainWildcardMappingBuilder}。</p>
 */
@Deprecated
public class DomainNameMapping<V> implements Mapping<String, V> {

    final V defaultValue;
    private final Map<String, V> map;
    private final Map<String, V> unmodifiableMap;

    /**
     * Creates a default, order-sensitive mapping. If your hostnames are in conflict, the mapping
     * will choose the one you add first.
     *
     * @param defaultValue the default value for {@link #map(String)} to return when nothing matches the input
     * @deprecated use {@link DomainNameMappingBuilder} to create and fill the mapping instead
     * <p>默认容量创建有序映射；冲突时以先添加的规则为准。</p>
     */
    @Deprecated
    public DomainNameMapping(V defaultValue) {
        this(4, defaultValue);
    }

    /**
     * Creates a default, order-sensitive mapping. If your hostnames are in conflict, the mapping
     * will choose the one you add first.
     *
     * @param initialCapacity initial capacity for the internal map
     * @param defaultValue    the default value for {@link #map(String)} to return when nothing matches the input
     * @deprecated use {@link DomainNameMappingBuilder} to create and fill the mapping instead
     * <p>指定初始容量创建有序映射。</p>
     */
    @Deprecated
    public DomainNameMapping(int initialCapacity, V defaultValue) {
        this(new LinkedHashMap<String, V>(initialCapacity), defaultValue);
    }

    DomainNameMapping(Map<String, V> map, V defaultValue) {
        this.defaultValue = checkNotNull(defaultValue, "defaultValue");
        this.map = map;
        unmodifiableMap = map != null ? Collections.unmodifiableMap(map)
                                      : null;
    }

    /**
     * Adds a mapping that maps the specified (optionally wildcard) host name to the specified output value.
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Wildcard_DNS_record">DNS wildcard</a> is supported as hostname.
     * For example, you can use {@code *.netty.io} to match {@code netty.io} and {@code downloads.netty.io}.
     * </p>
     *
     * @param hostname the host name (optionally wildcard)
     * @param output   the output value that will be returned by {@link #map(String)} when the specified host name
     *                 matches the specified input host name
     * @deprecated use {@link DomainNameMappingBuilder} to create and fill the mapping instead
     * <p>添加主机名模式与输出值的映射，hostname 会经 {@link #normalizeHostname} 规范化。</p>
     */
    @Deprecated
    public DomainNameMapping<V> add(String hostname, V output) {
        map.put(normalizeHostname(checkNotNull(hostname, "hostname")), checkNotNull(output, "output"));
        return this;
    }

    /**
     * Simple function to match <a href="https://en.wikipedia.org/wiki/Wildcard_DNS_record">DNS wildcard</a>.
     * <p>简单 DNS 通配符匹配：{@code *.domain} 可匹配 domain 及其一级子域。</p>
     */
    static boolean matches(String template, String hostName) {
        if (template.startsWith("*.")) {
            return template.regionMatches(2, hostName, 0, hostName.length())
                || commonSuffixOfLength(hostName, template, template.length() - 1);
        }
        return template.equals(hostName);
    }

    /**
     * IDNA ASCII conversion and case normalization
     * <p>IDNA 转 ASCII 并统一为小写（Locale.US）。</p>
     */
    static String normalizeHostname(String hostname) {
        if (needsNormalization(hostname)) {
            hostname = IDN.toASCII(hostname, IDN.ALLOW_UNASSIGNED);
        }
        return hostname.toLowerCase(Locale.US);
    }

    /** 若含非 ASCII 字符则需 IDN 规范化。 */
    private static boolean needsNormalization(String hostname) {
        final int length = hostname.length();
        for (int i = 0; i < length; i++) {
            int c = hostname.charAt(i);
            if (c > 0x7F) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V map(String hostname) {
        if (hostname != null) {
            hostname = normalizeHostname(hostname);

            // 按 LinkedHashMap 插入顺序遍历，先匹配者优先
            for (Map.Entry<String, V> entry : map.entrySet()) {
                if (matches(entry.getKey(), hostname)) {
                    return entry.getValue();
                }
            }
        }
        return defaultValue;
    }

    /**
     * Returns a read-only {@link Map} of the domain mapping patterns and their associated value objects.
     * <p>返回域名模式到值的只读映射视图。</p>
     */
    public Map<String, V> asMap() {
        return unmodifiableMap;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(default: " + defaultValue + ", map: " + map + ')';
    }
}
