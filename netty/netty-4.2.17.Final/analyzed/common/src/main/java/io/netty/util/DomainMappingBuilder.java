/*
 * Copyright 2015 The Netty Project
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

/**
 * Builder for immutable {@link DomainNameMapping} instances.
 *
 * @param <V> concrete type of value objects
 * @deprecated Use {@link DomainWildcardMappingBuilder} instead.
 * <p>构建不可变 {@link DomainNameMapping} 的流式 API；已废弃，请改用 {@link DomainWildcardMappingBuilder}。</p>
 */
@Deprecated
public final class DomainMappingBuilder<V> {

    /** 委托给 {@link DomainNameMappingBuilder} 完成实际构建。 */
    private final DomainNameMappingBuilder<V> builder;

    /**
     * Constructor with default initial capacity of the map holding the mappings
     *
     * @param defaultValue the default value for {@link DomainNameMapping#map(String)} to return
     *                     when nothing matches the input
     * <p>使用默认初始容量创建构建器，并指定无匹配时的默认返回值。</p>
     */
    public DomainMappingBuilder(V defaultValue) {
        builder = new DomainNameMappingBuilder<V>(defaultValue);
    }

    /**
     * Constructor with initial capacity of the map holding the mappings
     *
     * @param initialCapacity initial capacity for the internal map
     * @param defaultValue    the default value for {@link DomainNameMapping#map(String)} to return
     *                        when nothing matches the input
     * <p>指定内部映射初始容量与默认返回值。</p>
     */
    public DomainMappingBuilder(int initialCapacity, V defaultValue) {
        builder = new DomainNameMappingBuilder<V>(initialCapacity, defaultValue);
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
     * <p>添加主机名（可含 DNS 通配符）到输出值的映射，hostname 与 output 均不可为 null。</p>
     */
    public DomainMappingBuilder<V> add(String hostname, V output) {
        builder.add(hostname, output);
        return this;
    }

    /**
     * Creates a new instance of immutable {@link DomainNameMapping}
     * Attempts to add new mappings to the result object will cause {@link UnsupportedOperationException} to be thrown
     *
     * @return new {@link DomainNameMapping} instance
     * <p>构建不可变 {@link DomainNameMapping} 实例，构建后不可再修改。</p>
     */
    public DomainNameMapping<V> build() {
        return builder.build();
    }
}
