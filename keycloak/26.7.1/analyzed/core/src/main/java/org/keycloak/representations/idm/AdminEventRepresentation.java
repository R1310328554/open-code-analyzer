/*
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.keycloak.representations.idm;

import java.util.Map;

/**
 * 管理控制台操作审计事件的 REST 表示，用于 Admin Events API 查询与导出。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class AdminEventRepresentation {

    /** 事件唯一标识。 */
    private String id;
    /** 事件发生时间（Unix 毫秒时间戳）。 */
    private long time;
    /** 所属 realm 的内部 ID。 */
    private String realmId;
    /** 执行该管理操作的主体认证详情。 */
    private AuthDetailsRepresentation authDetails;
    /** 操作类型（如 CREATE、UPDATE、DELETE）。 */
    private String operationType;
    /** 被操作资源的类型名称。 */
    private String resourceType;
    /** 被操作资源的路径标识。 */
    private String resourcePath;
    /** 操作涉及资源的 JSON 表示快照。 */
    private String representation;
    /** 操作失败时的错误信息。 */
    private String error;
    /** 附加键值对详情。 */
    private Map<String, String> details;

    /** @return 事件 ID */
    public String getId() {
        return id;
    }

    /** @param id 事件 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 事件发生时间（毫秒） */
    public long getTime() {
        return time;
    }

    /** @param time 事件发生时间（毫秒） */
    public void setTime(long time) {
        this.time = time;
    }

    /** @return 所属 realm ID */
    public String getRealmId() {
        return realmId;
    }

    /** @param realmId 所属 realm ID */
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** @return 操作主体的认证详情 */
    public AuthDetailsRepresentation getAuthDetails() {
        return authDetails;
    }

    /** @param authDetails 操作主体的认证详情 */
    public void setAuthDetails(AuthDetailsRepresentation authDetails) {
        this.authDetails = authDetails;
    }

    /** @return 操作类型 */
    public String getOperationType() {
        return operationType;
    }

    /** @param operationType 操作类型 */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /** @return 资源类型 */
    public String getResourceType() {
        return resourceType;
    }

    /** @param resourceType 资源类型 */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /** @return 资源路径 */
    public String getResourcePath() {
        return resourcePath;
    }

    /** @param resourcePath 资源路径 */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /** @return 资源 JSON 快照 */
    public String getRepresentation() {
        return representation;
    }

    /** @param representation 资源 JSON 快照 */
    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    /** @return 错误信息，成功时为 null */
    public String getError() {
        return error;
    }

    /** @param error 错误信息 */
    public void setError(String error) {
        this.error = error;
    }

    /** @return 附加详情映射 */
    public Map<String, String> getDetails() {
        return details;
    }

    /** @param details 附加详情映射 */
    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
