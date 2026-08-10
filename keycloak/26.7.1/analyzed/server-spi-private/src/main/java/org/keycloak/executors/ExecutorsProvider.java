/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.executors;

import java.util.concurrent.ExecutorService;

import org.keycloak.provider.Provider;

/**
 * 线程池执行器 SPI：按任务类型返回 {@link ExecutorService}，供过期清理等后台任务使用。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ExecutorsProvider extends Provider {

    /** 会话/令牌过期清理等定时任务使用的执行器类型键。 */
    String EXPIRATION_TASKS = "expiration-tasks";

    /**
     * 按任务类型获取线程池；不同 {@code taskType} 通常映射不同执行器。
     *
     * @param taskType 任务类型标识（如 {@link #EXPIRATION_TASKS}）
     * @return 对应 {@link ExecutorService}
     */
    ExecutorService getExecutor(String taskType);

}
