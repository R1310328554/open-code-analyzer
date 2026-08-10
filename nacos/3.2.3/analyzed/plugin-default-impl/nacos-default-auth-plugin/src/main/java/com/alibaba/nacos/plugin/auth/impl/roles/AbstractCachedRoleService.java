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

package com.alibaba.nacos.plugin.auth.impl.roles;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionInfo;
import com.alibaba.nacos.plugin.auth.impl.persistence.RoleInfo;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos 角色服务抽象基类：定时从持久层加载角色与权限并维护内存缓存。
 *
 * <p>子类实现 {@link #getAllRoles()} 等数据访问；{@link #reload()} 每 15 秒刷新缓存。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractCachedRoleService implements NacosRoleService {
    
    /** 分页查询默认页码。 */
    protected static final int DEFAULT_PAGE_NO = 1;
    
    /** 已加载的全部角色名集合。 */
    private volatile Set<String> roleSet = new ConcurrentHashSet<>();
    
    /** 用户名 → 角色绑定列表。 */
    private volatile Map<String, List<RoleInfo>> roleInfoMap = new ConcurrentHashMap<>();
    
    /** 角色名 → 权限列表。 */
    private volatile Map<String, List<PermissionInfo>> permissionInfoMap =
        new ConcurrentHashMap<>();
    
    protected Set<String> getCachedRoleSet() {
        return roleSet;
    }
    
    protected Map<String, List<RoleInfo>> getCachedRoleInfoMap() {
        return roleInfoMap;
    }
    
    protected Map<String, List<PermissionInfo>> getCachedPermissionInfoMap() {
        return permissionInfoMap;
    }
    
    /** 定时刷新角色与权限缓存（启动 5 秒后首次，之后每 15 秒）。 */
    @Scheduled(initialDelay = 5000, fixedDelay = 15000)
    protected void reload() {
        try {
            // 拉取全部角色绑定并重建用户→角色映射
            List<RoleInfo> roleInfoPage = getAllRoles();
            Set<String> tmpRoleSet = new HashSet<>(16);
            Map<String, List<RoleInfo>> tmpRoleInfoMap = new ConcurrentHashMap<>(16);
            for (RoleInfo roleInfo : roleInfoPage) {
                if (!tmpRoleInfoMap.containsKey(roleInfo.getUsername())) {
                    tmpRoleInfoMap.put(roleInfo.getUsername(), new ArrayList<>());
                }
                tmpRoleInfoMap.get(roleInfo.getUsername()).add(roleInfo);
                tmpRoleSet.add(roleInfo.getRole());
            }
            
            Map<String, List<PermissionInfo>> tmpPermissionInfoMap = new ConcurrentHashMap<>(16);
            // 为每个角色加载全部权限
            for (String role : tmpRoleSet) {
                Page<PermissionInfo> permissionInfoPage =
                    getPermissions(role, DEFAULT_PAGE_NO, Integer.MAX_VALUE);
                tmpPermissionInfoMap.put(role, permissionInfoPage.getPageItems());
            }
            
            roleSet = tmpRoleSet;
            roleInfoMap = tmpRoleInfoMap;
            permissionInfoMap = tmpPermissionInfoMap;
        } catch (Exception e) {
            Loggers.AUTH.warn("[LOAD-ROLES] load failed", e);
        }
    }
}
