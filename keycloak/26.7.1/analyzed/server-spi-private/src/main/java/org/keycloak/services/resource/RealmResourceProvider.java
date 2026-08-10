/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services.resource;

import org.keycloak.provider.Provider;

/**
 * 领域 REST API 扩展提供者：为服务器无法解析的领域 REST 路径创建 JAX-RS 子资源实例。
 * <p>实现 {@link #getResource()} 返回对应路径的 JAX-RS 资源对象。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface RealmResourceProvider extends Provider {

    /**
     * 返回 JAX-RS 子资源实例。
     *
     * @return a JAX-RS sub-resource instance
     */
    Object getResource();

}
