/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.group;

import org.keycloak.provider.Provider;

/**
 * 组存储 Provider 接口，扩展 {@link GroupLookupProvider} 以支持外部组存储实现。
 *
 * <p>实现此接口的 Provider 可从 LDAP 等外部源读取组数据，与本地 {@link org.keycloak.models.GroupProvider} 协同工作。
 */
public interface GroupStorageProvider extends Provider, GroupLookupProvider {
}
