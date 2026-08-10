/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.common.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Account REST API 版本枚举。
 *
 * <p>用于标识账户管理 REST 接口的 API 版本字符串（如 {@code v1alpha1}）。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public enum AccountRestApiVersion {
    /** Alpha 版账户 REST API（{@code v1alpha1}）。 */
    V1_ALPHA1("v1alpha1");

    /** 默认 API 版本。 */
    public static final AccountRestApiVersion DEFAULT = V1_ALPHA1;
    private static final Map<String,AccountRestApiVersion> ENUM_MAP;

    static {
        Map<String, AccountRestApiVersion> map = new HashMap<>();
        for (AccountRestApiVersion value : AccountRestApiVersion.values()) {
            map.put(value.getStrVersion(), value);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    private final String strVersion;

    AccountRestApiVersion(String strVersion) {
        this.strVersion = strVersion;
    }

    /** 按版本字符串查找枚举常量；未知版本返回 {@code null}。 */
    public static AccountRestApiVersion get(String strVersion) {
        return ENUM_MAP.get(strVersion);
    }

    /** 返回该版本对应的字符串标识。 */
    public String getStrVersion() {
        return strVersion;
    }
}
