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
import com.alibaba.nacos.plugin.auth.impl.persistence.RoleInfo;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V3 角色管理 REST 控制器。
 *
 * <p>负责用户角色绑定、删除及分页/模糊查询，路径前缀为 {@link AuthConstants#ROLE_PATH}。</p>
 *
 * @author zhangyukun on:2024/8/16
 */
@RestController
@RequestMapping(AuthConstants.ROLE_PATH)
public class RoleControllerV3 {
    
    private final NacosRoleService roleService;
    
    /** 模糊搜索模式标识。 */
    private static final String SEARCH_TYPE_BLUR = "blur";
    
    /** 注入 {@link NacosRoleService}。 */
    public RoleControllerV3(NacosRoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
     * 为用户绑定角色。
     *
     * <p>兼作：创建角色并绑定 GLOBAL_ADMIN，或为普通用户分配角色。</p>
     *
     * @param role     role name
     * @param username username
     * @return Code 200 and message 'add role ok!'
     */
    @Since("3.0.0")
    @PostMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "roles",
        action = ActionTypes.WRITE)
    public Result<String> createRole(@RequestParam String role, @RequestParam String username) {
        roleService.addRole(role, username);
        return Result.success("add role ok!");
    }
    
    /**
     * 删除角色绑定；未指定 username 时删除该角色下全部绑定。
     *
     * @param role     role
     * @param username username
     * @return ok if succeed
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "roles",
        action = ActionTypes.WRITE)
    public Result<String> deleteRole(@RequestParam String role,
        @RequestParam(name = "username", defaultValue = StringUtils.EMPTY) String username) {
        if (StringUtils.isBlank(username)) {
            roleService.deleteRole(role);
        } else {
            roleService.deleteRole(role, username);
        }
        return Result.success("delete role of user " + username + " ok!");
    }
    
    /**
     * 分页查询角色列表，支持精确或模糊搜索。
     *
     * @param pageNo   number index of page
     * @param pageSize page size
     * @param username optional, username of user
     * @param role     optional role
     * @param search   the type of search: "accurate" for exact match, "blur" for fuzzy match
     * @return role list
     */
    @Since("3.0.0")
    @GetMapping("/list")
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "roles",
        action = ActionTypes.READ)
    public Result<Page<RoleInfo>> getRoleList(@RequestParam int pageNo, @RequestParam int pageSize,
        @RequestParam(name = "username", defaultValue = "") String username,
        @RequestParam(name = "role", defaultValue = "") String role,
        @RequestParam(name = "search", required = false, defaultValue = "accurate") String search) {
        Page<RoleInfo> rolePage;
        if (SEARCH_TYPE_BLUR.equalsIgnoreCase(search)) {
            rolePage = roleService.findRoles(username, role, pageNo, pageSize);
        } else {
            rolePage = roleService.getRoles(username, role, pageNo, pageSize);
        }
        return Result.success(rolePage);
    }
    
    /**
     * 按角色名模糊匹配，返回角色名列表。
     *
     * @param role role id
     * @return role list
     */
    @Since("3.0.0")
    @GetMapping("/search")
    @Secured(resource = AuthConstants.CONSOLE_RESOURCE_NAME_PREFIX + "roles",
        action = ActionTypes.READ)
    public Result<List<String>> getRoleListByRoleName(@RequestParam String role) {
        List<String> roles = roleService.findRoleNames(role);
        return Result.success(roles);
    }
}
