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

package org.keycloak.expiration.jpa;

/**
 * 周期性从数据库删除过期条目的任务接口。
 * <p>
 * 通过 {@link ExpirationTaskBuilder} 构建实例，并调用 {@link #schedule()} 注册到
 * {@link org.keycloak.timer.TimerProvider}。
 * </p>
 * <p>
 * 每次 {@link #run()} 将清理工作提交到 {@link java.util.concurrent.Executor}，
 * 避免长时间删除阻塞定时器线程；若前一次运行尚未结束，新触发会被跳过。
 * </p>
 *
 * @see ExpirationTaskBuilder
 * @see ExpirationAction
 */
public interface ExpirationTask extends Runnable {

    /**
     * 在配置的间隔周期内，将此任务注册到 {@link org.keycloak.timer.TimerProvider}。
     */
    void schedule();

    /**
     * 返回用于配置并构建 {@link ExpirationTask} 的构建器。
     */
    static ExpirationTaskBuilder builder() {
        return new ExpirationTaskBuilder();
    }
}
