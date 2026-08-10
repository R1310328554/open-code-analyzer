/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.resources.account.resources;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.Auth;
import org.keycloak.utils.MediaType;

/**
 * 单条 UMA 资源的权限管理：查询/授予/撤销及待审批请求。
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceService extends AbstractResourceService {

    private final org.keycloak.authorization.model.Resource resource;
    private final ResourceServer resourceServer;

    ResourceService(org.keycloak.authorization.model.Resource resource, KeycloakSession session, UserModel user,
            Auth auth, HttpRequest request) {
        super(session, user, auth, request);
        this.resource = resource;
        this.resourceServer = resource.getResourceServer();
    }

    /**
     * 返回 {@link #user} 作为所有者的 {@link Resource} 表示。
     * 
     * @return 资源 DTO
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Resource getResource() {
        return new Resource(resource, provider);
    }

    /**
     * 返回 {@link #user} 已授予访问权限的用户列表。
     * 
     * @return 已授权用户权限集合
     */
    @GET
    @Path("permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Permission> toPermissions() {
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        filters.put(PermissionTicket.FilterOption.OWNER, user.getId());
        filters.put(PermissionTicket.FilterOption.GRANTED, Boolean.TRUE.toString());
        filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());

        Collection<ResourcePermission> resources = toPermissions(ticketStore.find(resourceServer, filters, null, null));
        Collection<Permission> permissions = Collections.EMPTY_LIST;
        
        if (!resources.isEmpty()) {
            permissions = resources.iterator().next().getPermissions();
        }

        return permissions;
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    /** 按用户名或邮箱查询用户（需为本人或存在权限请求关系） */
    public Response user(@QueryParam("value") String value) {
        try {
            final UserModel queriedUser = getUser(value);
            final UserModel authenticatedUser = auth.getUser();

            if (!queriedUser.getId().equals(authenticatedUser.getId()) && !hasPermissionRequest(queriedUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            UserRepresentation minimalUserRep = new UserRepresentation();
            minimalUserRep.setId(queriedUser.getId());
            minimalUserRep.setUsername(queriedUser.getUsername());
            minimalUserRep.setFirstName(queriedUser.getFirstName());
            minimalUserRep.setLastName(queriedUser.getLastName());
            minimalUserRep.setEmail(queriedUser.getEmail());

            return Response.ok(minimalUserRep).build();
        } catch (NotFoundException e) {
            return Response.noContent().build();
        }
    }

    private boolean hasPermissionRequest(String userId) {
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());
        filters.put(PermissionTicket.FilterOption.REQUESTER, userId);

        return !ticketStore.find(resourceServer, filters, null, null).isEmpty();
    }

    /**
     * 根据给定权限列表更新资源的授权集合（授予或撤销 scope）。
     *
     * @param permissions 待更新的权限
     * @return 成功时 204 No Content
     */
    @PUT
    @Path("permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revoke(List<Permission> permissions) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        if (permissions == null || permissions.isEmpty()) {
            throw new BadRequestException("invalid_permissions");    
        }
        
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());


        for (Permission permission : permissions) {
            UserModel user = getUser(permission.getUsername());

            filters.put(PermissionTicket.FilterOption.REQUESTER, user.getId());

            List<PermissionTicket> tickets = ticketStore.find(resourceServer, filters, null, null);

            // 无现有 ticket 时授予全部请求 scope
            if (tickets.isEmpty()) {
                for (String scope : permission.getScopes()) {
                    grantPermission(user, scope);
                }
            } else {
                Iterator<String> scopesIterator = permission.getScopes().iterator();

                while (scopesIterator.hasNext()) {
                    org.keycloak.authorization.model.Scope scope = getScope(scopesIterator.next(), resourceServer);
                    Iterator<PermissionTicket> ticketIterator = tickets.iterator();

                    while (ticketIterator.hasNext()) {
                        PermissionTicket ticket = ticketIterator.next();

                        if (scope.getId().equals(ticket.getScope().getId())) {
                            if (!ticket.isGranted()) {
                                ticket.setGrantedTimestamp(System.currentTimeMillis());
                            }
                            // 已存在授权，从待删列表移除
                            ticketIterator.remove();
                            // scope 已授予，避免重复创建
                            scopesIterator.remove();
                        }
                    }
                }

                // 仅为尚无 ticket 的 scope 创建权限
                for (String scope : permission.getScopes()) {
                    grantPermission(user, scope);
                }
                
                // 删除不在请求权限内的 ticket
                for (PermissionTicket ticket : tickets) {
                    ticketStore.delete(ticket.getId());
                }                
            }
        }

        return Response.noContent().build();
    }

    /**
     * 返回待 {@link #user} 审批的权限请求列表。
     *
     * @return 待审批权限请求
     */
    @GET
    @Path("permissions/requests")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Permission> getPermissionRequests() {
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        filters.put(PermissionTicket.FilterOption.OWNER, user.getId());
        filters.put(PermissionTicket.FilterOption.GRANTED, Boolean.FALSE.toString());
        filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());
        
        Map<String, Permission> requests = new HashMap<>();

        for (PermissionTicket ticket : ticketStore.find(resourceServer, filters, null, null)) {
            requests.computeIfAbsent(ticket.getRequester(), requester -> new Permission(ticket, provider)).addScope(ticket.getScope().getName());
        }
        
        return requests.values();
    }

    /** 为用户授予指定 scope 的 permission ticket */
    private void grantPermission(UserModel user, String scopeId) {
        org.keycloak.authorization.model.Scope scope = getScope(scopeId, resourceServer);
        PermissionTicket ticket = ticketStore.create(resourceServer, resource, scope, user.getId());
        ticket.setGrantedTimestamp(Calendar.getInstance().getTimeInMillis());
    }

    /** 按名称或 ID 解析 scope */
    private org.keycloak.authorization.model.Scope getScope(String scopeId, ResourceServer resourceServer) {
        org.keycloak.authorization.model.Scope scope = scopeStore.findByName(resourceServer, scopeId);

        if (scope == null) {
            scope = scopeStore.findById(resourceServer, scopeId);
        }
        
        return scope;
    }

    /** 按用户名或邮箱解析用户，歧义或不存在时抛异常 */
    private UserModel getUser(String requester) {
        UserProvider users = provider.getKeycloakSession().users();
        UserModel userByUsername = users.getUserByUsername(provider.getRealm(), requester);
        UserModel userByEmail = users.getUserByEmail(provider.getRealm(), requester);

        if (userByUsername != null && userByEmail != null
                && !userByUsername.getId().equals(userByEmail.getId())) {
            throw new BadRequestException("ambiguous_user");
        }

        UserModel user = userByUsername != null ? userByUsername : userByEmail;

        if (user == null) {
            throw new NotFoundException(requester);
        }

        return user;
    }

    private Collection<ResourcePermission> toPermissions(List<PermissionTicket> tickets) {
        Map<String, ResourcePermission> permissions = new HashMap<>();

        for (PermissionTicket ticket : tickets) {
            ResourcePermission resource = permissions
                    .computeIfAbsent(ticket.getResource().getId(), s -> new ResourcePermission(ticket, provider));

            Permission user = resource.getPermission(ticket.getRequester());

            if (user == null) {
                resource.addPermission(ticket.getRequester(), user = new Permission(ticket.getRequester(), provider));
            }

            user.addScope(ticket.getScope().getName());
        }

        return permissions.values();
    }
}
