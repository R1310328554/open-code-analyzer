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
package org.keycloak.provider;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 异常转换器：解包 JTA 提交等场景下的包装异常，还原真实根因。
 * <p>Use to unwrap exceptions specifically if there is an exception at JTA commit</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ExceptionConverter extends Provider, ProviderFactory<ExceptionConverter> {

    /**
     * 尝试转换异常；无法处理时返回 {@code null}。
     * Return null if the provider doesn't handle this type
     *
     * @param t
     * @return
     */
    Throwable convert(Throwable t);

    /** 单例模式：直接返回自身。 */
    @Override
    default ExceptionConverter create(KeycloakSession session) {
        return this;
    }

    /** 默认无初始化逻辑。 */
    @Override
    default void init(Config.Scope config) {

    }

    /** 默认无后置初始化逻辑。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {

    }

    /** 默认无关闭资源逻辑。 */
    @Override
    default void close() {

    }



}
