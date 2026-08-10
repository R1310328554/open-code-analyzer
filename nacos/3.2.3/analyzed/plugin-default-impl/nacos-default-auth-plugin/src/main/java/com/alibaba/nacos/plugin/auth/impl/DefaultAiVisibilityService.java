/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.auth.impl;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUser;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginManager;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginService;
import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;
import com.alibaba.nacos.plugin.visibility.model.AuthorizedResources;
import com.alibaba.nacos.plugin.visibility.model.BaseVisibilityPredicate;
import com.alibaba.nacos.plugin.visibility.model.VisibilityQueryContext;
import com.alibaba.nacos.plugin.visibility.model.VisibilityResource;
import com.alibaba.nacos.plugin.visibility.spi.QueryAdvisor;
import com.alibaba.nacos.plugin.visibility.spi.ValidationResult;
import com.alibaba.nacos.plugin.visibility.spi.VisibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;

/**
 * Nacos 默认鉴权插件的 AI 可见性服务实现。
 *
 * <p>校验资源可见性、为列表查询提供 {@link QueryAdvisor} 谓词， 并与 {@link AuthPluginService} 联动做细粒度权限校验。</p>
 *
 * @author xiweng.yy
 */
public class DefaultAiVisibilityService implements VisibilityService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAiVisibilityService.class);
    
    private static final String NAME = AuthConstants.AUTH_PLUGIN_TYPE;
    
    /** 可见性权限资源 ID 前缀。 */
    private static final String RESOURCE_PREFIX = "@@visibility";
    
    /** 匿名身份标识，用于区分公开资源查询范围。 */
    private static final String ANONYMOUS_IDENTITY = AuthConstants.ANONYMOUS_USER;
    
    /** 校验当前身份对指定 AI 资源是否具有读/写可见性。 */
    @Override
    public ValidationResult validateVisibility(String identity, String action, String apiType,
        VisibilityResource resource) {
        if (isAuthDisabled(apiType)) {
            return ValidationResult.allow();
        }
        if (isCurrentIdentityGlobalAdmin(identity)) {
            return ValidationResult.allow();
        }
        boolean isRead = VisibilityConstants.ACTION_READ.equals(action);
        if (isPermitted(identity, isRead, resource)) {
            return ValidationResult.allow();
        }
        return ValidationResult
            .deny("No visibility permission for resource: " + resource.getResourceName());
    }
    
    /** 为列表查询推荐基础谓词与授权资源过滤条件。 */
    @Override
    public QueryAdvisor adviseQuery(String identity, String action, String apiType,
        VisibilityQueryContext context) {
        QueryAdvisor advisor = new QueryAdvisor();
        if (isAuthDisabled(apiType) || isCurrentIdentityGlobalAdmin(identity)) {
            advisor.setBasePredicate(BaseVisibilityPredicate.ALL);
            return advisor;
        }
        if (!VisibilityConstants.ACTION_READ.equals(action)) {
            advisor.setBasePredicate(BaseVisibilityPredicate.OWNER);
            return advisor;
        }
        advisor.setBasePredicate(isAnonymousIdentity(identity) ? BaseVisibilityPredicate.PUBLIC
            : BaseVisibilityPredicate.PUBLIC_AND_OWNER);
        AuthorizedResources authorized = new AuthorizedResources();
        authorized.setResourceType(context == null ? null : context.getResourceType());
        // TODO: 查询顾问与鉴权插件深度集成后，填充显式授权资源列表
        authorized.setResources(new ArrayList<>());
        advisor.setAuthorizedPredicate(authorized);
        return advisor;
    }
    
    /** 返回可见性服务名称（与鉴权插件类型一致）。 */
    @Override
    public String getVisibilityServiceName() {
        return NAME;
    }
    
    /** 综合所有者、公开范围与 RBAC 权限判断是否允许访问。 */
    private boolean isPermitted(String currentUser, boolean isRead, VisibilityResource candidate) {
        if (isOwner(currentUser, candidate)) {
            return true;
        }
        if (isRead && VisibilityConstants.SCOPE_PUBLIC.equals(candidate.getScope())) {
            return true;
        }
        String action = isRead ? VisibilityConstants.ACTION_READ : VisibilityConstants.ACTION_WRITE;
        return checkResourcePermission(candidate, action);
    }
    
    private boolean isOwner(String currentUser, VisibilityResource resource) {
        return StringUtils.isNotBlank(currentUser) && currentUser.equals(resource.getOwner());
    }
    
    /** 构造 @@visibility 命名空间下的权限资源标识。 */
    private String buildResourceIdentifier(VisibilityResource res) {
        String ns = StringUtils.isBlank(res.getNamespaceId()) ? Constants.DEFAULT_NAMESPACE_ID
            : res.getNamespaceId();
        return RESOURCE_PREFIX + "/" + ns + "/" + res.getResourceType() + "/"
            + res.getResourceName();
    }
    
    /** 委托鉴权插件校验指定资源的读/写权限。 */
    private boolean checkResourcePermission(VisibilityResource res, String action) {
        String resourceId = buildResourceIdentifier(res);
        Resource resource = new Resource("", "", resourceId, SignType.SPECIFIED, new Properties());
        Permission permission = new Permission(resource, action);
        try {
            Optional<AuthPluginService> authService = findAuthPluginService();
            if (authService.isPresent()) {
                IdentityContext identity =
                    RequestContextHolder.getContext().getAuthContext().getIdentityContext();
                return authService.get().validateAuthority(identity, permission).isSuccess();
            }
            return false;
        } catch (Exception e) {
            LOGGER.debug(
                "[DefaultAiVisibilityService] Permission check failed for resource '{}': {}",
                resourceId,
                e.getMessage());
            return false;
        }
    }
    
    private Optional<AuthPluginService> findAuthPluginService() {
        NacosAuthConfigHolder holder = NacosAuthConfigHolder.getInstance();
        for (NacosAuthConfig config : holder.getAllNacosAuthConfig()) {
            if (config.isAuthEnabled()) {
                return AuthPluginManager.getInstance()
                    .findAuthServiceSpiImpl(config.getNacosAuthSystemType());
            }
        }
        return Optional.empty();
    }
    
    /** 判断当前 API 作用域是否关闭鉴权。 */
    private boolean isAuthDisabled(String apiType) {
        if (StringUtils.isBlank(apiType)) {
            return !NacosAuthConfigHolder.getInstance().isAnyAuthEnabled();
        }
        NacosAuthConfig authConfig =
            NacosAuthConfigHolder.getInstance().getNacosAuthConfigByScope(apiType);
        return authConfig == null || !authConfig.isAuthEnabled();
    }
    
    /** 是否为匿名用户身份。 */
    private boolean isAnonymousIdentity(String identity) {
        return ANONYMOUS_IDENTITY.equals(identity);
    }
    
    /** 当前请求上下文中的用户是否为全局管理员。 */
    private boolean isCurrentIdentityGlobalAdmin(String identity) {
        if (StringUtils.isBlank(identity)) {
            return false;
        }
        try {
            IdentityContext identityContext =
                RequestContextHolder.getContext().getAuthContext().getIdentityContext();
            Object nacosUser = identityContext.getParameter(AuthConstants.NACOS_USER_KEY);
            if (!(nacosUser instanceof NacosUser user)) {
                return false;
            }
            return identity.equals(user.getUserName()) && user.isGlobalAdmin();
        } catch (Exception e) {
            return false;
        }
    }
}
