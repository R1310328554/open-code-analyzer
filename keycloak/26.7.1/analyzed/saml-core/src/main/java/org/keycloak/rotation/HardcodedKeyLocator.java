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

package org.keycloak.rotation;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 针对一组固定密钥的密钥定位器，可按密钥名称初始化，也可仅按密钥对象初始化。
 *
 * @author <a href="mailto:hmlnarik@redhat.com">Hynek Mlnařík</a>
 */
public class HardcodedKeyLocator implements KeyLocator, Iterable<Key> {

    /** 按名称索引的密钥映射。 */
    private final Map<String, ? extends Key> byName;
    /** 按密钥内容哈希索引的密钥映射。 */
    private final Map<KeyHash, ? extends Key> byKey;

    /**
     * 使用单个密钥构造定位器。
     *
     * @param key 不可为 null 的密钥
     */
    public HardcodedKeyLocator(Key key) {
        Objects.requireNonNull(key, "Key must not be null");
        this.byName = Collections.emptyMap();
        this.byKey = Collections.singletonMap(new KeyHash(key), key);
    }

    /**
     * 使用密钥集合构造定位器（无名称映射）。
     *
     * @param keys 不可为 null 的密钥集合
     */
    public HardcodedKeyLocator(Collection<? extends Key> keys) {
        Objects.requireNonNull(keys, "Keys must not be null");
        this.byName = Collections.emptyMap();
        this.byKey = Collections.unmodifiableMap(keys.stream().collect(
                Collectors.toMap(k -> new KeyHash(k), k -> k, (k1, k2) -> k1)));
    }

    /**
     * 使用名称到密钥的映射构造定位器。
     *
     * @param keys 不可为 null 的名称-密钥映射
     */
    public HardcodedKeyLocator(Map<String, ? extends Key> keys) {
        Objects.requireNonNull(keys, "Keys must not be null");
        this.byName = Collections.unmodifiableMap(keys);
        this.byKey = Collections.unmodifiableMap(keys.values().stream().collect(
                Collectors.toMap(k -> new KeyHash(k), k -> k, (k1, k2) -> k1)));
    }

    @Override
    public Key getKey(String kid) {
        if (this.byKey.size() == 1) {
            return this.byKey.values().iterator().next();
        } else if (kid == null) {
            return null;
        } else {
            return this.byName.get(kid);
        }
    }

    @Override
    public Key getKey(Key key) {
        if (this.byKey.size() == 1) {
            return this.byKey.values().iterator().next();
        } else if (key == null) {
            return null;
        } else {
            return this.byKey.get(new KeyHash(key));
        }
    }

    @Override
    public void refreshKeyCache() {
        // 硬编码密钥无需刷新缓存
    }

    @Override
    public String toString() {
        return "hardcoded keys, count: " + this.byKey.size();
    }

    @Override
    public Iterator<Key> iterator() {
        return (Iterator<Key>) byKey.values().iterator();
    }
}
