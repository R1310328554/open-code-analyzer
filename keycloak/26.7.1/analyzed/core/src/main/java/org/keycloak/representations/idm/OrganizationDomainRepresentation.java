/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.idm;

/**
 * 组织互联网域名的 REST 表示，支持模式匹配验证。
 *
 * <p>支持的域名匹配模式：
 * <ul>
 *   <li><code>example.com</code> — 仅精确匹配</li>
 *   <li><code>*.example.com</code> — 匹配 example.com 及所有子域</li>
 * </ul>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class OrganizationDomainRepresentation {

    /** 域名或通配符模式（如 {@code *.example.com}）。 */
    private String name;
    /** 域名是否已通过所有权验证。 */
    private boolean verified;

    /** 无参构造，供反射与 JSON 反序列化使用。 */
    public OrganizationDomainRepresentation() {
        // for reflection
    }

    /** @param name 域名或通配符模式 */
    public OrganizationDomainRepresentation(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /** 基于域名名称比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof OrganizationDomainRepresentation)) return false;

        OrganizationDomainRepresentation that = (OrganizationDomainRepresentation) o;
        return name != null && name.equals(that.getName());
    }

    /** 基于域名名称计算哈希。 */
    @Override
    public int hashCode() {
        if (name == null) {
            return super.hashCode();
        }
        return name.hashCode();
    }
}
