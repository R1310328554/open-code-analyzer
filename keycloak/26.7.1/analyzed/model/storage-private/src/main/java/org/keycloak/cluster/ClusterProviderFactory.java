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

package org.keycloak.cluster;

import org.keycloak.provider.ProviderFactory;

/**
 * 集群 Provider 工厂 SPI 接口，用于创建 {@link ClusterProvider} 实例。
 * <p>
 * 实现类负责在 Keycloak 集群节点间注册协调后端（如 Infinispan、JPA 事件存储等）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClusterProviderFactory extends ProviderFactory<ClusterProvider> {
}
