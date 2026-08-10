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

package org.keycloak.events.admin;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理 REST API 操作产生的审计事件模型，记录操作者、资源路径与变更内容。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AdminEvent {

    private String id;

    private long time;

    private String realmId;

    private String realmName;

    private AuthDetails authDetails;

    /** 触发本 {@link AdminEvent} 的资源类型字符串（可为自定义类型）。 */
    
    private String resourceType;

    private OperationType operationType;

    private String resourcePath;

    private String representation;

    private String error;

    private Map<String, String> details;

    /** 无参构造，供序列化与框架实例化。 */
    public AdminEvent() {}
    /** 深拷贝构造，复制认证详情与扩展字段。 */
    public AdminEvent(AdminEvent toCopy) {
        this.id = toCopy.getId();
        this.time = toCopy.getTime();
        this.realmId = toCopy.getRealmId();
        this.realmName = toCopy.getRealmName();
        this.authDetails = new AuthDetails(toCopy.getAuthDetails());
        this.resourceType = toCopy.getResourceTypeAsString();
        this.operationType = toCopy.getOperationType();
        this.resourcePath = toCopy.getResourcePath();
        this.representation = toCopy.getRepresentation();
        this.error = toCopy.getError();
        this.details = toCopy.getDetails() == null ? null : new HashMap<>(toCopy.getDetails());
    }

    /** 从 {@link #resourcePath} 末段解析资源 ID（最后一个 {@code /} 之后）。 */
    public String getResourceId() {
        if (this.resourcePath != null) {
            int slashIndex = this.resourcePath.lastIndexOf("/");
            if (slashIndex < this.resourcePath.length() - 1) {
                // return the id that is found after the last slash
                return this.resourcePath.substring(slashIndex + 1);
            }
        }
        return null;
    }

    /**
     * @return 事件 UUID
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return 事件发生时间（毫秒时间戳）
     */
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return 受影响 realm 的 ID
     */
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** @return 受影响 realm 的名称 */
    
    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /**
     * @return 执行管理操作的主体认证信息
     */
    public AuthDetails getAuthDetails() {
        return authDetails;
    }

    public void setAuthDetails(AuthDetails authDetails) {
        this.authDetails = authDetails;
    }

    /**
     * @return 操作类型（创建、更新、删除或动作）
     */
    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    /**
     * 返回被操作资源的 REST 路径，例如：
     * <ul>
     *     <li><b>realms</b> — realm 列表</li>
     *     <li><b>realms/master</b> — master realm</li>
     *     <li><b>realms/clients/00d4b16f-f1f9-4e73-8366-d76b18f3e0e1</b> — master realm 下的客户端</li>
     * </ul>
     *
     * @return 资源路径
     */
    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /**
     * 当 {@code operationType} 为 {@code CREATE} 或 {@code UPDATE} 时返回变更后的 JSON 表示，否则为 {@code null}。
     *
     * @return 资源 JSON 表示或 {@code null}
     */
    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    /**
     * 操作失败时返回错误信息，成功时为 {@code null}。
     *
     * @return 错误消息或 {@code null}
     */
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * 返回本事件影响的 {@link ResourceType}，如 {@link ResourceType#USER USER}、{@link ResourceType#GROUP GROUP} 等。
     * <p>未知字符串映射为 {@link ResourceType#CUSTOM}。</p>
     *
     * @return 资源类型枚举
     */
    public ResourceType getResourceType() {
        if (resourceType == null) {
          return null;
        }
        try {
            return ResourceType.valueOf(resourceType);
        }
        catch (IllegalArgumentException e) {
            return ResourceType.CUSTOM;
        }
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType == null ? null  : resourceType.toString();
    }

    /**
     * 以字符串形式返回资源类型；可为 {@link ResourceType} 之外的自定义值，此时 {@link #getResourceType()} 返回 {@code CUSTOM}。
     *
     * @return 资源类型字符串
     */
    public String getResourceTypeAsString() {
        return resourceType;
    }

    /** 设置自定义资源类型字符串（非 {@link ResourceType} 枚举值时使用）。 */
    
    public void setResourceTypeAsString(String resourceType) {
        this.resourceType = resourceType;
    }

    /** @return 附加键值对详情（可为 {@code null}） */
    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
