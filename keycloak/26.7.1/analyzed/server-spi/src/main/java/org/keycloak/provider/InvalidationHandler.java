/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.provider;

import org.keycloak.models.KeycloakSession;

/**
 * 失效处理器：由缓存外部对象变更的 Provider 实现，收到失效请求后清除缓存。
 * <p>实现者仅应响应失效请求，不应主动发起相同对象的失效（避免无限循环）。</p>
 * Handles invalidation requests. This interface is specifically implemented by
 * providers that implement a cache of objects that might change in the outside.
 * <p>
 * Note that implementors are expected to react to invalidation requests:
 * invalidate the objects in the cache. They should <b>not</b> initiate
 * invalidation of the same objects neither locally nor via network - that
 * could result in an infinite loop.
 *
 * @author hmlnarik
 */
public interface InvalidationHandler {

    /**
     * 可失效对象类型的标记接口。
     * Tagging interface for the kinds of invalidatable object
     */
    public interface InvalidableObjectType {}

    /** 可失效对象类型枚举。 */
    public enum ObjectType implements InvalidableObjectType {
        /** 全部 */ _ALL_, /** realm */ REALM, /** 客户端 */ CLIENT, /** 客户端范围 */ CLIENT_SCOPE, /** 用户 */ USER, /** 角色 */ ROLE, /** 组 */ GROUP, /** 组件 */ COMPONENT, /** Provider 工厂 */ PROVIDER_FACTORY
    }

    /**
     * 使给定对象的中间缓存状态失效。
     * Invalidates intermediate states of the given objects
     * @param session KeycloakSession
     * @param type Type of the objects to invalidate
     * @param params Parameters used for the invalidation
     */
    void invalidate(KeycloakSession session, InvalidableObjectType type, Object... params);

}
