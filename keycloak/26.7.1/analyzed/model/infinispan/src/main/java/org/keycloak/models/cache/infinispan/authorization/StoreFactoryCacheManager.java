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
package org.keycloak.models.cache.infinispan.authorization;

import java.util.Objects;
import java.util.Set;

import org.keycloak.models.cache.infinispan.CacheManager;
import org.keycloak.models.cache.infinispan.authorization.events.AuthorizationCacheInvalidationEvent;
import org.keycloak.models.cache.infinispan.authorization.stream.InResourcePredicate;
import org.keycloak.models.cache.infinispan.authorization.stream.InResourceServerPredicate;
import org.keycloak.models.cache.infinispan.authorization.stream.InScopePredicate;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.models.cache.infinispan.events.InvalidationEvent;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * 授权 StoreFactory 的 Infinispan 缓存管理器，负责资源/作用域/策略/权限票据的失效传播。
 * <p>
 * 继承 {@link CacheManager}，将 {@link AuthorizationCacheInvalidationEvent} 展开为具体缓存键集合，
 * 并配合流式谓词（InResource/InScope/InResourceServer）批量失效关联查询结果。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class StoreFactoryCacheManager extends CacheManager {
    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(StoreFactoryCacheManager.class);

    /** 构造授权缓存管理器。 */
    public StoreFactoryCacheManager(Cache<String, Revisioned> cache, Cache<String, Long> revisions) {
        super(cache, revisions);
    }
    @Override
    protected Logger getLogger() {
        return logger;
    }

    /** 从授权失效事件提取需失效的缓存键。 */
    @Override
    protected void addInvalidationsFromEvent(InvalidationEvent event, Set<String> invalidations) {
        if (event instanceof AuthorizationCacheInvalidationEvent) {
            invalidations.add(event.getId());

            ((AuthorizationCacheInvalidationEvent) event).addInvalidations(this, invalidations);
        }
    }

    /** 资源服务器更新时失效自身及按客户端 ID 的查询缓存。 */
    public void resourceServerUpdated(String id, Set<String> invalidations) {
        invalidations.add(id);
        invalidations.add(StoreFactoryCacheSession.getResourceServerByClientCacheKey(id));
    }

    /** 资源服务器删除时额外失效所有关联 InResourceServer 谓词匹配的条目。 */
    public void resourceServerRemoval(String id, Set<String> invalidations) {
        resourceServerUpdated(id, invalidations);

        addInvalidations(InResourceServerPredicate.create(id), invalidations);
    }

    /** 作用域更新时失效按 ID、名称、关联资源与权限票据的查询缓存。 */
    public void scopeUpdated(String id, String name, String serverId, Set<String> invalidations) {
        invalidations.add(id);
        invalidations.add(StoreFactoryCacheSession.getScopeByNameCacheKey(name, serverId));
        invalidations.add(StoreFactoryCacheSession.getResourceByScopeCacheKey(id, serverId));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByScope(id, serverId));
    }

    /** 作用域删除时额外失效所有 InScope 谓词匹配的条目。 */
    public void scopeRemoval(String id, String name, String serverId, Set<String> invalidations) {
        scopeUpdated(id, name, serverId, invalidations);
        addInvalidations(InScopePredicate.create(id), invalidations);
    }

    /** 资源更新时失效按 ID、名称、所有者、类型、URI、作用域及权限票据的查询缓存。 */
    public void resourceUpdated(String id, String name, String type, Set<String> uris, Set<String> scopes, String serverId, String owner, Set<String> invalidations) {
        invalidations.add(id);
        invalidations.add(StoreFactoryCacheSession.getResourceByNameCacheKey(name, owner, serverId));
        invalidations.add(StoreFactoryCacheSession.getResourceByOwnerCacheKey(owner, serverId));
        invalidations.add(StoreFactoryCacheSession.getResourceByOwnerCacheKey(owner, null));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByResource(id, serverId));
        addInvalidations(InResourcePredicate.create(name), invalidations);

        if (type != null) {
            invalidations.add(StoreFactoryCacheSession.getResourceByTypeCacheKey(type, serverId));
            invalidations.add(StoreFactoryCacheSession.getResourceByTypeCacheKey(type, owner, serverId));
            invalidations.add(StoreFactoryCacheSession.getResourceByTypeCacheKey(type, null, serverId));
            invalidations.add(StoreFactoryCacheSession.getResourceByTypeInstanceCacheKey(type, serverId));
            addInvalidations(InResourcePredicate.create(type), invalidations);
        }

        if (uris != null) {
            for (String uri: uris) {
                invalidations.add(StoreFactoryCacheSession.getResourceByUriCacheKey(uri, serverId));
            }
        }

        if (scopes != null) {
            for (String scope : scopes) {
                invalidations.add(StoreFactoryCacheSession.getResourceByScopeCacheKey(scope, serverId));
                addInvalidations(InScopePredicate.create(scope), invalidations);
            }
        }
    }

    /** 资源删除时额外失效所有 InResource 谓词匹配的条目。 */
    public void resourceRemoval(String id, String name, String type, Set<String> uris, String owner, Set<String> scopes, String serverId, Set<String> invalidations) {
        resourceUpdated(id, name, type, uris, scopes, serverId, owner, invalidations);
        addInvalidations(InResourcePredicate.create(id), invalidations);
    }

    /** 策略更新时失效按 ID、名称、资源、资源类型与作用域的查询缓存。 */
    public void policyUpdated(String id, String name, Set<String> resources, Set<String> resourceTypes, Set<String> scopes, String serverId, Set<String> invalidations) {
        invalidations.add(id);
        invalidations.add(StoreFactoryCacheSession.getPolicyByNameCacheKey(name, serverId));

        if (resources != null) {
            for (String resource : resources) {
                invalidations.add(StoreFactoryCacheSession.getPolicyByResource(resource, serverId));
                if (Objects.nonNull(scopes)) {
                    for (String scope : scopes) {
                        invalidations.add(StoreFactoryCacheSession.getPolicyByResourceScope(scope, resource, serverId));
                    }
                }
            }
        }

        if (resourceTypes != null) {
            for (String type : resourceTypes) {
                invalidations.add(StoreFactoryCacheSession.getPolicyByResourceType(type, serverId));
            }
        }

        if (scopes != null) {
            for (String scope : scopes) {
                invalidations.add(StoreFactoryCacheSession.getPolicyByScope(scope, serverId));
                invalidations.add(StoreFactoryCacheSession.getPolicyByResourceScope(scope, null, serverId));
            }
        }
    }

    /** 权限票据更新时失效按 ID、所有者、请求者、资源与作用域的查询缓存。 */
    public void permissionTicketUpdated(String id, String owner, String requester, String resource, String resourceName, String scope, String serverId, Set<String> invalidations) {
        invalidations.add(id);
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByOwner(owner, serverId));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByResource(resource, serverId));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByGranted(requester, serverId));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByGranted(requester, null));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByResourceNameAndGranted(resourceName, requester, serverId));
        invalidations.add(StoreFactoryCacheSession.getPermissionTicketByResourceNameAndGranted(resourceName, requester, null));
        if (scope != null) {
            invalidations.add(StoreFactoryCacheSession.getPermissionTicketByScope(scope, serverId));
        }
    }

    /** 策略删除时复用更新逻辑展开失效键。 */
    public void policyRemoval(String id, String name, Set<String> resources, Set<String> resourceTypes, Set<String> scopes, String serverId, Set<String> invalidations) {
        policyUpdated(id, name, resources, resourceTypes, scopes, serverId, invalidations);
    }

    /** 权限票据删除时复用更新逻辑展开失效键。 */
    public void permissionTicketRemoval(String id, String owner, String requester, String resource, String resourceName, String scope, String serverId, Set<String> invalidations) {
        permissionTicketUpdated(id, owner, requester, resource, resourceName, scope, serverId, invalidations);
    }

}
