/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Supplier;

import org.keycloak.utils.StringUtil;

/**
 * 领域配置抽象基类：提供将配置属性持久化到 {@link RealmModel} 的通用逻辑。
 * <p>子类通过 {@link #persistRealmAttribute} 写入 realm 属性，支持整型与字符串。</p>
 */
public abstract class AbstractConfig implements Serializable {

    /** 已弃用：只读 realm 引用。
     * @deprecated 自 26.5 起移除 */
    @Deprecated(since = "26.5", forRemoval = true)
    protected transient Supplier<RealmModel> realm;

    // 构造期间避免触发 setter 导致数据库写入
    // Make sure setters are not called when calling this from constructor to avoid DB updates
    /** 用于写入 realm 属性的 realm 供应器（构造期间可为 null）。 */
    protected transient Supplier<RealmModel> realmForWrite;

    /** 将字符串属性持久化到 realm。
     * @param name 属性名
     * @param value 属性值 */
    protected void persistRealmAttribute(String name, String value) {
        RealmModel realm = realmForWrite == null ? null : this.realmForWrite.get();
        if (realm != null) {
            realm.setAttribute(name, value);
        }
    }

    /** 将整型属性持久化到 realm。
     * @param name 属性名
     * @param value 属性值 */
    protected void persistRealmAttribute(String name, Integer value) {
        RealmModel realm = realmForWrite == null ? null : this.realmForWrite.get();
        if (realm != null) {
            realm.setAttribute(name, value);
        }
    }

    /** 从属性映射读取整型值，解析失败或为空时返回默认值。
     * @param attributes 属性映射
     * @param name 属性名
     * @param defaultValue 默认值
     * @return 解析后的整型值 */
    protected static int getIntAttribute(Map<String, String> attributes, String name, int defaultValue) {
        var value = attributes.get(name);
        if (StringUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
