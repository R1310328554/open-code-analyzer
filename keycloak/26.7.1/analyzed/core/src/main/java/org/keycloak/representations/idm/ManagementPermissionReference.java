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
package org.keycloak.representations.idm;

import java.util.Map;

/**
 * 细粒度管理权限的引用表示，描述某资源上各 scope 对应的权限策略 ID。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ManagementPermissionReference {
    /** 是否已启用细粒度权限。 */
    private boolean enabled;
    /** 受管资源的标识。 */
    private String resource;
    /** scope 名称到权限策略 ID 的映射。 */
    private Map<String, String> scopePermissions;

    /** @return 是否启用细粒度权限 */
    public boolean isEnabled() {
        return enabled;
    }

    /** @param enabled 是否启用细粒度权限 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 资源标识 */
    public String getResource() {
        return resource;
    }

    /** @param resource 资源标识 */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /** @return scope 到权限策略 ID 的映射 */
    public Map<String, String> getScopePermissions() {
        return scopePermissions;
    }

    /** @param scopePermissions scope 到权限策略 ID 的映射 */
    public void setScopePermissions(Map<String, String> scopePermissions) {
        this.scopePermissions = scopePermissions;
    }
}
