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

package org.keycloak.forms.login.freemarker.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.models.OrganizationDomainModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;

/**
 * 组织 FreeMarker Bean：向登录/选择组织模板暴露组织名称、别名、域名与成员状态。
 * <p>基于 {@link OrganizationModel} 与当前 {@link UserModel} 构建只读视图。</p>
 */
public class OrganizationBean {

    /** 组织显示名称。 */
    private final String name;
    /** 组织别名（URL 友好标识）。 */
    private final String alias;
    /** 组织关联的域名集合。 */
    private final Set<String> domains;
    /** 当前用户是否为该组织成员。 */
    private final boolean isMember;
    /** 组织自定义属性（不可变映射）。 */
    private final Map<String, List<String>> attributes;

    /** @param organization 组织模型 @param user 当前用户（可为 null） */
    public OrganizationBean(OrganizationModel organization, UserModel user) {
        this.name = organization.getName();
        this.alias = organization.getAlias();
        this.domains = organization.getDomains().map(OrganizationDomainModel::getName).collect(Collectors.toSet());
        this.isMember = user != null && organization.isMember(user);
        this.attributes = Collections.unmodifiableMap(organization.getAttributes());
    }

    /** @return 组织名称 */
    public String getName() {
        return name;
    }

    /** @return 组织别名 */
    public String getAlias() {
        return alias;
    }

    /** @return 组织域名集合 */
    public Set<String> getDomains() {
        return domains;
    }

    /** @return 组织属性映射 */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** @return 当前用户是否为组织成员 */
    public boolean isMember() {
        return isMember;
    }
}
