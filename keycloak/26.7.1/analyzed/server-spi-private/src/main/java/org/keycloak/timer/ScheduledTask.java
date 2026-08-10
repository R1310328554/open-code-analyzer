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

package org.keycloak.timer;

import org.keycloak.models.KeycloakSessionTask;

/**
 * 定时任务接口：在 Keycloak 会话上下文中执行的周期性任务。
 * <p>继承 {@link KeycloakSessionTask}，默认以类名作为任务名称。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ScheduledTask extends KeycloakSessionTask {

    /** @return 任务名称，默认为实现类简单名 */
    default String getTaskName() {
        return getClass().getSimpleName();
    }

}
