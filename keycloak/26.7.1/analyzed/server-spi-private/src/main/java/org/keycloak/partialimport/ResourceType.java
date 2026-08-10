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

package org.keycloak.partialimport;

/**
 * 部分导入支持的 realm 资源类型枚举。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public enum ResourceType {
    /** 用户 */ USER,
    /** 组 */ GROUP,
    /** 客户端 */ CLIENT,
    /** 身份提供者 */ IDP,
    /** 身份提供者映射器 */ IDP_MAPPER,
    /** Realm 角色 */ REALM_ROLE,
    /** 客户端角色 */ CLIENT_ROLE;

    /**
     * 生成管理事件中使用的 admin 路径片段。
     *
     * @return The resource portion of the path.
     */
    public String getPath() {
        switch(this) {
            case USER: return "users";
            case GROUP: return "groups";
            case CLIENT: return "clients";
            case IDP: return "identity-provider-settings";
            case IDP_MAPPER: return "mappers";
            case REALM_ROLE: return "realms";
            case CLIENT_ROLE: return "clients";
            default: return "";
        }
    }

    /** @return 资源类型的可读显示名称 */
    @Override
    public String toString() {
        switch(this) {
            case USER: return "User";
            case GROUP: return "Group";
            case CLIENT: return "Client";
            case IDP: return "Identity Provider";
            case IDP_MAPPER: return "Identity Provider Mapper";
            case REALM_ROLE: return "Realm Role";
            case CLIENT_ROLE: return "Client Role";
            default: return super.toString();
        }
    }
}
