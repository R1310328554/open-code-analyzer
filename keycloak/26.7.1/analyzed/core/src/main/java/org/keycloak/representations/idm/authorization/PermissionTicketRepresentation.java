/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.idm.authorization;

/**
 * 权限票据（Permission Ticket）的 REST 表示，用于 UMA 场景下的权限申请与审批。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionTicketRepresentation {

    /** 票据唯一标识。 */
    private String id;
    /** 资源所有者 ID。 */
    private String owner;
    /** 目标资源 ID。 */
    private String resource;
    /** 目标作用域 ID。 */
    private String scope;
    /** 是否已批准授予。 */
    private boolean granted;
    /** 作用域显示名称。 */
    private String scopeName;
    /** 资源显示名称。 */
    private String resourceName;
    /** 请求者 ID。 */
    private String requester; 
    /** 所有者显示名称。 */
    private String ownerName;
    /** 请求者显示名称。 */
    private String requesterName;

    /** @return 票据 ID */
    public String getId() {
        return id;
    }

    /** @param id 票据 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 资源所有者 ID */
    public String getOwner() {
        return owner;
    }

    /** @param owner 资源所有者 ID */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** @return 目标资源 ID */
    public String getResource() {
        return resource;
    }

    /** @param resource 目标资源 ID */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /** @return 目标作用域 ID */
    public String getScope() {
        return scope;
    }

    /** @param scope 目标作用域 ID */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** @return 是否已批准 */
    public boolean isGranted() {
        return granted;
    }

    /** @param granted 是否已批准 */
    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    /** @param scopeName 作用域显示名称 */
    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    /** @return 作用域显示名称 */
    public String getScopeName() {
        return scopeName;
    }

    /** @param resourceName 资源显示名称 */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /** @return 资源显示名称 */
    public String getResourceName() {
        return resourceName;
    }
    
    /** @param requesterName 请求者显示名称 */
    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    /** @return 请求者显示名称 */
    public String getRequesterName() {
        return requesterName;
    }
    
    /** @param requester 请求者 ID */
    public void setRequester(String requester) {
        this.requester = requester;
    }

    /** @return 请求者 ID */
    public String getRequester() {
        return requester;
    }
    
    /** @param ownerName 所有者显示名称 */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /** @return 所有者显示名称 */
    public String getOwnerName() {
        return ownerName;
    }
}
