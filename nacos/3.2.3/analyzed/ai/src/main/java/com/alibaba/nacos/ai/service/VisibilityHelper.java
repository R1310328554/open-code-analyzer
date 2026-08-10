/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.service;

import com.alibaba.nacos.ai.model.AiResource;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;
import com.alibaba.nacos.plugin.visibility.model.VisibilityResource;
import com.alibaba.nacos.plugin.visibility.spi.ValidationResult;
import com.alibaba.nacos.plugin.visibility.spi.VisibilityPluginManager;
import com.alibaba.nacos.plugin.visibility.spi.VisibilityService;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Helper for visibility checking in AI service layer.
 * <p>AI 服务层可见性辅助类：解析当前用户身份、过滤可读资源及校验写权限与默认 scope。</p>
 *
 * @author nacos
 */
public class VisibilityHelper {
    
    /** 可见性插件类型配置键。 */
    private static final String VISIBILITY_PLUGIN_TYPE_CONFIG_KEY = "nacos.plugin.visibility.type";
    
    /** 默认可见性服务插件名。 */
    private static final String DEFAULT_VISIBILITY_SERVICE_NAME = "nacos";
    
    /** 缓存的可见性服务名，避免重复读配置。 */
    private static volatile String cachedVisibilityServiceName;
    
    private VisibilityHelper() {
    }
    
    /**
     * Resolve the current identity from request context using the plugin-level identity abstraction.
     * <p>从请求上下文解析当前用户身份 ID。</p>
     */
    public static String resolveCurrentIdentity() {
        try {
            IdentityContext identity =
                RequestContextHolder.getContext().getAuthContext().getIdentityContext();
            Object id = identity.getParameter(Constants.Identity.IDENTITY_ID);
            return id == null ? "" : id.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Resolve current API type from auth context.
     * <p>从鉴权上下文解析当前 API 类型。</p>
     *
     * @return api type name, empty string when absent
     */
    public static String resolveCurrentApiType() {
        try {
            String apiType = RequestContextHolder.getContext().getAuthContext().getApiType();
            return apiType == null ? "" : apiType;
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Resolve the client IP from request context.
     * <p>从请求上下文解析客户端源 IP。</p>
     *
     * @return client IP address, empty string when absent
     */
    public static String resolveClientIp() {
        try {
            String sourceIp = RequestContextHolder.getContext().getBasicContext()
                .getAddressContext().getSourceIp();
            return sourceIp == null ? "" : sourceIp;
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Filter candidate resources by read permission for current user.
     * <p>按当前用户读权限过滤候选资源列表。</p>
     *
     * @param candidates candidate resources
     * @param <T>        filterable resource type
     * @return resources the current user is allowed to read
     */
    public static <T extends VisibilityResource> List<T> filterReadableResources(
        List<T> candidates) {
        Optional<VisibilityService> visibilityService = findVisibilityService();
        if (visibilityService.isEmpty()) {
            return candidates;
        }
        String currentUser = resolveCurrentIdentity();
        List<T> result = new ArrayList<>(candidates.size());
        for (T each : candidates) {
            ValidationResult validationResult = visibilityService.get()
                .validateVisibility(currentUser, VisibilityConstants.ACTION_READ,
                    resolveCurrentApiType(), each);
            if (validationResult.isAllowed()) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Check read permission for current user on the given resource.
     * <p>判断当前用户是否对指定资源具有读权限。</p>
     *
     * @param resource the resource to check
     * @return true when readable, false otherwise
     */
    public static boolean canReadResource(VisibilityResource resource) {
        Optional<VisibilityService> visibilityService = findVisibilityService();
        if (visibilityService.isEmpty()) {
            return true;
        }
        ValidationResult result = visibilityService.get()
            .validateVisibility(resolveCurrentIdentity(), VisibilityConstants.ACTION_READ,
                resolveCurrentApiType(),
                resource);
        return result.isAllowed();
    }
    
    /**
     * Check write permission for current user on the given resource. Throws 403 if denied.
     * <p>校验写权限，拒绝时抛出 403 ACCESS_DENIED。</p>
     *
     * @param resource the resource to check
     * @throws NacosException if no write permission
     */
    public static void checkWritableResource(AiResource resource) throws NacosException {
        Optional<VisibilityService> visibilityService = findVisibilityService();
        if (visibilityService.isEmpty()) {
            return;
        }
        ValidationResult result = visibilityService.get()
            .validateVisibility(resolveCurrentIdentity(), VisibilityConstants.ACTION_WRITE,
                resolveCurrentApiType(),
                resource);
        if (!result.isAllowed()) {
            throw new NacosApiException(NacosException.NO_RIGHT, ErrorCode.ACCESS_DENIED,
                "No permission to modify " + resource.getType() + ": " + resource.getName());
        }
    }
    
    /**
     * Resolve default scope for creating a new resource, delegated to visibility plugin.
     * <p>解析新建资源的默认可见性 scope，委托可见性插件，缺省 PRIVATE。</p>
     *
     * @param resourceType resource type, such as skill / agentspec
     * @return resolved default scope, fallback to PRIVATE
     */
    public static String resolveDefaultScopeForCreate(String resourceType) {
        String identity = resolveCurrentIdentity();
        String apiType = resolveCurrentApiType();
        return findVisibilityService()
            .map(service -> service.resolveDefaultScopeForCreate(identity, apiType, resourceType))
            .filter(StringUtils::isNotBlank)
            .map(each -> each.toUpperCase(Locale.ROOT))
            .orElse(VisibilityConstants.SCOPE_PRIVATE);
    }
    
    private static String resolveVisibilityServiceName() {
        String serviceName = cachedVisibilityServiceName;
        if (serviceName != null) {
            return serviceName;
        }
        synchronized (VisibilityHelper.class) {
            if (cachedVisibilityServiceName == null) {
                String configured = EnvUtil.getProperty(VISIBILITY_PLUGIN_TYPE_CONFIG_KEY,
                    DEFAULT_VISIBILITY_SERVICE_NAME);
                cachedVisibilityServiceName = configured.trim();
            }
            return cachedVisibilityServiceName;
        }
    }
    
    /**
     * Find configured visibility service from plugin manager.
     * <p>从插件管理器查找已配置的可见性服务实例。</p>
     *
     * @return optional visibility service
     */
    public static Optional<VisibilityService> findVisibilityService() {
        return VisibilityPluginManager.getInstance()
            .findVisibilityService(resolveVisibilityServiceName());
    }
}
