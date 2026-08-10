/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.forms.login.freemarker.model;

import java.util.List;

import org.keycloak.authentication.AuthenticationSelectionOption;
import org.keycloak.forms.login.freemarker.model.AuthenticationContextBean;

/**
 * 组织感知的认证上下文 FreeMarker Bean：包装 {@link AuthenticationContextBean}，按组织登录场景控制“尝试其他方式”、用户名显示及重置凭据等 UI 行为。
 * <p>用于 select-organization.ftl 及身份优先登录页的属性映射。</p>
 */
public class OrganizationAwareAuthenticationContextBean extends AuthenticationContextBean {

    private final AuthenticationContextBean delegate;
    private final boolean showTryAnotherWayLink;

    /**
     * @param delegate 被包装的认证上下文 Bean
     * @param showTryAnotherWayLink 是否允许显示“尝试其他方式”链接
     */
    public OrganizationAwareAuthenticationContextBean(AuthenticationContextBean delegate, boolean showTryAnotherWayLink) {
        super(null, null);
        this.delegate = delegate;
        this.showTryAnotherWayLink = showTryAnotherWayLink;
    }

    @Override
    /** @return 认证方式选择列表 */
    public List<AuthenticationSelectionOption> getAuthenticationSelections() {
        return delegate.getAuthenticationSelections();
    }

    /** @return 是否显示“尝试其他方式”链接 */
    public boolean showTryAnotherWayLink() {
        if (showTryAnotherWayLink) {
            return delegate.showTryAnotherWayLink();
        }
        return false;
    }

    /** @return 是否显示用户名字段 */
    public boolean showUsername() {
        return delegate.showUsername();
    }

    /** @return 是否显示重置凭据链接 */
    public boolean showResetCredentials() {
        return delegate.showResetCredentials();
    }

    /** @return 已尝试的用户名 */
    public String getAttemptedUsername() {
        return delegate.getAttemptedUsername();
    }
}
