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
package org.keycloak.storage.federated;

import org.keycloak.provider.ProviderFactory;

/**
 * 用户联邦存储 Provider 的工厂接口，负责创建 {@link UserFederatedStorageProvider} 实例。
 * <p>
 * 联邦存储用于持久化外部用户存储无法直接承载的数据（属性、角色映射、Broker 链接、同意书等）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserFederatedStorageProviderFactory extends ProviderFactory<UserFederatedStorageProvider> {
}
