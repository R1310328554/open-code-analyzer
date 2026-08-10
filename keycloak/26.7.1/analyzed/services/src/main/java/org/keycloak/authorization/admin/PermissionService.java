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
package org.keycloak.authorization.admin;

import java.util.List;
import java.util.Map;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.extensions.Extension;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
/**
 * 权限策略专用服务：搜索与 CRUD 时自动过滤 {@code permission=true} 的策略。
 */
public class PermissionService extends PolicyService {

    /** 构造权限管理服务。 */
    public PermissionService(ResourceServer resourceServer, AuthorizationProvider authorization, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(resourceServer, authorization, auth, adminEvent);
    }

    @Override
    /** @return 权限策略实例资源服务 */
    protected PolicyResourceService doCreatePolicyResource(Policy policy) {
        return new PolicyTypeResourceService(policy, resourceServer, authorization, auth, adminEvent);
    }

    @Override
    /** @return 按类型过滤的权限策略列表服务 */
    protected PolicyTypeService doCreatePolicyTypeResource(String type) {
        return new PolicyTypeService(type, resourceServer, authorization, auth, adminEvent) {
            @Override
            /** 搜索时强制加入 PERMISSION=true 过滤条件。 */
    protected List<Object> doSearch(Integer firstResult, Integer maxResult, String fields, Map<Policy.FilterOption, String[]> filters) {
                filters.put(Policy.FilterOption.PERMISSION, new String[] {Boolean.TRUE.toString()});
                filters.put(Policy.FilterOption.TYPE, new String[] {type});
                return super.doSearch(firstResult, maxResult, fields, filters);
            }
        };
    }

    @Override
    protected List<Object> doSearch(Integer firstResult, Integer maxResult, String fields, Map<Policy.FilterOption, String[]> filters) {
        filters.put(Policy.FilterOption.PERMISSION, new String[] {Boolean.TRUE.toString()});
        return super.doSearch(firstResult, maxResult, fields, filters);
    }

    @Override
    /** 权限表示转换（不含关联策略详情）。 */
    protected AbstractPolicyRepresentation toRepresentation(Policy policy, String fields, AuthorizationProvider authorization) {
        return ModelToRepresentation.toRepresentation(policy, authorization, false, false, fields != null && fields.equals("*"));
    }
}
