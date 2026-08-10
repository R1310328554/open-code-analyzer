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

package org.keycloak.timer.basic;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.timer.TimerProvider;
import org.keycloak.timer.TimerProviderFactory;

/**
 * 基础 {@link TimerProviderFactory} 实现，使用 {@link Timer} 调度后台定时任务。
 * <p>维护已注册任务名称到 {@link TimerTaskContextImpl} 的映射，可通过 {@link #TRANSACTION_TIMEOUT} 配置事务超时。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class BasicTimerProviderFactory implements TimerProviderFactory {

    /** 底层 {@link Timer} 实例。 */
    private Timer timer;

    /** 定时任务关联的事务超时（毫秒），0 表示不限制。 */
    private int transactionTimeout;

    /** 配置项键名：事务超时时间。 */
    public static final String TRANSACTION_TIMEOUT = "transactionTimeout";

    /** 已调度任务的名称到上下文映射。 */
    private ConcurrentMap<String, TimerTaskContextImpl> scheduledTasks = new ConcurrentHashMap<>();

    /** 为会话创建 {@link BasicTimerProvider} 实例。 */
    @Override
    public TimerProvider create(KeycloakSession session) {
        return new BasicTimerProvider(session, timer, transactionTimeout, this);
    }

    /** 读取配置并启动守护线程 {@link Timer}。 */
    @Override
    public void init(Config.Scope config) {
        transactionTimeout = config.getInt(TRANSACTION_TIMEOUT, 0);
        timer = new Timer(true);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** 关闭 {@link Timer} 并释放资源。 */
    @Override
    public void close() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public String getId() {
        return "basic";
    }

    /** 注册或替换指定名称的定时任务上下文。 */
    protected TimerTaskContextImpl putTask(String taskName, TimerTaskContextImpl task) {
        return scheduledTasks.put(taskName, task);
    }

    /** 移除并返回指定名称的定时任务上下文。 */
    protected TimerTaskContextImpl removeTask(String taskName) {
        return scheduledTasks.remove(taskName);
    }

    /** 返回当前所有已调度任务的只读视图。 */
    protected Map<String, TimerTaskContextImpl> getTasks(){
        return scheduledTasks;
    }
}
