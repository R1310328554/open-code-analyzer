/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.representations.idm;

import java.util.Objects;

import org.keycloak.common.util.ObjectUtil;

/**
 * 用于描述 LDAP 模式、扩展与特性的 OID（对象标识符）值对象。
 * 参见 <a href="https://ldap.com/ldap-oid-reference-guide/">LDAP OID Reference Guide</a>。
 *
 * @author Lars Uffmann, 2020-05-13
 * @since 11.0
 */
public class LDAPCapabilityRepresentation {

    /** LDAP 能力类型枚举。 */
    public enum CapabilityType {
        /** LDAP 控制扩展。 */
        CONTROL,
        /** LDAP 标准扩展。 */
        EXTENSION,
        /** LDAP 特性。 */
        FEATURE,
        /** 未知类型。 */
        UNKNOWN;

        /**
         * 根据 Root DSE 属性名解析能力类型。
         *
         * @param attributeName Root DSE 属性名
         * @return 对应的能力类型
         */
        public static CapabilityType fromRootDseAttributeName(String attributeName) {
            switch (attributeName) {
                case "supportedExtension": return CapabilityType.EXTENSION;
                case "supportedControl": return CapabilityType.CONTROL;
                case "supportedFeatures": return CapabilityType.FEATURE;
                default: return CapabilityType.UNKNOWN;
            }
        }
    };

    /** OID 值（可为 String 或其他类型）。 */
    private Object oid;

    /** 能力类型。 */
    private CapabilityType type;

    /** 无参构造。 */
    public LDAPCapabilityRepresentation() {
    }

    /**
     * @param oidValue OID 值
     * @param type 能力类型
     */
    public LDAPCapabilityRepresentation(Object oidValue, CapabilityType type) {
        this.oid = Objects.requireNonNull(oidValue);
        this.type = type;
    }

    /** @return OID 字符串 */
    public String getOid() {
        return oid instanceof String ? (String) oid : String.valueOf(oid);
    }

    /** @return 能力类型 */
    public CapabilityType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LDAPCapabilityRepresentation ldapOid = (LDAPCapabilityRepresentation) o;
        return ObjectUtil.isEqualOrBothNull(oid, ldapOid.oid) && ObjectUtil.isEqualOrBothNull(type, ldapOid.type);
    }

    @Override
    public int hashCode() {
        return oid.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(LDAPCapabilityRepresentation.class.getSimpleName() + "[ ")
                .append("oid=" + oid + ", ")
                .append("type=" + type + " ]")
                .toString();
    }
}
