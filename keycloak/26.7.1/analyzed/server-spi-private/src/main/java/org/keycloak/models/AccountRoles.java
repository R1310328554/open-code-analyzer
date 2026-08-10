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

package org.keycloak.models;

/**
 * 账户控制台（Account Console）角色名称常量。
 * <p>定义用户自助管理资料、应用、同意与可验证凭证等权限角色。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface AccountRoles {

    /** 查看个人资料。 */
    String VIEW_PROFILE = "view-profile";
    /** 管理账户设置。 */
    String MANAGE_ACCOUNT = "manage-account";
    /** 管理关联的 IdP 账户链接。 */
    String MANAGE_ACCOUNT_LINKS = "manage-account-links";
    /** 查看已授权应用列表。 */
    String VIEW_APPLICATIONS = "view-applications";
    /** 查看 OAuth 同意记录。 */
    String VIEW_CONSENT = "view-consent";
    /** 撤销或管理 OAuth 同意。 */
    String MANAGE_CONSENT = "manage-consent";
    /** 删除账户（需启用 delete-account 必需操作）。 */
    String DELETE_ACCOUNT = "delete-account";
    /** 查看所属用户组。 */
    String VIEW_GROUPS = "view-groups";
    /** 查看可验证凭证（VC）。 */
    String VIEW_VERIFIABLE_CREDENTIALS = "view-verifiable-credentials";
    /** 管理可验证凭证（VC）。 */
    String MANAGE_VERIFIABLE_CREDENTIALS = "manage-verifiable-credentials";

    /** 默认授予的账户角色集合。 */
    String[] DEFAULT = {VIEW_PROFILE, MANAGE_ACCOUNT};

}
