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
package org.keycloak.storage.jpa;

import java.util.regex.Pattern;

import org.keycloak.models.light.LightweightUserAdapter;

import org.jboss.logging.Logger;

/**
 * 联邦用户存储 ID 格式校验工具。
 * <p>
 * 合法键为：标准 UUID、{@code f:[UUID|SHORT_ID]:...} 联邦格式，或轻量用户 {@code lwt:UUID} 前缀。
 *
 * @author hmlnarik
 */
public class KeyUtils {

    private static final Logger LOG = Logger.getLogger(KeyUtils.class);

    /** 标准 UUID 正则。 */
    public static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}");
    /** 22 字符短 ID 正则（Base64 URL-safe）。 */
    public static final Pattern SHORT_ID_PATTERN = Pattern.compile("[0-9A-Za-z_-]{22}");

    /** 合法用户/联邦键的联合匹配模式。 */
    public static final Pattern EXPECTED_KEY_PATTERN = Pattern.compile(
      UUID_PATTERN.pattern()
      + "|"
      + "f:(" + UUID_PATTERN.pattern() + "|" + SHORT_ID_PATTERN.pattern() + "):.*"
      + "|"
      + LightweightUserAdapter.ID_PREFIX + UUID_PATTERN.pattern()
    );

    /**
     * 判断字符串是否为合法键。
     * @param key 键的字符串表示
     * @return 当 key 为 {@code null}、纯 UUID，或 {@code f:[UUID]:...} / {@code f:[SHORT_ID]:...} / 轻量用户格式时为 true
     */
    public static boolean isValidKey(String key) {
        return key == null || EXPECTED_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * 键不合法时记录警告（未来迁移可能失败）。
     * @param key 键的字符串表示
     */
    public static void assertValidKey(String key) throws IllegalArgumentException {
        if (! isValidKey(key)) {
            LOG.warnf("The given key is not a valid key per specification, future migration might fail: %s", key);
        }
    }
}
