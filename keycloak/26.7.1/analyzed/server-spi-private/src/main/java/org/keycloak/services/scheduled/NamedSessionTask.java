/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.services.scheduled;

import org.keycloak.models.KeycloakSessionTask;

/**
 * 具名会话任务抽象基类：为 {@link KeycloakSessionTask} 提供固定任务名称。
 * <p>子类实现 {@link #run(KeycloakSession)} 定义具体逻辑。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class NamedSessionTask implements KeycloakSessionTask {

    private final String taskName;

    /** @param taskName 任务名称，由 {@link #getTaskName()} 返回 */
    public NamedSessionTask(String taskName) {
        this.taskName = taskName;
    }

    /** @return 构造时指定的任务名称 */
    @Override
    public String getTaskName() {
        return taskName;
    }
}
