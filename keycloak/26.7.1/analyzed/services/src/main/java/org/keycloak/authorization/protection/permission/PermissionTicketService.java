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
package org.keycloak.authorization.protection.permission;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuthErrorException;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PermissionTicketStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.ScopeStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.authorization.PermissionTicketRepresentation;
import org.keycloak.services.ErrorResponseException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
/**
 * 权限票据 CRUD 与查询端点：支持所有者托管访问（OMA）场景下的细粒度授权。
 */
public class PermissionTicketService {

    /** 授权 Provider。 */
    private final AuthorizationProvider authorization;
    /** 当前调用身份。 */
    private final KeycloakIdentity identity;
    /** 所属资源服务器。 */
    private final ResourceServer resourceServer;

    /** 构造权限票据服务。 */
    public PermissionTicketService(KeycloakIdentity identity, ResourceServer resourceServer, AuthorizationProvider authorization) {
        this.identity = identity;
        this.resourceServer = resourceServer;
        this.authorization = authorization;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    /** 创建权限票据：校验资源/作用域/请求方并可选立即标记为已授予。 */
    public Response create(PermissionTicketRepresentation representation) {
        PermissionTicketStore ticketStore = authorization.getStoreFactory().getPermissionTicketStore();
        if (representation == null)
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "invalid_permission", Response.Status.BAD_REQUEST);
        if (representation.getId() != null)
            throw new ErrorResponseException("invalid_permission", "created permissions should not have id", Response.Status.BAD_REQUEST);
        if (representation.getResource() == null)
            throw new ErrorResponseException("invalid_permission", "created permissions should have resource", Response.Status.BAD_REQUEST);
        if (representation.getScope() == null && representation.getScopeName() == null)
            throw new ErrorResponseException("invalid_permission", "created permissions should have scope or scopeName", Response.Status.BAD_REQUEST);
        if (representation.getRequester() == null && representation.getRequesterName() == null)
            throw new ErrorResponseException("invalid_permission", "created permissions should have requester or requesterName", Response.Status.BAD_REQUEST);
         
        ResourceStore rstore = this.authorization.getStoreFactory().getResourceStore();
        Resource resource = rstore.findById(resourceServer, representation.getResource());
        if (resource == null ) throw new ErrorResponseException("invalid_resource_id", "Resource set with id [" + representation.getResource() + "] does not exists in this server.", Response.Status.BAD_REQUEST);
        
        if (!identity.isResourceServer() && !resource.getOwner().equals(this.identity.getId()))
            throw new ErrorResponseException("not_authorised", "permissions for [" + representation.getResource() + "] can be only created by the owner or by the resource server itself", Response.Status.FORBIDDEN);
        if (!resource.isOwnerManagedAccess())
            throw new ErrorResponseException("invalid_permission", "permission can only be created for resources with user-managed access enabled", Response.Status.BAD_REQUEST);
        
        UserModel user = null;
        if(representation.getRequester() != null)
            user = this.authorization.getKeycloakSession().users().getUserById(this.authorization.getRealm(), representation.getRequester());
        else 
            user = this.authorization.getKeycloakSession().users().getUserByUsername(this.authorization.getRealm(), representation.getRequesterName());
        
        if (user == null)
            throw new ErrorResponseException("invalid_permission", "Requester does not exists in this server as user.", Response.Status.BAD_REQUEST);
        
        Scope scope = null;
        ScopeStore sstore = this.authorization.getStoreFactory().getScopeStore();

        if(representation.getScopeName() != null)
            scope = sstore.findByName(resourceServer, representation.getScopeName());
        else
            scope = sstore.findById(resourceServer, representation.getScope());

        if (scope == null && representation.getScope() !=null )
            throw new ErrorResponseException("invalid_scope", "Scope [" + representation.getScope() + "] is invalid", Response.Status.BAD_REQUEST);
        if (scope == null && representation.getScopeName() !=null )
            throw new ErrorResponseException("invalid_scope", "Scope [" + representation.getScopeName() + "] is invalid", Response.Status.BAD_REQUEST);

        boolean match = resource.getScopes().contains(scope);

        if (!match)
           throw new ErrorResponseException("invalid_resource_id", "Resource set with id [" + representation.getResource() + "] does not have Scope [" + scope.getName() + "]", Response.Status.BAD_REQUEST);     
        
        Map<PermissionTicket.FilterOption, String> attributes = new EnumMap<>(PermissionTicket.FilterOption.class);
        attributes.put(PermissionTicket.FilterOption.RESOURCE_ID, resource.getId());
        attributes.put(PermissionTicket.FilterOption.SCOPE_ID, scope.getId());
        attributes.put(PermissionTicket.FilterOption.REQUESTER, user.getId());
        
        if (!ticketStore.find(resourceServer, attributes, null, null).isEmpty())
            throw new ErrorResponseException("invalid_permission", "Permission already exists", Response.Status.BAD_REQUEST);
        
        PermissionTicket ticket = ticketStore.create(resourceServer, resource, scope, user.getId());
        if(representation.isGranted())
                ticket.setGrantedTimestamp(java.lang.System.currentTimeMillis());
        representation = ModelToRepresentation.toRepresentation(ticket, authorization);
        return Response.ok(representation).build();
    }

    @PUT
    @Consumes("application/json")
    /** 更新已有权限票据（仅所有者可改，资源服务器亦可）。 */
    public Response update(PermissionTicketRepresentation representation) {
        if (representation == null || representation.getId() == null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "invalid_ticket", Response.Status.BAD_REQUEST);
        }

        PermissionTicket ticket = getPermissionTicket(representation.getId());

        if (!ticket.getOwner().equals(this.identity.getId()) && !this.identity.isResourceServer())
            throw new ErrorResponseException("not_authorised", "permissions for [" + representation.getResource() + "] can be updated only by the owner or by the resource server", Response.Status.FORBIDDEN);

        RepresentationToModel.toModel(representation, resourceServer, authorization);

        return Response.noContent().build();
    }

    
    @Path("{id}")
    @DELETE
    @Consumes("application/json")
    /** 删除权限票据；所有者可删，请求方亦可删除自身票据。 */
    public Response delete(@PathParam("id") String id) {
        if (id == null) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "invalid_ticket", Response.Status.BAD_REQUEST);
        }

        PermissionTicket ticket = getPermissionTicket(id);

        if (!ticket.getOwner().equals(this.identity.getId()) && !this.identity.isResourceServer() && !ticket.getRequester().equals(this.identity.getId()))
            throw new ErrorResponseException("not_authorised", "permissions for [" + ticket.getResource() + "] can be deleted only by the owner, the requester, or the resource server", Response.Status.FORBIDDEN);

        PermissionTicketStore ticketStore = authorization.getStoreFactory().getPermissionTicketStore();

        ticketStore.delete(id);

        return Response.noContent().build();
    }

    /** 按 ID 加载票据，不存在或不属于当前资源服务器时抛出 400。 */
    private PermissionTicket getPermissionTicket(String id) {
        PermissionTicketStore ticketStore = authorization.getStoreFactory().getPermissionTicketStore();
        PermissionTicket ticket = ticketStore.findById(resourceServer, id);

        if (ticket == null || !ticket.getResourceServer().equals(resourceServer)) {
            throw new ErrorResponseException(OAuthErrorException.INVALID_REQUEST, "invalid_ticket", Response.Status.BAD_REQUEST);
        }

        return ticket;
    }

    @GET
    @Produces("application/json")
    /** 按资源/作用域/所有者/请求方/授予状态等条件分页查询票据。 */
    public Response find(@QueryParam("scopeId") String scopeId,
                         @QueryParam("resourceId") String resourceId,
                         @QueryParam("owner") String owner,
                         @QueryParam("requester") String requester,
                         @QueryParam("granted") Boolean granted,
                         @QueryParam("returnNames") Boolean returnNames,
                         @QueryParam("first") Integer firstResult,
                         @QueryParam("max") Integer maxResult) {
        StoreFactory storeFactory = authorization.getStoreFactory();
        PermissionTicketStore permissionTicketStore = storeFactory.getPermissionTicketStore();

        Map<PermissionTicket.FilterOption, String> filters = getFilters(storeFactory, resourceId, scopeId, owner, requester, granted);

        return Response.ok().entity(permissionTicketStore.find(resourceServer, filters, firstResult != null ? firstResult : -1, maxResult != null ? maxResult : Constants.DEFAULT_MAX_RESULTS)
                    .stream()
                        .map(permissionTicket -> ModelToRepresentation.toRepresentation(permissionTicket, authorization, returnNames == null ? false : returnNames))
                        .collect(Collectors.toList()))
                .build();
    }

    @Path("/count")
    @GET
    @Produces("application/json")
    /** 返回符合过滤条件的权限票据数量。 */
    public Response getPermissionCount(@QueryParam("scopeId") String scopeId,
                                       @QueryParam("resourceId") String resourceId,
                                       @QueryParam("owner") String owner,
                                       @QueryParam("requester") String requester,
                                       @QueryParam("granted") Boolean granted,
                                       @QueryParam("returnNames") Boolean returnNames) {
        StoreFactory storeFactory = authorization.getStoreFactory();
        PermissionTicketStore permissionTicketStore = storeFactory.getPermissionTicketStore();
        Map<PermissionTicket.FilterOption, String> filters = getFilters(storeFactory, resourceId, scopeId, owner, requester, granted);
        long count = permissionTicketStore.count(resourceServer, filters);

        return Response.ok().entity(count).build();
    }

    /** 将查询参数转换为 {@link PermissionTicket.FilterOption} 过滤映射。 */
    private Map<PermissionTicket.FilterOption, String> getFilters(StoreFactory storeFactory,
                                           String resourceId,
                                           String scopeId,
                                           String owner,
                                           String requester,
                                           Boolean granted) {
        Map<PermissionTicket.FilterOption, String> filters = new EnumMap<>(PermissionTicket.FilterOption.class);

        if (resourceId != null) {
            filters.put(PermissionTicket.FilterOption.RESOURCE_ID, resourceId);
        }

        if (scopeId != null) {
            ScopeStore scopeStore = storeFactory.getScopeStore();
            Scope scope = scopeStore.findById(resourceServer, scopeId);

            if (scope == null) {
                scope = scopeStore.findByName(resourceServer, scopeId);
            }

            filters.put(PermissionTicket.FilterOption.SCOPE_ID, scope != null ? scope.getId() : scopeId);
        }

        if (owner != null) {
            filters.put(PermissionTicket.FilterOption.OWNER, getUserId(owner));
        }

        if (requester != null) {
            filters.put(PermissionTicket.FilterOption.REQUESTER, getUserId(requester));
        }

        if (granted != null) {
            filters.put(PermissionTicket.FilterOption.GRANTED, granted.toString());
        }

        return filters;
    }

    /** 将用户名或 ID 解析为领域用户 ID。 */
    private String getUserId(String userIdOrName) {
        UserProvider userProvider = authorization.getKeycloakSession().users();
        RealmModel realm = authorization.getRealm();
        UserModel userModel = userProvider.getUserById(realm, userIdOrName);

        if (userModel != null) {
            return userModel.getId();
        }

        userModel = userProvider.getUserByUsername(realm, userIdOrName);

        if (userModel != null) {
            return userModel.getId();
        }

        return userIdOrName;
    }
}
