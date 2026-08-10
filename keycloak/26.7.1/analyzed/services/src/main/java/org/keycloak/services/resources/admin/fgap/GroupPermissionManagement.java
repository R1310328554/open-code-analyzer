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
package org.keycloak.services.resources.admin.fgap;

import java.util.Map;

import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.GroupModel;

/**
 * 组细粒度管理权限策略管理接口。
 * <p>管理组资源及 view/manage/view-members/manage-members/manage-membership 策略。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface GroupPermissionManagement {
    /** 组是否已启用细粒度管理权限 */
    boolean isPermissionsEnabled(GroupModel group);
    /** 启用或禁用组细粒度管理权限 */
    void setPermissionsEnabled(GroupModel group, boolean enable);

    /** 查看组成员策略 */
    Policy viewMembersPermission(GroupModel group);
    /** 管理组成员策略 */
    Policy manageMembersPermission(GroupModel group);

    Policy manageMembershipPermission(GroupModel group);

    Policy viewPermission(GroupModel group);
    Policy managePermission(GroupModel group);

    /** 组授权资源 */
    Resource resource(GroupModel group);

    /** 各作用域策略 ID 映射 */
    Map<String, String> getPermissions(GroupModel group);

}
