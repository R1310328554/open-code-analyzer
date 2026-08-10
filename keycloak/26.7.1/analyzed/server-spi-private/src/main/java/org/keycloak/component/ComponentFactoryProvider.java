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
package org.keycloak.component;

import org.keycloak.provider.Provider;

/**
 * 组件工厂 SPI 的会话级 {@link Provider} 标记接口。
 * <p>实际工厂逻辑由 {@link ComponentFactoryProviderFactory} 提供。</p>
 *
 * @author hmlnarik
 */
public interface ComponentFactoryProvider extends Provider {
    @Override
    default void close() {};
}
