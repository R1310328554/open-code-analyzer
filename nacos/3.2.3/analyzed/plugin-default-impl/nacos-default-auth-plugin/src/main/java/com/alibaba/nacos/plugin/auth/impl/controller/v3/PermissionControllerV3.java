/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.plugin.auth.impl.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionInfo;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * V3 权限管理 REST 控制器。
 *
 * <p>提供角色权限的增删查及重复校验，路径前缀为 {@link AuthConstants#PERMISSION_PATH}。</p>
 *
 * @author zhangyukun on:2024/8/16
 */
@RestController
@RequestMapping(AuthConstants.PERMISSION_PATH)
public class PermissionControllerV3 {
    
    private final NacosRoleService nacosRoleService;
    
    /** 模糊搜索模式标识。 */
    private static final String SEARCH_TYPE_BLUR = "blur";
    
    /** 注入 {@link NacosRoleService} 处理权限持久化与查询。 */

    @Autowired
    public PermissionControllerV3(NacosRoleService nacosRoleService) {
        this.nacosRoleService = nacosRoleService;
    }
    
    /**
     * 为角色新增一条资源权限。
     *
     * @param role     the role
     * @param resource the related resource
     * @param action   the related action
     * @return 成功时返回 ok 消息
     */
    @Since("3.0.0")
    @PostMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions",
        action = ActionTypes.WRITE)
    public Result<String> createPermission(@RequestParam String role, @RequestParam String resource,
        @RequestParam String action) {
        nacosRoleService.addPermission(role, resource, action);
        return Result.success("add permission ok!");
    }
    
    /**
     * 删除角色的指定资源权限。
     *
     * @param role     the role
     * @param resource the related resource
     * @param action   the related action
     * @return 成功时返回 ok 消息
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions",
        action = ActionTypes.WRITE)
    public Result<String> deletePermission(@RequestParam String role, @RequestParam String resource,
        @RequestParam String action) {
        nacosRoleService.deletePermission(role, resource, action);
        return Result.success("delete permission ok!");
    }
    
    /**
     * 分页查询角色权限列表。
     *
     * @param role     the role
     * @param pageNo   page index
     * @param pageSize page size
     * @param search   the type of search (accurate or blur)
     * @return 分页权限数据
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions",
        action = ActionTypes.READ)
    public Result<Page<PermissionInfo>> getPermissionList(@RequestParam int pageNo,
        @RequestParam int pageSize,
        @RequestParam(name = "role", defaultValue = StringUtils.EMPTY) String role,
        @RequestParam(name = "search", defaultValue = "accurate") String search) {
        Page<PermissionInfo> permissionPage;
        if (SEARCH_TYPE_BLUR.equalsIgnoreCase(search)) {
            permissionPage = nacosRoleService.findPermissions(role, pageNo, pageSize);
        } else {
            permissionPage = nacosRoleService.getPermissions(role, pageNo, pageSize);
        }
        return Result.success(permissionPage);
    }
    
    /**
     * 判断角色下是否已存在相同资源与动作的权限。
     *
     * @param role     the role
     * @param resource the related resource
     * @param action   the related action
     * @return true if duplicate, false otherwise
     */
    @Since("3.0.0")
    @GetMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "permissions",
        action = ActionTypes.READ)
    public Result<Boolean> isDuplicatePermission(@RequestParam String role,
        @RequestParam String resource,
        @RequestParam String action) {
        return nacosRoleService.isDuplicatePermission(role, resource, action);
    }
}
