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

package com.alibaba.nacos.plugin.visibility.spi;

import com.alibaba.nacos.plugin.visibility.constant.VisibilityConstants;
import com.alibaba.nacos.plugin.visibility.model.VisibilityQueryContext;
import com.alibaba.nacos.plugin.visibility.model.VisibilityResource;

import java.util.Properties;

/**
 * 资源可见性服务 SPI 接口。
 *
 * <p>各可见性策略插件需实现本接口，供 {@link VisibilityPluginManager}
 * 按服务名加载并调用，用于控制资源的读写可见范围。</p>
 *
 * @author xiweng.yy
 */
public interface VisibilityService {
    
    /**
     * 使用外部配置初始化服务。
     *
     * <p>配置来源由 {@link VisibilityPluginManager} 统一管理。默认空实现以保持
     * 与已有 SPI 实现的向后兼容性。</p>
     *
     * @param properties 服务专属配置项
     */
    default void init(Properties properties) {
    }
    
    /**
     * 解析新建资源的默认可见范围。
     *
     * <p>默认实现返回 {@link VisibilityConstants#SCOPE_PRIVATE}，
     * 以保持与已有 SPI 实现的向后兼容性。</p>
     *
     * @param identity     当前操作者身份
     * @param apiType      当前 API 类型
     * @param resourceType 资源类型，如 skill / agentspec
     * @return 新建资源的默认可见范围
     */
    default String resolveDefaultScopeForCreate(String identity, String apiType,
        String resourceType) {
        return VisibilityConstants.SCOPE_PRIVATE;
    }
    
    /**
     * 校验单个资源的可见性权限。
     *
     * @param identity 当前操作者身份
     * @param action   操作类型（读/写）
     * @param apiType  当前 API 类型
     * @param resource 待校验的资源
     * @return 校验结果
     */
    ValidationResult validateVisibility(String identity, String action, String apiType,
        VisibilityResource resource);
    
    /**
     * 为范围或列表查询提供可见性过滤建议。
     *
     * <p>返回 {@link QueryAdvisor}，指导查询层如何过滤不可见资源。</p>
     *
     * @param identity 当前操作者身份
     * @param action   操作类型（读/写）
     * @param apiType  当前 API 类型
     * @param context  查询上下文
     * @return 查询可见性建议
     */
    QueryAdvisor adviseQuery(String identity, String action, String apiType,
        VisibilityQueryContext context);
    
    /**
     * 返回可见性服务名称。
     *
     * <p>同名服务后加载者会覆盖先加载者。</p>
     *
     * @return 服务名称
     */
    String getVisibilityServiceName();
}
