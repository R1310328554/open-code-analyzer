/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.spi.infinispan;

import java.util.Optional;

import org.keycloak.provider.Provider;

import org.infinispan.client.hotrod.configuration.Configuration;

/**
 * 为 Hot Rod 远程客户端提供 Infinispan 配置的 Provider。
 */
public interface CacheRemoteConfigProvider extends Provider {

    /**
     * 创建 Hot Rod 客户端的 {@link Configuration}。
     * <p>
     * 若返回非空 Optional，表示应实例化并启动 Hot Rod 客户端，且假定外部 Infinispan 集群已就绪；
     * 否则 Keycloak 启动失败。
     *
     * @return Hot Rod 客户端配置；空 Optional 表示不使用远程客户端
     */
    Optional<Configuration> configuration();

    @Override
    default void close() {
        //no-op
    }
}
