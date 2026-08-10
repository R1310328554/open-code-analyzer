/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.keycloak.representations.AccessToken;

/**
 * UMA 授权请求体，封装权限票据、声明令牌、受众及待评估的权限集合。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthorizationRequest {

    /** 权限票据（permission ticket）。 */
    private String ticket;
    /** 附加声明令牌（claim token）。 */
    private String claimToken;
    /** 声明令牌格式。 */
    private String claimTokenFormat;
    /** 持久化声明令牌（PCT）。 */
    private String pct;
    /** 请求的 Scope。 */
    private String scope;
    /** 待评估的权限票据令牌。 */
    private PermissionTicketToken permissions = new PermissionTicketToken();
    /** 响应元数据选项。 */
    private Metadata metadata;
    /** 目标受众（audience）。 */
    private String audience;
    /** 主体令牌（subject token）。 */
    private String subjectToken;
    /** 是否提交权限请求（而非仅评估）。 */
    private boolean submitRequest;
    /** 附加声明键值对。 */
    private Map<String, List<String>> claims;
    /** 请求携带的 RPT（Requesting Party Token）。 */
    private AccessToken rpt;
    /** RPT 的字符串形式。 */
    private String rptToken;

    /** @param ticket 权限票据 */
    public AuthorizationRequest(String ticket) {
        this.ticket = ticket;
    }

    public AuthorizationRequest() {
        this(null);
    }

    /** @return 权限票据 */
    public String getTicket() {
        return this.ticket;
    }

    /** @param ticket 权限票据 */
    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    /** @return RPT 对象 */
    public AccessToken getRpt() {
        return this.rpt;
    }

    /** @param rpt RPT 对象 */
    public void setRpt(AccessToken rpt) {
        this.rpt = rpt;
    }

    /** @param rpt RPT 字符串 */
    public void setRpt(String rpt) {
        this.rptToken = rpt;
    }

    /** @return RPT 字符串 */
    public String getRptToken() {
        return rptToken;
    }

    /** @param claimToken 声明令牌 */
    public void setClaimToken(String claimToken) {
        this.claimToken = claimToken;
    }

    /** @return 声明令牌 */
    public String getClaimToken() {
        return claimToken;
    }

    /** @param claimTokenFormat 声明令牌格式 */
    public void setClaimTokenFormat(String claimTokenFormat) {
        this.claimTokenFormat = claimTokenFormat;
    }

    /** @return 声明令牌格式 */
    public String getClaimTokenFormat() {
        return claimTokenFormat;
    }

    /** @param pct 持久化声明令牌 */
    public void setPct(String pct) {
        this.pct = pct;
    }

    /** @return 持久化声明令牌 */
    public String getPct() {
        return pct;
    }

    /** @param scope 请求 Scope */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /** @return 请求 Scope */
    public String getScope() {
        return scope;
    }

    /** @param permissions 权限票据令牌 */
    public void setPermissions(PermissionTicketToken permissions) {
        this.permissions = permissions;
    }

    /** @return 权限票据令牌 */
    public PermissionTicketToken getPermissions() {
        return permissions;
    }

    /** @return 响应元数据 */
    public Metadata getMetadata() {
        return metadata;
    }

    /** @param metadata 响应元数据 */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /** @param audience 目标受众 */
    public void setAudience(String audience) {
        this.audience = audience;
    }

    /** @return 目标受众 */
    public String getAudience() {
        return audience;
    }

    /** @param subjectToken 主体令牌 */
    public void setSubjectToken(String subjectToken) {
        this.subjectToken = subjectToken;
    }

    /** @return 主体令牌 */
    public String getSubjectToken() {
        return subjectToken;
    }

    /** @return 附加声明映射 */
    public Map<String, List<String>> getClaims() {
        return claims;
    }

    /** @param claims 附加声明映射 */
    public void setClaims(Map<String, List<String>> claims) {
        this.claims = claims;
    }

    /** 为指定资源添加 Scope 权限（列表形式）。 */
    public void addPermission(String resourceId, List<String> scopes) {
        addPermission(resourceId, scopes.toArray(new String[scopes.size()]));
    }

    /** 为指定资源添加一个或多个 Scope 权限。 */
    public void addPermission(String resourceId, String... scopes) {
        if (permissions == null) {
            permissions = new PermissionTicketToken(new ArrayList<Permission>());
        }

        Permission permission = null;

        for (Permission resourcePermission : permissions.getPermissions()) {
            if (resourcePermission.getResourceId() != null && resourcePermission.getResourceId().equals(resourceId)) {
                permission = resourcePermission;
                break;
            }
        }

        if (permission == null) {
            permission = new Permission(resourceId, new HashSet<String>());
            permissions.getPermissions().add(permission);
        }

        permission.getScopes().addAll(Arrays.asList(scopes));
    }

    /** @param submitRequest 是否提交权限请求 */
    public void setSubmitRequest(boolean submitRequest) {
        this.submitRequest = submitRequest;
    }

    /** @return 是否提交权限请求（需同时存在 ticket） */
    public boolean isSubmitRequest() {
        return submitRequest && ticket != null;
    }

    /** 授权响应的元数据选项。 */
    public static class Metadata {

        /** 是否在响应中包含资源名称。 */
        private Boolean includeResourceName;
        /** 返回权限的最大条数限制。 */
        private Integer limit;
        /** 响应模式（如 decision 或 permissions）。 */
        private String responseMode;
        /** 权限中资源的表示格式。 */
        private String permissionResourceFormat;
        /** 权限资源是否按 URI 匹配。 */
        private Boolean permissionResourceMatchingUri;

        /** @return 是否包含资源名称，默认为 true */
        public Boolean getIncludeResourceName() {
            if (includeResourceName == null) {
                includeResourceName = Boolean.TRUE;
            }
            return includeResourceName;
        }

        /** @param includeResourceName 是否包含资源名称 */
        public void setIncludeResourceName(Boolean includeResourceName) {
            this.includeResourceName = includeResourceName;
        }

        /** @return 返回条数限制 */
        public Integer getLimit() {
            return limit;
        }

        /** @param limit 返回条数限制 */
        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        /** @param responseMode 响应模式 */
        public void setResponseMode(String responseMode) {
            this.responseMode = responseMode;
        }

        /** @return 响应模式 */
        public String getResponseMode() {
            return responseMode;
        }

        /** @return 权限资源表示格式 */
        public String getPermissionResourceFormat() {
            return permissionResourceFormat;
        }

        /** @param permissionResourceFormat 权限资源表示格式 */
        public void setPermissionResourceFormat(String permissionResourceFormat) {
            this.permissionResourceFormat = permissionResourceFormat;
        }

        /** @return 权限资源是否按 URI 匹配 */
        public Boolean getPermissionResourceMatchingUri() {
            return permissionResourceMatchingUri;
        }

        /** @param permissionResourceMatchingUri 权限资源是否按 URI 匹配 */
        public void setPermissionResourceMatchingUri(Boolean permissionResourceMatchingUri) {
            this.permissionResourceMatchingUri = permissionResourceMatchingUri;
        }
    }
}
