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

package org.keycloak.social.openshift;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * OpenShift v4 用户属性映射器。
 * <p>将 OpenShift User API 返回的 JSON 字段映射到 Keycloak 用户属性。</p>
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class OpenshiftV4AttributeMapper extends AbstractJsonUserAttributeMapper {
    /** 映射器唯一 id。 */
    public static final String ID = "openshift-v4-user-attribute-mapper";
    /** 兼容的 IdP provider id 列表。 */
    private static final String[] cp = new String[] { OpenshiftV4IdentityProviderFactory.PROVIDER_ID };

    /** 返回仅支持 OpenShift v4 IdP 的 provider id 数组。 */
    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    /** 返回 {@link #ID}。 */
    @Override
    public String getId() {
        return ID;
    }
}
