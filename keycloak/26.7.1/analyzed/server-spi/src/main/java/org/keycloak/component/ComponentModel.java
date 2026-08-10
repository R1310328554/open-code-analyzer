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

package org.keycloak.component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.keycloak.common.util.MultivaluedHashMap;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 组件配置模型：持久化存储组件实例的 ID、名称、提供者类型及键值配置。
 * <p>Stored configuration of a User Storage provider instance.</p>
 * <p>支持运行时 notes 与 Infinispan ProtoStream 序列化。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
public class ComponentModel {

    private String id;
    private String name;
    private String providerId;
    private String providerType;
    private String parentId;
    private String subType;
    private MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
    private transient ConcurrentHashMap<String, Object> notes = new ConcurrentHashMap<>();

    /** 默认构造函数。 */
    public ComponentModel() {}

    /** 复制构造：深拷贝配置映射。 */
    public ComponentModel(ComponentModel copy) {
        this.id = copy.id;
        this.name = copy.name;
        this.providerId = copy.providerId;
        this.providerType = copy.providerType;
        this.parentId = copy.parentId;
        this.subType = copy.subType;
        this.config.addAll(copy.config);
    }


    @ProtoField(1)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ProtoField(2)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return 多值配置映射 */
    public MultivaluedHashMap<String, String> getConfig() {
        return config;
    }

    public void setConfig(MultivaluedHashMap<String, String> config) {
        this.config = config;
    }

    /** @param key 配置键
     * @return 存在该键时返回 {@code true} */
    public boolean contains(String key) {
        return config.containsKey(key);
    }

    /** @param key 配置键
     * @return 首个配置值，不存在时返回 {@code null} */
    public String get(String key) {
        return config.getFirst(key);
    }

    /** @param defaultValue 默认值
     * @return 配置值或默认值 */
    public String get(String key, String defaultValue) {
        String s = get(key);
        return s != null ? s : defaultValue;
    }

    /** 读取整型配置值。 */
    public int get(String key, int defaultValue) {
        String s = get(key);
        return s != null ? Integer.parseInt(s) : defaultValue;
    }

    /** 读取长整型配置值。 */
    public long get(String key, long defaultValue) {
        String s = get(key);
        return s != null ? Long.parseLong(s) : defaultValue;
    }

    /** 读取布尔配置值。 */
    public boolean get(String key, boolean defaultValue) {
        String s = get(key);
        return s != null ? Boolean.valueOf(s) : defaultValue;
    }

    /** 写入字符串配置项。 */
    public void put(String key, String value) {
        config.putSingle(key, value);
    }

    public void put(String key, int value) {
        put(key, Integer.toString(value));
    }

    public void put(String key, long value) {
        put(key, Long.toString(value));
    }

    public void put(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    /** @return 运行时 note 是否存在 */
    public boolean hasNote(String key) {
        return notes.containsKey(key);
    }

    /** @return 运行时 note 值（不持久化） */
    public <T> T getNote(String key) {
        return (T) notes.get(key);
    }

    /** 设置运行时 note（会话级，不写入数据库）。 */
    public void setNote(String key, Object object) {
        notes.put(key, object);
    }

    public void removeNote(String key) {
        notes.remove(key);
    }

    @ProtoField(3)
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @ProtoField(4)
    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    @ProtoField(5)
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @ProtoField(6)
    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @ProtoField(7)
    public List<MultiMapEntry> getConfigProto() {
        return config.entrySet().stream().map(MultiMapEntry::new).collect(Collectors.toList());
    }

    public void setConfigProto(List<MultiMapEntry> configProto) {
        if (configProto != null) {
            configProto.forEach(multiMapEntry -> multiMapEntry.insert(config));
        }
    }

    /** ProtoStream 多值映射条目，用于配置序列化。 */
    @ProtoTypeId(65538) //see org.keycloak.Marshalling
    public static final class MultiMapEntry {
        private final String key;
        private final List<String> value;

        @ProtoFactory
        public MultiMapEntry(String key, List<String> value) {
            this.key = key;
            this.value = value;
        }

        public MultiMapEntry(Map.Entry<String, List<String>> entry) {
            this(entry.getKey(), entry.getValue());
        }

        @ProtoField(1)
        public String getKey() {
            return key;
        }

        @ProtoField(2)
        public List<String> getValue() {
            return value;
        }

        /** 将条目插入目标配置映射。 */
        public void insert(MultivaluedHashMap<String, String> config) {
            config.put(key, value);
        }
    }
}
