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

import java.io.IOException;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
/**
 * 扩展 {@link PolicyResourceService}：使用策略 Provider 的专用表示类解析请求体。
 */
public class PolicyTypeResourceService extends PolicyResourceService {

    /** 构造类型化策略实例服务。 */
    public PolicyTypeResourceService(Policy policy, ResourceServer resourceServer, AuthorizationProvider authorization, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        super(policy, resourceServer, authorization, auth, adminEvent);
    }

    @Override
    /** 按策略类型对应的表示类反序列化 JSON。 */
    protected AbstractPolicyRepresentation doCreateRepresentation(String payload) {
        String type = getPolicy().getType();
        Class<? extends AbstractPolicyRepresentation> representationType = authorization.getProviderFactory(type).getRepresentationType();

        if (representationType == null) {
            throw new RuntimeException("Policy provider for type [" + type + "] returned a null representation type.");
        }

        AbstractPolicyRepresentation representation;

        try {
            representation = JsonSerialization.readValue(payload, representationType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize JSON using policy provider for type [" + type + "].", e);
        }

        representation.setType(type);

        return representation;
    }

    /** 转换为权限型表示（不含策略关联详情）。 */
    protected AbstractPolicyRepresentation toRepresentation(Policy policy, String fields, AuthorizationProvider authorization) {
        return ModelToRepresentation.toRepresentation(policy, authorization, false, false, fields != null && fields.equals("*"));
    }
}
