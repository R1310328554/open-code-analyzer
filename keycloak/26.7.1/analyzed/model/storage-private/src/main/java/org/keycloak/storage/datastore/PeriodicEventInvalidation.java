/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.datastore;

import org.keycloak.provider.InvalidationHandler;

/**
 * 周期性事件失效对象类型：标识需按定时任务批量清理的缓存/存储对象。
 * <p>
 * 实现 {@link InvalidationHandler.InvalidableObjectType}，供集群内缓存失效处理器识别。
 */
public enum PeriodicEventInvalidation implements InvalidationHandler.InvalidableObjectType {
    /** JPA 事件存储（用户/管理员事件）的周期性失效类型。 */
    JPA_EVENT_STORE,
}
