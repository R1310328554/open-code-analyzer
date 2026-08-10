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

package org.keycloak.protocol.oid4vc.model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.keycloak.protocol.oid4vc.issuance.mappers.OID4VCTargetRoleMapper;

/**
 * {@link OID4VCTargetRoleMapper} 写入凭证的角色 POJO。
 * <p>包含角色名集合与目标声明路径。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class Role {

    /** 要映射的角色名集合。 */
    private Set<String> names;
    /** 角色写入凭证中的目标路径/字段。 */
    private String target;

    /** 默认构造器。 */
    public Role() {
    }

    /** @param names 角色名集合
     * @param target 目标路径 */
    public Role(Set<String> names, String target) {
        this.names = Collections.unmodifiableSet(names);
        this.target = target;
    }

    /** @return 角色名集合 */
    public Set<String> getNames() {
        return names;
    }

    /** @param names 角色名集合 */
    public void setNames(Set<String> names) {
        this.names = names;
    }

    /** @return 目标路径 */
    public String getTarget() {
        return target;
    }

    /** @param target 目标路径 */
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(names, role.names) && Objects.equals(target, role.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names, target);
    }
}