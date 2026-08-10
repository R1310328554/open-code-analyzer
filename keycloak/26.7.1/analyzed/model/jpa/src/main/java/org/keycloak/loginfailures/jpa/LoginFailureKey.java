/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.loginfailures.jpa;

import java.io.Serializable;
import java.util.Objects;

/**
 * {@link LoginFailureEntity} 的复合主键：(realmId, userId)。
 * <p>
 * 作为 JPA {@code @IdClass} 与 Map 缓存键，record 构造器强制非 null。
 */
public record LoginFailureKey(String realmId, String userId) implements Serializable {
    public LoginFailureKey {
        Objects.requireNonNull(realmId);
        Objects.requireNonNull(userId);
    }
}
