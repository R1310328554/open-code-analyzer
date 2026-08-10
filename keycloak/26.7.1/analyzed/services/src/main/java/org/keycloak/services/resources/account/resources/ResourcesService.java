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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.store.PermissionTicketStore;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.Auth;
import org.keycloak.utils.MediaType;

/**
 * 账户控制台授权资源 REST 服务。
 * <p>提供当前用户拥有的资源、共享资源及待处理权限请求的查询与分页。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourcesService extends AbstractResourceService {

    public ResourcesService(KeycloakSession session, UserModel user, Auth auth, HttpRequest request) {
        super(session, user, auth, request);
    }

    /**
     * 返回当前 {@link #user} 作为资源所有者的 {@link Resource} 列表。
     *
     * @param first 分页起始索引
     * @param max   分页最大条数
     * @return 用户拥有的资源列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResources(@QueryParam("name") String name,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max) {
        Map<org.keycloak.authorization.model.Resource.FilterOption, String[]> filters =
                new EnumMap<>(org.keycloak.authorization.model.Resource.FilterOption.class);

        filters.put(org.keycloak.authorization.model.Resource.FilterOption.OWNER, new String[] { user.getId() });

        if (name != null) {
            filters.put(org.keycloak.authorization.model.Resource.FilterOption.NAME, new String[] { name });
        }

        return queryResponse((f, m) -> resourceStore.find(null, filters, f, m).stream()
                .map(resource -> new Resource(resource, user, provider)), first, max);
    }

    /**
     * 返回与 {@link #user} 共享的 {@link Resource} 列表。
     *
     * @param first 分页起始索引
     * @param max 分页最大条数
     * @return 共享给当前用户的资源列表
     */
    @GET
    @Path("shared-with-me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSharedWithMe(@QueryParam("name") String name,
            @QueryParam("first") Integer first,
            @QueryParam("max") Integer max) {
        return queryResponse((f, m) -> toPermissions(ticketStore.findGrantedResources(auth.getUser().getId(), name, f, m), false)
                .stream(), first, max);
    }

    /**
     * 返回当前用户拥有且已共享给其他用户的 {@link Resource} 列表。
     *
     * @param first 分页起始索引
     * @param max 分页最大条数
     * @return 用户拥有并已共享的资源列表
     */
    @GET
    @Path("shared-with-others")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSharedWithOthers(@QueryParam("first") Integer first, @QueryParam("max") Integer max) {
        return queryResponse(
                (f, m) -> toPermissions(ticketStore.findGrantedOwnerResources(auth.getUser().getId(), f, m), true)
                        .stream(), first, max);
    }

    /** 返回当前用户待处理的权限请求列表。 */
    @GET
    @Path("pending-requests")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPendingRequests() {
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        filters.put(PermissionTicket.FilterOption.REQUESTER, user.getId());
        filters.put(PermissionTicket.FilterOption.GRANTED, Boolean.FALSE.toString());

        final List<PermissionTicket> permissionTickets = ticketStore.find(null, filters, null, null);

        final List<ResourcePermission> resourceList = new ArrayList<>(permissionTickets.size());
        for (PermissionTicket ticket : permissionTickets) {
            ResourcePermission resourcePermission = new ResourcePermission(ticket.getResource(), provider);
            resourcePermission.addScope(new Scope(ticket.getScope()));
            resourceList.add(resourcePermission);
        }

        return queryResponse(
                (f, m) -> resourceList.stream(), -1, resourceList.size());
    }

    /** 按 ID 获取单个资源的子服务端点（需为资源所有者）。 */
    @Path("{id}")
    public Object getResource(@PathParam("id") String id) {
        org.keycloak.authorization.model.Resource resource = resourceStore.findById(null, id);

        if (resource == null) {
            throw new NotFoundException("resource_not_found");
        }

        if (!resource.getOwner().equals(user.getId())) {
            throw new BadRequestException("invalid_resource");
        }
        
        return new ResourceService(resource, provider.getKeycloakSession(), user, auth, request);
    }

    /** 将授权资源列表转换为 {@link ResourcePermission} 集合并附加权限票据信息。 */
    private Collection<ResourcePermission> toPermissions(List<org.keycloak.authorization.model.Resource> resources, boolean withRequesters) {
        Collection<ResourcePermission> permissions = new ArrayList<>();
        PermissionTicketStore ticketStore = provider.getStoreFactory().getPermissionTicketStore();

        for (org.keycloak.authorization.model.Resource resource : resources) {
            ResourcePermission permission = new ResourcePermission(resource, provider);

            List<PermissionTicket> tickets;

            if (withRequesters) {
                Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

                filters.put(PermissionTicket.FilterOption.OWNER, user.getId());
                filters.put(PermissionTicket.FilterOption.GRANTED, Boolean.TRUE.toString());
                filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());

                tickets = ticketStore.find(resource.getResourceServer(), filters, null, null);
            } else {
                tickets = ticketStore.findGranted(resource.getResourceServer(), resource.getName(), user.getId());
            }

            for (PermissionTicket ticket : tickets) {
                if (resource.equals(ticket.getResource())) {
                    if (withRequesters) {
                        Permission user = permission.getPermission(ticket.getRequester());

                        if (user == null) {
                            permission.addPermission(ticket.getRequester(),
                                    user = new Permission(ticket.getRequester(), provider));
                        }

                        user.addScope(ticket.getScope().getName());
                    } else {
                        permission.addScope(new Scope(ticket.getScope()));
                    }
                }
            }

            permissions.add(permission);
        }

        return permissions;
    }
    
    /** 执行分页查询并构建带 next/prev 链接的 JSON 响应。 */
    private Response queryResponse(BiFunction<Integer, Integer, Stream<?>> query, Integer first, Integer max) {
        if (first != null && max != null) {
            List result = query.apply(first, max + 1).collect(Collectors.toList());
            int size = result.size();

            if (size > max) {
                result = result.subList(0, size - 1);
            }

            return Response.ok().entity(result).links(createPageLinks(first, max, size)).build();
        }

        return Response.ok().entity(query.apply(-1, -1).collect(Collectors.toList())).build();
    }

    /** 根据分页参数生成分页导航 Link 数组。 */
    private Link[] createPageLinks(Integer first, Integer max, int resultSize) {
        if (resultSize == 0 || (first == 0 && resultSize <= max)) {
            return new Link[] {};
        }

        List<Link> links = new ArrayList();
        boolean nextPage = resultSize > max;

        if (nextPage) {
            links.add(Link.fromUri(
                    KeycloakUriBuilder.fromUri(uriInfo.getRequestUri()).replaceQuery("first={first}&max={max}")
                            .build(first + max, max))
                    .rel("next").build());
        }

        if (first > 0) {
            links.add(Link.fromUri(
                    KeycloakUriBuilder.fromUri(uriInfo.getRequestUri()).replaceQuery("first={first}&max={max}")
                            .build(Math.max(first - max, 0), max))
                    .rel("prev").build());
        }

        return links.toArray(new Link[links.size()]);
    }
}
