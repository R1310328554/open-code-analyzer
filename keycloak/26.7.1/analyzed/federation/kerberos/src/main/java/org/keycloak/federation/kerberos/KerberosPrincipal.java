/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.federation.kerberos;

/**
 * Kerberos 主体名值对象，解析 {@code prefix@REALM} 格式并规范化 realm 为大写。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosPrincipal {

    // 完整主体名，如 "john@KEYCLOAK.ORG"
    private final String kerberosPrincipal;
    /** 主体前缀（用户名部分）。 */
    private final String prefix;
    /** Kerberos realm（大写）。 */
    private final String realm;
    /**
     * 从完整 Kerberos 主体字符串构造；格式必须为 {@code name@REALM}。
     *
     * @param kerberosPrincipal 完整主体名
     */
    public KerberosPrincipal(String kerberosPrincipal) {
        String[] parts = kerberosPrincipal.split("@");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Kerberos principal '" + kerberosPrincipal + "' not valid");
        }
        this.prefix = parts[0];
        this.realm = parts[1].toUpperCase();
        this.kerberosPrincipal = prefix + "@" + realm;
    }

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public String toString() {
        return this.kerberosPrincipal;
    }
}
