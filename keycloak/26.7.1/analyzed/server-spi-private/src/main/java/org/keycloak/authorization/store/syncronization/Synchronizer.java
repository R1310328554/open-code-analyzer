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

package org.keycloak.authorization.store.syncronization;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderEvent;

/**
 * 授权数据同步器接口：响应 {@link ProviderEvent} 并清理关联授权持久化数据。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Synchronizer<E extends ProviderEvent> {

    /** 处理领域模型删除事件，同步更新授权存储。 */
    void synchronize(E event, KeycloakSessionFactory factory);

}
